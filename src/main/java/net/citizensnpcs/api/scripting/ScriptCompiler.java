package net.citizensnpcs.api.scripting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
            return created;
        }
    });
    private final Function<File, ScriptSource> fileEngineConverter = new Function<File, ScriptSource>() {
        @Override
        public ScriptSource apply(File file) {
            if (!file.isFile())
                return null;
            String fileName = file.getName();
            String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
            ScriptEngine engine = loadEngine(extension);
            if (engine == null)
                return null;
            return new ScriptSource(file, engine);
        }
    };
    private final List<ContextProvider> globalContextProviders = Lists.newArrayList();

    public ScriptCompiler(ClassLoader overrideClassLoader) {
        engineManager = new ScriptEngineManager(overrideClassLoader);
        classLoader = new WeakReference<ClassLoader>(overrideClassLoader);
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
        ScriptSource source = fileEngineConverter.apply(file);
        if (source == null)
            throw new IllegalArgumentException("could not recognise file");
        return new CompileTaskBuilder(source);
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
        return new CompileTaskBuilder(new ScriptSource(src, identifier, loadEngine(extension)));
    }

    public void interrupt() {
        executor.shutdownNow();
    }

    private ScriptEngine loadEngine(String extension) {
        ScriptEngine engine = engines.get(extension);
        if (engine != null)
            return engine;
        ScriptEngine search = engineManager.getEngineByExtension(extension);
        if (search != null && (!(search instanceof Compilable) || !(search instanceof Invocable)))
            search = null;
        engines.put(extension, search);
        ClassLoader cl = classLoader.get();
        if (cl != null) {
            updateSunClassLoader(cl);
        }
        return search;
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

    private void updateSunClassLoader(ClassLoader cl) {
        if (CLASSLOADER_OVERRIDE_ENABLED) {
            try {
                Object global = GET_GLOBAL.invoke(null);
                if (GET_APPLICATION_CLASS_LOADER.invoke(global) == null) {
                    INIT_APPLICATION_CLASS_LOADER.invoke(global, cl);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class CompileTask implements Callable<ScriptFactory> {
        private final boolean cache;
        private final CompileCallback[] callbacks;
        private final ContextProvider[] contextProviders;
        private final ScriptSource engine;

        public CompileTask(CompileTaskBuilder builder) {
            List<ContextProvider> copy = Lists.newArrayList(builder.contextProviders);
            copy.addAll(globalContextProviders);
            this.contextProviders = copy.toArray(new ContextProvider[copy.size()]);
            this.callbacks = builder.callbacks.toArray(new CompileCallback[builder.callbacks.size()]);
            this.engine = builder.engine;
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
                Messaging.severe("Unexpected error while parsing script.");
                t.printStackTrace();
            } finally {
                Closeables.closeQuietly(reader);
            }
            return null;
        }
    }

    public class CompileTaskBuilder {
        private boolean cache;
        private final List<CompileCallback> callbacks = Lists.newArrayList();
        private final List<ContextProvider> contextProviders = Lists.newArrayList();
        private final ScriptSource engine;

        private CompileTaskBuilder(ScriptSource engine) {
            this.engine = engine;
        }

        public Future<ScriptFactory> beginWithFuture() {
            CompileTask task = new CompileTask(this);
            return executor.submit(task);
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

    private static class ScriptSource {
        private final ScriptEngine engine;
        private final File file;
        private final String identifier;
        private final String src;

        private ScriptSource(File file, ScriptEngine engine) {
            this.file = file;
            this.identifier = file.getAbsolutePath();
            this.engine = engine;
            this.src = null;
        }

        private ScriptSource(String src, String identifier, ScriptEngine engine) {
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

    public static void main(String[] args) {
    }

    private static final Map<String, ScriptFactory> CACHE = new MapMaker().weakValues().makeMap();

    private static boolean CLASSLOADER_OVERRIDE_ENABLED;
    private static Method GET_APPLICATION_CLASS_LOADER, GET_GLOBAL, INIT_APPLICATION_CLASS_LOADER;
    static {
        try {
            Class<?> CONTEXT_FACTORY = Class.forName("sun.org.mozilla.javascript.internal.ContextFactory");
            GET_APPLICATION_CLASS_LOADER = CONTEXT_FACTORY.getDeclaredMethod("getApplicationClassLoader");
            GET_APPLICATION_CLASS_LOADER.setAccessible(true);
            GET_GLOBAL = CONTEXT_FACTORY.getDeclaredMethod("getGlobal");
            GET_GLOBAL.setAccessible(true);
            INIT_APPLICATION_CLASS_LOADER = CONTEXT_FACTORY.getDeclaredMethod("initApplicationClassLoader",
                    ClassLoader.class);
            INIT_APPLICATION_CLASS_LOADER.setAccessible(true);
            CLASSLOADER_OVERRIDE_ENABLED = true;
        } catch (Exception e) {
            Messaging
                    .severe("Unable to load Rhino script classes - javascript scripts will only be able to access CraftBukkit");
        }
    }
}