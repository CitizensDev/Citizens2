package net.citizensnpcs.npc.ai.tree;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.google.common.primitives.Doubles;

import net.citizensnpcs.api.expr.CompiledExpression;
import net.citizensnpcs.api.expr.ExpressionEngine;
import net.citizensnpcs.api.expr.ExpressionScope;
import net.citizensnpcs.api.expr.Memory;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Inventory;
import net.citizensnpcs.api.util.Placeholders;
import net.citizensnpcs.api.util.SpigotUtil;
import team.unnamed.mocha.MochaEngine;
import team.unnamed.mocha.runtime.MochaFunction;
import team.unnamed.mocha.runtime.value.Function;
import team.unnamed.mocha.runtime.value.MutableObjectBinding;
import team.unnamed.mocha.runtime.value.NumberValue;
import team.unnamed.mocha.runtime.value.ObjectProperty;
import team.unnamed.mocha.runtime.value.ObjectValue;
import team.unnamed.mocha.runtime.value.StringValue;
import team.unnamed.mocha.runtime.value.Value;

/**
 * Molang expression engine implementation using the Mocha library.
 */
public class MolangEngine implements ExpressionEngine {
    private final MochaEngine<?> engine;

    public MolangEngine() {
        this.engine = MochaEngine.createStandard();
    }

    @Override
    public CompiledExpression compile(String expression) throws ExpressionCompileException {
        try {
            return new MolangCompiledExpression(engine.prepareEval(expression), engine);
        } catch (Exception e) {
            throw new ExpressionCompileException("Failed to parse Molang expression: " + expression, e);
        }
    }

    @Override
    public String getName() {
        return "molang";
    }

    public static class ItemStackValue implements Value {
        private final ItemStack itemStack;

        public ItemStackValue(ItemStack itemStack) {
            this.itemStack = itemStack;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }
    }

    private static class LazyObjectBinding implements ObjectValue {
        private final Map<String, Value> eagerProperties = new HashMap<>();
        private final Map<String, Supplier<?>> lazyProperties = new HashMap<>();

        @Override
        public Value get(String property) {
            Value eagerValue = eagerProperties.get(property);
            if (eagerValue != null)
                return eagerValue;

            Supplier<?> supplier = lazyProperties.get(property);
            if (supplier != null) {
                Object value = supplier.get();
                return toMochaValue(value);
            }
            return NumberValue.zero();
        }

        @Override
        public ObjectProperty getProperty(String property) {
            return ObjectProperty.property(get(property), false);
        }

        @Override
        public boolean set(String property, Value value) {
            lazyProperties.remove(property);
            eagerProperties.put(property, value);
            return true;
        }

        public void setEager(String name, Value value) {
            eagerProperties.put(name, value);
        }

        public void setLazy(String name, Supplier<?> supplier) {
            lazyProperties.put(name, supplier);
        }

        @Override
        public String toString() {
            return "LazyObjectBinding [eagerProperties=" + eagerProperties + ", lazyProperties=" + lazyProperties + "]";
        }
    }

    private static class MolangCompiledExpression implements CompiledExpression {
        private final MochaEngine<?> baseEngine;
        private final MochaFunction function;

        MolangCompiledExpression(MochaFunction function, MochaEngine<?> baseEngine) {
            this.function = function;
            this.baseEngine = baseEngine;
        }

        private void bindCustomFunctions(MochaEngine<?> evalEngine, ExpressionScope scope) {
            Memory memory = scope.getMemory();
            NPC npc = scope.getNPC();

            evalEngine.scope().set("list", createListBinding(memory));
            evalEngine.scope().set("mem", createMemBinding(memory));
            if (npc != null) {
                evalEngine.scope().set("inv", createInvBinding(npc));
            }
            evalEngine.scope().set("item", createItemBinding());

            // papi('placeholder_name')
            evalEngine.scope().set("papi", (Function<?>) (context, args) -> {
                if (args.length() < 1)
                    return StringValue.of("");

                String placeholderName = args.next().eval().getAsString();
                Player player = scope.getPlayer();

                return new NumberParseableValue(Placeholders.replace(placeholderName, player));
            });
        }

