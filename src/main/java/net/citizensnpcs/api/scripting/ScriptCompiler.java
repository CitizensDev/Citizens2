package net.citizensnpcs.api.scripting;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.google.common.base.Function;
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
    private final ScriptEngineManager engineManager = new ScriptEngineManager();
    private final Map<String, ScriptEngine> engines = Maps.newHashMap();
    private final Function<File, FileEngine> fileEngineConverter = new Function<File, FileEngine>() {
        @Override
        public FileEngine apply(File file) {
            if (!file.isFile())
                return null;
            String fileName = file.getName();
            String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
            if (!engines.containsKey(extension)) {
                ScriptEngine search = engineManager.getEngineByExtension(extension);
                if (search != null && (!(search instanceof Compilable) || !(search instanceof Invocable)))
                    search = null;
                engines.put(extension, search);
            }
            ScriptEngine engine = engineManager.getEngineByExtension(extension);
            if (engine == null)
                return null;
            return new FileEngine(file, engine);
        }
    };
    private final List<ContextProvider> globalContextProviders = Lists.newArrayList();
    private final BlockingQueue<CompileTask> toCompile = new LinkedBlockingQueue<CompileTask>();

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
        Iterable<FileEngine> toCompile = Iterables.transform(Lists.newArrayList(files), fileEngineConverter);
        return new CompileTaskBuilder(Iterables.toArray(toCompile, FileEngine.class));
    }

    /**
     * Registers a global {@link ContextProvider}, which will be invoked on all
     * scripts created by this ScriptCompiler.
     * 
     * @param provider
     *            The global provider
     */
    public void registerGlobalContextProvider(ContextProvider provider) {
        synchronized (globalContextProviders) {
            if (!globalContextProviders.contains(provider)) {
                globalContextProviders.add(provider);
            }
        }
    }

    @Override
    public void run() {
        try {
            CompileTask task = toCompile.take();
            for (FileEngine engine : task.files) {
                Compilable compiler = (Compilable) engine.engine;
                Reader reader = null;
                try {
                    reader = new FileReader(engine.file);
                    CompiledScript src = compiler.compile(reader);
                    for (CompileCallback callback : task.callbacks) {
                        synchronized (callback) {
                            callback.onScriptCompiled(new SimpleScriptFactory(src, task.contextProviders));
                        }
                    }
                } catch (IOException ex) {
                    System.err.println("[Citizens]: IO fail while reading " + engine.file + " for scripting.");
                    ex.printStackTrace();
                } catch (ScriptException e) {
                    System.err.println("[Citizens]: Compile error while parsing script at " + engine.file.getName()
                            + ".");
                    e.printStackTrace();
                } finally {
                    Closeables.closeQuietly(reader);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class CompileTask {
        private final CompileCallback[] callbacks;
        private final ContextProvider[] contextProviders;
        private final FileEngine[] files;

        public CompileTask(CompileTaskBuilder builder) {
            List<ContextProvider> copy = Lists.newArrayList(builder.contextProviders);
            copy.addAll(globalContextProviders);
            this.contextProviders = copy.toArray(new ContextProvider[0]);
            this.files = builder.files;
            this.callbacks = builder.callbacks.toArray(new CompileCallback[0]);
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

    private static class FileEngine { // File + ScriptEngine POJO
        ScriptEngine engine;
        File file;

        FileEngine(File file, ScriptEngine engine) {
            this.file = file;
            this.engine = engine;
        }
    }
}
