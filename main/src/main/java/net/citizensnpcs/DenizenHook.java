package net.citizensnpcs;

import java.util.UUID;

import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.JavaReflectedObjectTag;
import com.denizenscript.denizencore.scripts.containers.core.TaskScriptContainer;
import com.denizenscript.denizencore.scripts.queues.ScriptQueue;
import com.denizenscript.denizencore.utilities.YamlConfiguration;
import com.google.common.collect.Lists;

import net.citizensnpcs.api.expr.CompiledExpression;
import net.citizensnpcs.api.expr.ExpressionEngine;
import net.citizensnpcs.api.expr.ExpressionScope;

public class DenizenHook {
    private final Citizens plugin;

    public DenizenHook(Citizens plugin) {
        this.plugin = plugin;
        plugin.getExpressionRegistry().registerEngine(new DenizenEngine());
        plugin.getExpressionRegistry().setDefaultEngine("denizen");
    }

    public class DenizenEngine implements ExpressionEngine {
        @Override
        public CompiledExpression compile(String expression) throws ExpressionCompileException {
            YamlConfiguration config = new YamlConfiguration();
            config.set("type", "task");
            config.set("script", Lists.newArrayList(expression));
            TaskScriptContainer sc = new TaskScriptContainer(config, UUID.randomUUID().toString());
            return new CompiledExpression() {
                @Override
                public Object evaluate(ExpressionScope scope) {
                    ScriptQueue sq = sc.run(null, field -> {
                        switch (field) {
                            case "npc":
                                return new NPCTag(scope.getNPC());
                            case "player":
                                return new PlayerTag(scope.getPlayer());
                        }
                        Object value = scope.get(field);
                        if (value == null)
                            return null;
                        if (value instanceof Byte)
                            return new ElementTag((byte) value);
                        if (value instanceof Integer)
                            return new ElementTag((int) value);
                        if (value instanceof Double)
                            return new ElementTag((double) value);
                        if (value instanceof Character)
                            return new ElementTag((char) value);
                        if (value instanceof String)
                            return new ElementTag((String) value);
                        if (value instanceof Long)
                            return new ElementTag((long) value);
                        if (value instanceof Enum)
                            return new ElementTag((Enum<?>) value);
                        if (value instanceof Float)
                            return new ElementTag((float) value);
                        if (value instanceof Boolean)
                            return new ElementTag((boolean) value);
                        return new JavaReflectedObjectTag(scope.get(field));
                    });
                    sq.start();
                    return sq.getLastEntryExecuted().getObject("outcome");
                }
            };
        }

        @Override
        public String getName() {
            return "denizen";
        }
    }
}