        private void bindScopeVariables(MochaEngine<?> evalEngine, ExpressionScope scope) {
            Map<String, LazyObjectBinding> topLevelObjects = new HashMap<>();

            for (String name : scope.getVariableNames()) {
                String[] parts = name.split("\\.", 2);

                if (parts.length == 1) {
                    if (scope.isConstant(name)) {
                        Object value = scope.get(name);
                        if (value != null) {
                            evalEngine.scope().set(name, toMochaValue(value));
                        }
                    } else {
                        Supplier<?> supplier = scope.getSupplier(name);
                        if (supplier != null) {
                            evalEngine.scope().set(name, (Function<?>) (ctx, args) -> toMochaValue(supplier.get()));
                        }
                    }
                } else {
                    String topLevel = parts[0];
                    String remaining = parts[1];

                    LazyObjectBinding top = topLevelObjects.get(topLevel);
                    if (top == null) {
                        top = new LazyObjectBinding();
                        topLevelObjects.put(topLevel, top);
                        evalEngine.scope().set(topLevel, top);
                    }
                    setNestedProperty(top, remaining, scope, name);
                }
            }
        }

        @Override
        public Object evaluate(ExpressionScope scope) {
            bindScopeVariables(baseEngine, scope);
            bindCustomFunctions(baseEngine, scope);

            try {
                return function.evaluate();
            } catch (Exception e) {
                e.printStackTrace();
                return 0.0;
            }
        }

        @Override
        public boolean evaluateAsBoolean(ExpressionScope scope) {
            Object result = evaluate(scope);
            if (result instanceof Boolean)
                return (Boolean) result;

            if (result instanceof Number)
                return ((Number) result).doubleValue() != 0;

            return result != null;
        }

        @Override
        public double evaluateAsNumber(ExpressionScope scope) {
            Object result = evaluate(scope);
            if (result instanceof Number)
                return ((Number) result).doubleValue();

            if (result instanceof Boolean)
                return (Boolean) result ? 1.0 : 0.0;

            return 0.0;
        }

        @Override
        public String evaluateAsString(ExpressionScope scope) {
            Object result = evaluate(scope);
            return result == null ? "" : result.toString();
        }

        private void setNestedProperty(LazyObjectBinding parent, String path, ExpressionScope scope, String fullName) {
            String[] parts = path.split("\\.", 2);
            String currentPart = parts[0];

            if (parts.length == 1) {
                if (scope.isConstant(fullName)) {
                    Object value = scope.get(fullName);
                    if (value != null) {
                        parent.setEager(currentPart, toMochaValue(value));
                    }
                } else {
                    Supplier<?> supplier = scope.getSupplier(fullName);
                    if (supplier != null) {
                        parent.setLazy(currentPart, supplier);
                    }
                }
            } else {
                Value existing = parent.get(currentPart);
                LazyObjectBinding nested;
                if (existing instanceof LazyObjectBinding) {
                    nested = (LazyObjectBinding) existing;
                } else {
                    nested = new LazyObjectBinding();
                    parent.setEager(currentPart, nested);
                }
                setNestedProperty(nested, parts[1], scope, fullName);
            }
        }

        @Override
        public String toString() {
            return "MolangCompiledExpression [function=" + function + "]";
        }
    }

    private static class NumberParseableValue implements Value {
        private Double cache;
        private final String string;

        NumberParseableValue(String value) {
            this.string = value;
        }

        @Override
        public boolean getAsBoolean() {
            try {
                return Double.parseDouble(string) != 0;
            } catch (NumberFormatException e) {
                return "true".equalsIgnoreCase(string.trim()) || !string.isEmpty();
            }
        }

        @Override
        public double getAsNumber() {
            if (cache == null) {
                cache = Doubles.tryParse(string.trim());
            }
            return cache;
        }

        @Override
        public String getAsString() {
            return string;
        }
    }

