package net.citizensnpcs.api.scripting;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map.Entry;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;

//TODO find another implementation than Sun's, which uses different API...
public class RhinoScriptEngine extends AbstractScriptEngine implements Invocable, Compilable {
    private ScriptEngineFactory factory;

    @Override
    public CompiledScript compile(Reader from) throws ScriptException {
        Context cx = Context.enter();
        cx.initStandardObjects();
        String filename = (filename = (String) get(ScriptEngine.FILENAME)) == null ? "<unknown>" : filename;
        try {
            Script compiled = cx.compileReader(from, filename, 0, null);
            return new RhinoCompiledScript(this, compiled);
        } catch (IOException e) {
            throw new ScriptException(e);
        } finally {
            Context.exit();
        }
    }

    @Override
    public CompiledScript compile(String script) throws ScriptException {
        return compile(new StringReader(script));
    }

    @Override
    public Bindings createBindings() {
        return new SimpleBindings();
    }

    @Override
    public Object eval(Reader reader, ScriptContext context) throws ScriptException {
        Context cx = Context.enter();
        Scriptable scope = cx.initStandardObjects(new ImporterTopLevel(cx));
        String filename = (filename = (String) get(ScriptEngine.FILENAME)) == null ? "<unknown>" : filename;
        for (Entry<String, Object> entry : context.getBindings(ScriptContext.ENGINE_SCOPE).entrySet()) {
            ScriptableObject.putProperty(scope, entry.getKey(), Context.javaToJS(entry.getValue(), scope));
        }
        try {
            return cx.evaluateReader(scope, reader, filename, 0, filename);
        } catch (Error e) {
            throw new ScriptException(e.getMessage());
        } catch (IOException e) {
            throw new ScriptException(e);
        } catch (RhinoException e) {
            String msg = e instanceof JavaScriptException ? ((JavaScriptException) e).getValue().toString() : e
                    .getMessage();
            int lineNumber = e.lineNumber() == 0 ? -1 : e.lineNumber();
            ScriptException scriptException = new ScriptException(msg, e.sourceName(), lineNumber);
            scriptException.initCause(e);
            throw scriptException;
        } finally {
            Context.exit();
        }
    }

    @Override
    public Object eval(String script, ScriptContext context) throws ScriptException {
        return eval(new StringReader(script), context);
    }

    @Override
    public ScriptEngineFactory getFactory() {
        return factory;
    }

    @Override
    public <T> T getInterface(Class<T> clazz) {
        try {
            return clazz.cast(Context.jsToJava(null, clazz));
        } catch (EvaluatorException ex) {
            return null;
        }
    }

    @Override
    public <T> T getInterface(Object thiz, Class<T> clazz) {
        try {
            return clazz.cast(Context.jsToJava(thiz, clazz));
        } catch (EvaluatorException ex) {
            return null;
        }
    }

    @Override
    public Object invokeFunction(String name, Object... args) throws ScriptException, NoSuchMethodException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object invokeMethod(Object thiz, String name, Object... args) throws ScriptException, NoSuchMethodException {
        // TODO Auto-generated method stub
        return null;
    }

    public ScriptEngine setScriptEngineFactory(ScriptEngineFactory factory) {
        this.factory = factory;
        return this;
    }

    public static class RhinoCompiledScript extends CompiledScript {
        private final ScriptEngine engine;
        private final Script script;

        RhinoCompiledScript(ScriptEngine engine, Script script) {
            this.engine = engine;
            this.script = script;
        }

        @Override
        public Object eval(ScriptContext context) throws ScriptException {
            Context cx = Context.enter();
            try {
                return unwrapReturnValue(script.exec(cx, cx.initStandardObjects()));
            } catch (Exception e) {
            } finally {
                Context.exit();
            }
            return null;
        }

        @Override
        public ScriptEngine getEngine() {
            return this.engine;
        }
    }

    private static Object unwrapReturnValue(Object obj) {
        if (obj instanceof Wrapper) {
            obj = ((Wrapper) obj).unwrap();
        }
        return obj instanceof Undefined ? null : obj;
    }
}
