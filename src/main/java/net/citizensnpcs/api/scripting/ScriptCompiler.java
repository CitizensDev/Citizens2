package net.citizensnpcs.api.scripting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;

import net.citizensnpcs.api.util.Messaging;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;

/**
 * Compiles files into {@link ScriptFactory}s. Intended for use as a separate
 * thread - {@link ScriptCompiler#run()} will block while waiting for new tasks
 * to compile.
 */
public class ScriptCompiler {
    private final WeakReference<ClassLoader> classLoader;
    private final ScriptEngineManager engineManager;
    private final Map<String, ScriptEngine> engines = Maps.newHashMap();
    private final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {
        int n = 1;

        @Override
        public Thread newThread(Runnable r) {
            Thread created = new Thread(r, "Citizens Script Compiler #" + n++);
            created.setContextClassLoader(classLoader.get());
            return created;
        }
    });
    private final Function<File, SimpleScriptSource> fileEngineConverter = new Function<File, SimpleScriptSource>() {
        @Override
        public SimpleScriptSource apply(File file) {
            if (!file.isFile())
                return null;
            String fileName = file.getName();
            String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
            ScriptEngine engine = loadEngine(extension);
            if (engine == null)
                return null;
            return new SimpleScriptSource(file, engine);
        }
    };
    private final List<ContextProvider> globalContextProviders = Lists.newArrayList();

    public ScriptCompiler(ClassLoader classLoader) {
        engineManager = new ScriptEngineManager(classLoader);
        this.classLoader = new WeakReference<ClassLoader>(classLoader);
    }

    /**
     * Create a builder to compile the given files.
     * 
     * @param files
     *            The files to compile
     * @return The {@link CompileTaskBuilder}
     */
    public CompileTaskBuilder compile(File file) {
        if (file == null)
            throw new IllegalArgumentException("file should not be null");
        return new CompileTaskBuilder(fileEngineConverter.apply(file));
    }

    /**
     * Create a builder to compile the given source code.
     * 
     * @param src
     *            The source code to compile
     * @param identifier
     *            A unique identifier of the source code
     * @param extension
     *            The source code externsion
     * @return The {@link CompileTaskBuilder}
     */
    public CompileTaskBuilder compile(String src, String identifier, String extension) {
        if (src == null)
            throw new IllegalArgumentException("source must not be null");
        return new CompileTaskBuilder(new SimpleScriptSource(src, identifier, loadEngine(extension)));
    }

    public void interrupt() {
        executor.shutdownNow();
    }

    private ScriptEngine loadEngine(String extension) {
        ScriptEngine engine = engines.get(extension);
        if (engine == null) {
            ClassLoader replace = classLoader.get();
            ClassLoader old = null;
            if (replace != null) {
                old = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(replace);
            }
            ScriptEngine search = engineManager.getEngineByExtension(extension);
            if (replace != null) {
                Thread.currentThread().setContextClassLoader(old);
            }
            if (search != null && (!(search instanceof Compilable) || !(search instanceof Invocable)))
                search = null;
            engines.put(extension, search);
        }
        return engine;
    }

    /**
     * Registers a global {@link ContextProvider}, which will be invoked on all
     * scripts created by this ScriptCompiler.
     * 
     * @param provider
     *            The global provider
     */
    public void registerGlobalContextProvider(ContextProvider provider) {
        if (!globalContextProviders.contains(provider)) {
            globalContextProviders.add(provider);
        }
    }

    public void run(String code, String extension) throws ScriptException {
        run(code, extension, null);
    }

    public void run(String code, String extension, Map<String, Object> vars) throws ScriptException {
        ScriptEngine engine = loadEngine(extension);
        if (engine == null)
            throw new ScriptException("Couldn't load engine with extension " + extension);
        ScriptContext context = new SimpleScriptContext();
        if (vars != null)
            context.setBindings(new SimpleBindings(vars), ScriptContext.ENGINE_SCOPE);
        engine.eval(extension, context);
    }

    private class CompileTask implements Callable<ScriptFactory> {
        private final boolean cache;
        private final CompileCallback[] callbacks;
        private final ContextProvider[] contextProviders;
        private final SimpleScriptSource engine;
        private final Future<ScriptFactory> future;

        public CompileTask(CompileTaskBuilder builder) {
            List<ContextProvider> copy = Lists.newArrayList(builder.contextProviders);
            copy.addAll(globalContextProviders);
            this.contextProviders = copy.toArray(new ContextProvider[copy.size()]);
            this.callbacks = builder.callbacks.toArray(new CompileCallback[builder.callbacks.size()]);
            this.engine = builder.engine;
            this.future = new FutureTask<ScriptFactory>(this);
            this.cache = builder.cache;
        }

        @Override
        public ScriptFactory call() {
            if (cache && CACHE.containsKey(engine.getIdentifier()))
                return CACHE.get(engine.getIdentifier());
            Compilable compiler = (Compilable) engine.engine;
            Reader reader = null;
            try {
                CompiledScript src = compiler.compile(reader = engine.getReader());
                ScriptFactory compiled = new SimpleScriptFactory(src, contextProviders);
                if (cache)
                    CACHE.put(engine.getIdentifier(), compiled);
                for (CompileCallback callback : callbacks)
                    callback.onScriptCompiled(engine.getIdentifier(), compiled);
                return compiled;
            } catch (IOException e) {
                Messaging.severe("IO error while reading a file for scripting.");
                e.printStackTrace();
            } catch (ScriptException e) {
                Messaging.severe("Compile error while parsing script.");
                Throwables.getRootCause(e).printStackTrace();
            } catch (Throwable t) {
                Messaging.severe("Unexpected error while parsing script at.");
                t.printStackTrace();
            } finally {
                Closeables.closeQuietly(reader);
            }
            return null;
        }
    }

    public class CompileTaskBuilder {
        private boolean cache;
        private List<CompileCallback> callbacks;
        private final List<ContextProvider> contextProviders = Lists.newArrayList();
        private final SimpleScriptSource engine;

        private CompileTaskBuilder(SimpleScriptSource engine) {
            this.engine = engine;
        }

        public Future<ScriptFactory> beginWithFuture() {
            CompileTask t = new CompileTask(this);
            executor.submit(t);
            return t.future;
        }

        public CompileTaskBuilder cache(boolean cache) {
            this.cache = cache;
            return this;
        }

        public CompileTaskBuilder withCallback(CompileCallback callback) {
            callbacks.add(callback);
            return this;
        }

        public CompileTaskBuilder withContextProvider(ContextProvider provider) {
            contextProviders.add(provider);
            return this;
        }
    }

    private static class SimpleScriptSource {
        private final ScriptEngine engine;
        private final File file;
        private final String identifier;
        private final String src;

        private SimpleScriptSource(File file, ScriptEngine engine) {
            this.file = file;
            this.identifier = file.getAbsolutePath();
            this.engine = engine;
            this.src = null;
        }

        private SimpleScriptSource(String src, String identifier, ScriptEngine engine) {
            this.src = src;
            this.identifier = identifier;
            this.engine = engine;
            this.file = null;
        }

        public String getIdentifier() {
            return identifier;
        }

        @SuppressWarnings("resource")
        public Reader getReader() throws FileNotFoundException {
            return file == null ? new StringReader(src) : new FileReader(file);
        }
    }

    private static final Map<String, ScriptFactory> CACHE = new MapMaker().weakValues().makeMap();
}