    private static ObjectValue createInvBinding(NPC npc) {
        MutableObjectBinding binding = new MutableObjectBinding();

        // inv.has(material)
        binding.set("has", (Function<?>) (context, args) -> {
            if (npc == null || !npc.isSpawned() || args.length() < 1)
                return NumberValue.zero();
            ItemStack item = resolveItemStack(args.next().eval(), 1);
            if (item == null)
                return NumberValue.zero();

            Inventory inv = npc.getOrAddTrait(Inventory.class);
            for (ItemStack stack : inv.getContents()) {
                if (stack != null && stack.getType() == item.getType()) {
                    return NumberValue.of(1);
                }
            }
            return NumberValue.zero();
        });

        // inv.count(material)
        binding.set("count", (Function<?>) (context, args) -> {
            if (npc == null || !npc.isSpawned() || args.length() < 1)
                return NumberValue.zero();
            ItemStack item = resolveItemStack(args.next().eval(), 1);
            if (item == null)
                return NumberValue.zero();

            int count = 0;
            Inventory inv = npc.getOrAddTrait(Inventory.class);
            for (ItemStack stack : inv.getContents()) {
                if (stack != null && stack.getType() == item.getType()) {
                    count += stack.getAmount();
                }
            }
            return NumberValue.of(count);
        });

        // inv.add(item) or inv.add(material, amount)
        binding.set("add", (Function<?>) (context, args) -> {
            if (npc == null || !npc.isSpawned() || args.length() < 1)
                return NumberValue.zero();
            int amount = args.length() > 1 ? (int) args.next().eval().getAsNumber() : 1;
            ItemStack item = resolveItemStack(args.next().eval(), amount);
            if (item == null)
                return NumberValue.zero();

            Inventory inv = npc.getOrAddTrait(Inventory.class);
            ItemStack[] contents = inv.getContents();
            for (int i = 0; i < contents.length; i++) {
                if (contents[i] == null || contents[i].getType() == Material.AIR) {
                    contents[i] = item;
                    inv.setContents(contents);
                    return NumberValue.of(1);
                }
            }
            return NumberValue.zero();
        });

        // inv.remove(material, amount) - amount is optional, defaults to 1
        binding.set("remove", (Function<?>) (context, args) -> {
            if (npc == null || !npc.isSpawned() || args.length() < 1)
                return NumberValue.zero();
            int amount = args.length() > 1 ? (int) args.next().eval().getAsNumber() : 1;
            ItemStack item = resolveItemStack(args.next().eval(), amount);
            if (item == null)
                return NumberValue.zero();

            int remaining = amount;
            Inventory inv = npc.getOrAddTrait(Inventory.class);
            ItemStack[] contents = inv.getContents();
            for (int i = 0; i < contents.length && remaining > 0; i++) {
                if (contents[i] != null && contents[i].getType() == item.getType()) {
                    int stackAmount = contents[i].getAmount();
                    if (stackAmount <= remaining) {
                        contents[i] = null;
                        remaining -= stackAmount;
                    } else {
                        contents[i].setAmount(stackAmount - remaining);
                        remaining = 0;
                    }
                }
            }
            inv.setContents(contents);
            return remaining == 0 ? NumberValue.of(1) : NumberValue.zero();
        });

        // inv.clear()
        binding.set("clear", (Function<?>) (context, args) -> {
            if (npc == null || !npc.isSpawned())
                return NumberValue.zero();
            Inventory inv = npc.getOrAddTrait(Inventory.class);
            inv.setContents(new ItemStack[inv.getContents().length]);
            return NumberValue.of(1);
        });

        // inv.set_hand(item) or inv.set_hand(material)
        binding.set("set_hand", (Function<?>) (context, args) -> {
            if (npc == null || !npc.isSpawned() || args.length() < 1)
                return NumberValue.zero();
            ItemStack item = resolveItemStack(args.next().eval(), 1);
            if (item == null)
                return NumberValue.zero();

            if (npc.getEntity() instanceof Player) {
                ((Player) npc.getEntity()).getInventory().setItemInMainHand(item);
                return NumberValue.of(1);
            }
            EntityEquipment equip = npc.getEntity() instanceof LivingEntity
                    ? ((LivingEntity) npc.getEntity()).getEquipment()
                    : null;
            if (equip != null) {
                equip.setItemInMainHand(item);
                return NumberValue.of(1);
            }
            return NumberValue.zero();
        });

        // inv.set_offhand(item) or inv.set_offhand(material)
        binding.set("set_offhand", (Function<?>) (context, args) -> {
            if (npc == null || !npc.isSpawned() || args.length() < 1)
                return NumberValue.zero();
            ItemStack item = resolveItemStack(args.next().eval(), 1);
            if (item == null)
                return NumberValue.zero();

            if (npc.getEntity() instanceof Player) {
                ((Player) npc.getEntity()).getInventory().setItemInOffHand(item);
                return NumberValue.of(1);
            }
            EntityEquipment equip = npc.getEntity() instanceof LivingEntity
                    ? ((LivingEntity) npc.getEntity()).getEquipment()
                    : null;
            if (equip != null) {
                equip.setItemInOffHand(item);
                return NumberValue.of(1);
            }
            return NumberValue.zero();
        });

        // inv.hand_is(material)
        binding.set("hand_is", (Function<?>) (context, args) -> {
            if (npc == null || !npc.isSpawned() || args.length() < 1)
                return NumberValue.zero();
            ItemStack item = resolveItemStack(args.next().eval(), 1);
            if (item == null)
                return NumberValue.zero();

            EntityEquipment equip = npc.getEntity() instanceof LivingEntity
                    ? ((LivingEntity) npc.getEntity()).getEquipment()
                    : null;
            if (equip != null) {
                ItemStack hand = equip.getItemInMainHand();
                return hand != null && hand.getType() == item.getType() ? NumberValue.of(1) : NumberValue.zero();
            }
            return NumberValue.zero();
        });

        // inv.equip(slot, item) or inv.equip(slot, material)
        binding.set("equip", (Function<?>) (context, args) -> {
            if (npc == null || !npc.isSpawned() || args.length() < 2)
                return NumberValue.zero();
            String slotName = args.next().eval().getAsString();
            ItemStack item = resolveItemStack(args.next().eval(), 1);
            if (item == null)
                return NumberValue.zero();

            EntityEquipment equip = npc.getEntity() instanceof LivingEntity
                    ? ((LivingEntity) npc.getEntity()).getEquipment()
                    : null;
            if (equip == null)
                return NumberValue.zero();

            try {
                EquipmentSlot slot = EquipmentSlot.valueOf(slotName.toUpperCase());
                equip.setItem(slot, item);
                return NumberValue.of(1);
            } catch (IllegalArgumentException e) {
                return NumberValue.zero();
            }
        });

        return binding;
    }

