package net.citizensnpcs.api.scripting;

import java.io.Reader;
import java.io.StringReader;

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
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;

//TODO find another implementation than Sun's, which uses different API...
public class RhinoScriptEngine extends AbstractScriptEngine implements Invocable, Compilable {
    private ScriptEngineFactory factory;

    @Override
    public CompiledScript compile(Reader script) throws ScriptException {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object eval(String script, ScriptContext context) throws ScriptException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ScriptEngineFactory getFactory() {
        return factory;
    }

    @Override
    public <T> T getInterface(Class<T> clasz) {
        try {
            return clasz.cast(Context.jsToJava(null, clasz));
        } catch (EvaluatorException ex) {
            return null;
        }
    }

    @Override
    public <T> T getInterface(Object thiz, Class<T> clasz) {
        try {
            return clasz.cast(Context.jsToJava(thiz, clasz));
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

    private static Object unwrapReturnValue(Object obj) {
        if (obj instanceof Wrapper) {
            obj = ((Wrapper) obj).unwrap();
        }
        return obj instanceof Undefined ? null : obj;
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
                return unwrapReturnValue(script.exec(cx, null));
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
}
