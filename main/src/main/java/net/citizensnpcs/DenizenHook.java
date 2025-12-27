package net.citizensnpcs;

import java.util.List;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.flags.FlaggableObject;
import com.denizenscript.denizencore.flags.SavableMapFlagTracker;
import com.denizenscript.denizencore.objects.Adjustable;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.JavaReflectedObjectTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.containers.core.TaskScriptContainer;
import com.denizenscript.denizencore.scripts.queues.ScriptQueue;
import com.denizenscript.denizencore.utilities.YamlConfiguration;
import com.google.common.collect.Lists;

import net.citizensnpcs.api.expr.CompiledExpression;
import net.citizensnpcs.api.expr.ExpressionEngine;
import net.citizensnpcs.api.expr.ExpressionScope;
import net.citizensnpcs.api.expr.Memory;

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
                private ObjectTag convert(Object value) {
                    if (value == null)
                        return null;
                    if (value instanceof List)
                        return new ListTag((List<? extends ObjectTag>) Lists.transform((List) value, this::convert));
                    if (value instanceof ItemStack)
                        return new ItemTag((ItemStack) value);
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
                    return new JavaReflectedObjectTag(value);
                }

                @Override
                public Object evaluate(ExpressionScope scope) {
                    ScriptQueue sq = sc.run(null, field -> {
                        switch (field) {
                            case "npc":
                                return new NPCTag(scope.getNPC());
                            case "player":
                                return new PlayerTag(scope.getPlayer());
                            case "memory":
                                return new MemoryTag(scope.getMemory());
                        }
                        return convert(scope.get(field));
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

    public static class MemoryTag implements ObjectTag, Adjustable, FlaggableObject {
        private final SavableMapFlagTracker flag = new SavableMapFlagTracker();
        private final Memory memory;
        private String prefix = "memory";

        public MemoryTag(Memory memory) {
            this.memory = memory;
        }

        @Override
        public void adjust(Mechanism mech) {
            if (mech.matches("set")) {
                ListTag value = mech.valueAsType(ListTag.class);
                if (value.size() != 2) {
                    mech.echoError("Must have key and value");
                    return;
                }
                memory.set(value.get(0), value.getObject(1).asElement().getJavaObject());
                return;
            }
        }

        @Override
        public void applyProperty(Mechanism mech) {
            adjust(mech);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null || getClass() != obj.getClass())
                return false;

            MemoryTag other = (MemoryTag) obj;
            if (memory == null) {
                if (other.memory != null) {
                    return false;
                }
            } else if (!memory.equals(other.memory)) {
                return false;
            }
            return true;
        }

        @Override
        public AbstractFlagTracker getFlagTracker() {
            return flag;
        }

        @Override
        public String getPrefix() {
            return prefix;
        }

        @Override
        public int hashCode() {
            return 31 + ((memory == null) ? 0 : memory.hashCode());
        }

        @Override
        public String identify() {
            return memory.toString();
        }

        @Override
        public String identifySimple() {
            return memory.toString();
        }

        @Override
        public boolean isUnique() {
            return true;
        }

        @Override
        public void reapplyTracker(AbstractFlagTracker arg0) {
        }

        @Override
        public ObjectTag setPrefix(String prefix) {
            this.prefix = prefix;
            return this;
        }
    }
}