    private static ObjectValue createItemBinding() {
        MutableObjectBinding binding = new MutableObjectBinding();

        // item.from_component(itemString) or item.from_component(itemString, amount)
        binding.set("from_component", (Function<?>) (context, args) -> {
            if (args.length() < 1)
                return NumberValue.zero();
            String itemString = args.next().eval().getAsString();
            int amount = args.length() > 1 ? (int) args.next().eval().getAsNumber() : 1;

            ItemStack item = SpigotUtil.parseItemStack(null, itemString);
            if (amount > 1) {
                item.setAmount(amount);
            }
            return new ItemStackValue(item);
        });

        return binding;
    }

    private static ObjectValue createListBinding(Memory memory) {
        MutableObjectBinding binding = new MutableObjectBinding();

        // list.add(key, value)
        binding.set("add", (Function<?>) (context, args) -> {
            if (memory == null || args.length() < 2)
                return NumberValue.zero();
            String key = args.next().eval().getAsString();
            double value = args.next().eval().getAsNumber();
            memory.listAdd(key, value);
            return NumberValue.of(1);
        });

        // list.remove(key, value)
        binding.set("remove", (Function<?>) (context, args) -> {
            if (memory == null || args.length() < 2)
                return NumberValue.zero();
            String key = args.next().eval().getAsString();
            double value = args.next().eval().getAsNumber();
            return memory.listRemove(key, value) ? NumberValue.of(1) : NumberValue.zero();
        });

        // list.remove_at(key, index)
        binding.set("remove_at", (Function<?>) (context, args) -> {
            if (memory == null || args.length() < 2)
                return NumberValue.zero();
            String key = args.next().eval().getAsString();
            int index = (int) args.next().eval().getAsNumber();
            return memory.listRemoveAt(key, index) != null ? NumberValue.of(1) : NumberValue.zero();
        });

        // list.clear(key)
        binding.set("clear", (Function<?>) (context, args) -> {
            if (memory == null || args.length() < 1)
                return NumberValue.zero();
            String key = args.next().eval().getAsString();
            memory.listClear(key);
            return NumberValue.of(1);
        });

        // list.size(key)
        binding.set("size", (Function<?>) (context, args) -> {
            if (memory == null || args.length() < 1)
                return NumberValue.zero();
            String key = args.next().eval().getAsString();
            return NumberValue.of(memory.listSize(key));
        });

        // list.get(key, index)
        binding.set("get", (Function<?>) (context, args) -> {
            if (memory == null || args.length() < 2)
                return NumberValue.zero();
            String key = args.next().eval().getAsString();
            int index = (int) args.next().eval().getAsNumber();
            Object value = memory.listGet(key, index);
            if (value instanceof Number) {
                return NumberValue.of(((Number) value).doubleValue());
            }
            return NumberValue.zero();
        });

        // list.contains(key, value)
        binding.set("contains", (Function<?>) (context, args) -> {
            if (memory == null || args.length() < 2)
                return NumberValue.zero();
            String key = args.next().eval().getAsString();
            double value = args.next().eval().getAsNumber();
            return memory.listContains(key, value) ? NumberValue.of(1) : NumberValue.zero();
        });

        return binding;
    }

