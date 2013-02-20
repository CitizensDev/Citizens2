package net.citizensnpcs.api.scripting;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

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
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;

/**
 * Compiles files into {@link ScriptFactory}s. Intended for use as a separate
 * thread - {@link ScriptCompiler#run()} will block while waiting for new tasks
 * to compile.
 */
public class ScriptCompiler implements Runnable {
    private final ScriptEngineManager engineManager;
    private final Map<String, ScriptEngine> engines = Maps.newHashMap();
    private final Function<File, FileEngine> fileEngineConverter = new Function<File, FileEngine>() {
        @Override
        public FileEngine apply(File file) {
            if (!file.isFile())
                return null;
            String fileName = file.getName();
            String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
            ScriptEngine engine = loadEngine(extension);
            if (engine == null)
                return null;
            return new FileEngine(file, engine);
        }
    };
    private final List<ContextProvider> globalContextProviders = Lists.newArrayList();
    private final Thread runningThread;
    private final BlockingQueue<CompileTask> toCompile = new ArrayBlockingQueue<CompileTask>(50);
    private final WeakReference<ClassLoader> classLoader;

    public ScriptCompiler(ClassLoader classLoader) {
        engineManager = new ScriptEngineManager(classLoader);
        runningThread = new Thread(this, "Citizens Script Compiler");
        runningThread.setContextClassLoader(classLoader);
        runningThread.start();
        this.classLoader = new WeakReference<ClassLoader>(classLoader);
    }

    /**
     * Create a builder to compile the given files.
     * 
     * @param files
     *            The files to compile
     * @return The {@link CompileTaskBuilder}
     */
    public CompileTaskBuilder compile(File... files) {
        if (files == null || files.length == 0)
            throw new IllegalArgumentException("files should have a length of at least one");
        List<FileEngine> toCompile = Lists.newArrayList();
        for (File file : files) {
            FileEngine res = fileEngineConverter.apply(file);
            if (res != null)
                toCompile.add(res);
        }
        return new CompileTaskBuilder(toCompile.toArray(new FileEngine[toCompile.size()]));
    }

    /**
     * A helper method for {@link #compile(File...)}
     */
    public CompileTaskBuilder compile(Iterable<File> files) {
        return compile(Iterables.toArray(files, File.class));
    }

    public void interrupt() {
        runningThread.interrupt();
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

    @Override
    public void run() {
        while (true) {
            CompileTask task;
            try {
                task = toCompile.take();
            } catch (InterruptedException e) {
                return;
            }
            for (FileEngine engine : task.files) {
                Compilable compiler = (Compilable) engine.engine;
                Reader reader = null;
                try {
                    reader = new FileReader(engine.file);
                    CompiledScript src = compiler.compile(reader);
                    ScriptFactory compiled = new SimpleScriptFactory(src, task.contextProviders);
                    for (CompileCallback callback : task.callbacks) {
                        callback.onScriptCompiled(engine.file, compiled);
                    }
                } catch (IOException e) {
                    Messaging.severe("IO error while reading " + engine.file + " for scripting.");
                    e.printStackTrace();
                } catch (ScriptException e) {
                    Messaging.severe("Compile error while parsing script at " + engine.file.getName() + ".");
                    Throwables.getRootCause(e).printStackTrace();
                } catch (Throwable t) {
                    Messaging.severe("[Unexpected error while parsing script at " + engine.file.getName() + ".");
                    t.printStackTrace();
                } finally {
                    Closeables.closeQuietly(reader);
                }
            }
            for (CompileCallback callback : task.callbacks) {
                callback.onCompileTaskFinished();
            }
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

    private class CompileTask {
        private final CompileCallback[] callbacks;
        private final ContextProvider[] contextProviders;
        private final FileEngine[] files;

        public CompileTask(CompileTaskBuilder builder) {
            List<ContextProvider> copy = Lists.newArrayList(builder.contextProviders);
            copy.addAll(globalContextProviders);
            this.contextProviders = copy.toArray(new ContextProvider[copy.size()]);
            this.files = builder.files;
            this.callbacks = builder.callbacks.toArray(new CompileCallback[builder.callbacks.size()]);
        }
    }

    public class CompileTaskBuilder {
        private final List<CompileCallback> callbacks = Lists.newArrayList();
        private final List<ContextProvider> contextProviders = Lists.newArrayList();
        private final FileEngine[] files;

        private CompileTaskBuilder(FileEngine[] files) {
            this.files = files;
        }

        public boolean begin() {
            return toCompile.offer(new CompileTask(this));
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

    private static class FileEngine {
        final ScriptEngine engine;
        final File file;

        FileEngine(File file, ScriptEngine engine) {
            this.file = file;
            this.engine = engine;
        }
    }
}