    private static ObjectValue createMemBinding(Memory memory) {
        MutableObjectBinding binding = new MutableObjectBinding();

        // mem.set(key, value)
        binding.set("set", (Function<?>) (context, args) -> {
            if (memory == null || args.length() < 2)
                return NumberValue.zero();
            String key = args.next().eval().getAsString();
            double value = args.next().eval().getAsNumber();
            memory.set(key, value);
            return NumberValue.of(value);
        });

        // mem.get(key) or mem.get(key, default)
        binding.set("get", (Function<?>) (context, args) -> {
            if (memory == null || args.length() < 1)
                return NumberValue.zero();
            String key = args.next().eval().getAsString();
            double defaultValue = args.length() > 1 ? args.next().eval().getAsNumber() : 0;
            return NumberValue.of(memory.getNumber(key, defaultValue));
        });

        // mem.has(key)
        binding.set("has", (Function<?>) (context, args) -> {
            if (memory == null || args.length() < 1)
                return NumberValue.zero();
            String key = args.next().eval().getAsString();
            return memory.has(key) ? NumberValue.of(1) : NumberValue.zero();
        });

        // mem.remove(key)
        binding.set("remove", (Function<?>) (context, args) -> {
            if (memory == null || args.length() < 1)
                return NumberValue.zero();
            String key = args.next().eval().getAsString();
            memory.remove(key);
            return NumberValue.of(1);
        });

        return binding;
    }

    private static ItemStack resolveItemStack(Value value, int amount) {
        if (value instanceof ItemStackValue) {
            ItemStack stack = ((ItemStackValue) value).getItemStack();
            if (amount != stack.getAmount()) {
                stack = stack.clone();
                stack.setAmount(amount);
            }
            return stack;
        }
        String materialName = value.getAsString();
        if (materialName != null && !materialName.isEmpty()) {
            Material mat = Material.matchMaterial(materialName);
            if (mat != null) {
                return new ItemStack(mat, amount);
            }
        }
        return null;
    }

    /**
     * Converts a Java object to a Mocha Value.
     */
    private static Value toMochaValue(Object value) {
        if (value == null)
            return NumberValue.zero();

        if (value instanceof Value)
            return (Value) value;

        if (value instanceof Number)
            return NumberValue.of(((Number) value).doubleValue());

        if (value instanceof Boolean)
            return NumberValue.of((Boolean) value ? 1.0 : 0.0);

        if (value instanceof String)
            return StringValue.of(value.toString());

        if (value instanceof ItemStack)
            return new ItemStackValue((ItemStack) value);

        return NumberValue.zero();
    }
}