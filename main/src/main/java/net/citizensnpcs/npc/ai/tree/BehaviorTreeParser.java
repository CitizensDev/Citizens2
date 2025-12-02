package net.citizensnpcs.npc.ai.tree;

import java.util.List;
import java.util.function.Supplier;

import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import net.citizensnpcs.api.ai.tree.Behavior;
import net.citizensnpcs.api.ai.tree.BehaviorRegistry;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.ai.tree.CoalescedBehavior;
import net.citizensnpcs.api.ai.tree.IfElse;
import net.citizensnpcs.api.ai.tree.InstantBehavior;
import net.citizensnpcs.api.ai.tree.InverterDecorator;
import net.citizensnpcs.api.ai.tree.Loop;
import net.citizensnpcs.api.ai.tree.ParallelBehaviorWrapper;
import net.citizensnpcs.api.ai.tree.ParallelComposite;
import net.citizensnpcs.api.ai.tree.Selector;
import net.citizensnpcs.api.ai.tree.Sequence;
import net.citizensnpcs.api.ai.tree.TimeoutDecorator;
import net.citizensnpcs.api.expr.CompiledExpression;
import net.citizensnpcs.api.expr.ExpressionEngine;
import net.citizensnpcs.api.expr.ExpressionEngine.ExpressionCompileException;
import net.citizensnpcs.api.expr.ExpressionRegistry;
import net.citizensnpcs.api.expr.ExpressionRegistry.ExpressionValue;
import net.citizensnpcs.api.expr.ExpressionScope;
import net.citizensnpcs.api.expr.Memory;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Util;

/**
 * Parses DataKey structures into Behavior trees.
 *
 * The eval block allows evaluating expressions without backtick wrappers. It takes the expression language as an
 * argument (e.g., "eval molang:"). If no language is specified, the default engine is used.
 *
 * Lines starting with ` are also auto-evaluated as expressions:
 *
 * <pre>
 *   - `inv.add(item.json('{"material":"DIAMOND","amount":64}'))`
 * </pre>
 */
public class BehaviorTreeParser {
    private final BehaviorRegistry registry;

    public BehaviorTreeParser(BehaviorRegistry registry) {
        this.registry = registry;
    }

    private List<Behavior> coalesceInstantBehaviors(List<Behavior> behaviors) {
        List<Behavior> result = Lists.newArrayList();
        List<Behavior> instantGroup = Lists.newArrayList();

        for (Behavior behavior : behaviors) {
            if (behavior instanceof InstantBehavior) {
                instantGroup.add(behavior);
            } else {
                if (!instantGroup.isEmpty()) {
                    result.add(instantGroup.size() == 1 ? instantGroup.get(0) : new CoalescedBehavior(instantGroup));
                    instantGroup = Lists.newArrayList();
                }
                result.add(behavior);
            }
        }
        if (!instantGroup.isEmpty()) {
            result.add(instantGroup.size() == 1 ? instantGroup.get(0) : new CoalescedBehavior(instantGroup));
        }
        return result;
    }

    private Behavior createCommandBehavior(String command, BehaviorRegistry.BehaviorContext context) {
        ExpressionRegistry expressions = registry.getExpressionRegistry();
        ExpressionValue commandHolder = expressions.parseValue(command);

        return new InstantBehavior() {
            @Override
            public BehaviorStatus run() {
                String command = commandHolder.evaluateAsString(context.getScope());
                Util.runCommand(context.getNPC(),
                        context.getNPC().getEntity() instanceof Player ? ((Player) context.getNPC().getEntity()) : null,
                        command, command.contains("-o"), command.contains("-p"));
                return BehaviorStatus.SUCCESS;
            }

            @Override
            public String toString() {
                return "CommandBehavior[" + command + "]";
            }
        };
    }

    /**
     * Creates a behavior that evaluates an expression for its side effects. This allows lines like "-
     * `inv.add(item.create('DIAMOND', 64))`" without needing an explicit "eval" behavior.
     */
    private Behavior createExpressionBehavior(String expression, BehaviorRegistry.BehaviorContext context) {
        ExpressionRegistry exprRegistry = registry.getExpressionRegistry();
        ExpressionValue exprHolder = exprRegistry.parseValue(expression);

        return new InstantBehavior() {
            @Override
            public BehaviorStatus run() {
                exprHolder.evaluate(context.getScope());
                return BehaviorStatus.SUCCESS;
            }

            @Override
            public String toString() {
                return "ExpressionBehavior[" + expression + "]";
            }
        };
    }

    /**
     * Parses a behavior tree from a DataKey.
     */
    public Behavior parse(DataKey root, NPC npc, ExpressionScope scope, Memory blackboard) {
        scope.setMemory(blackboard);
        scope.setNPC(npc);
        BehaviorRegistry.BehaviorContext context = new BehaviorRegistry.BehaviorContext(npc, scope,
                registry.getExpressionRegistry(), blackboard);
        return parseNode(root, context);
    }

    private Supplier<Boolean> parseCondition(String conditionStr, BehaviorRegistry.BehaviorContext context) {
        ExpressionRegistry exprRegistry = registry.getExpressionRegistry();

        if (exprRegistry.isPossiblyExpression(conditionStr)) {
            try {
                CompiledExpression expr = exprRegistry.compile(conditionStr);
                return () -> expr.evaluateAsBoolean(context.getScope());
            } catch (ExpressionCompileException e) {
                e.printStackTrace();
                return () -> false;
            }
        }
        if (conditionStr.equalsIgnoreCase("true") || conditionStr.equals("1"))
            return () -> true;

        if (conditionStr.equalsIgnoreCase("false") || conditionStr.equals("0"))
            return () -> false;

        return () -> {
            Object value = context.getScope().get(conditionStr);
            if (value instanceof Boolean)
                return (Boolean) value;

            if (value instanceof Number)
                return ((Number) value).doubleValue() != 0;

            return value != null;
        };
    }

    private Behavior parseContainer(String name, DataKey key, BehaviorRegistry.BehaviorContext context) {
        if (name.startsWith("timeout ")) {
            String timeoutStr = name.substring(8).trim();
            ExpressionRegistry exprRegistry = registry.getExpressionRegistry();
            ExpressionValue timeoutHolder = exprRegistry.parseValue(timeoutStr);
            int ticks = (int) timeoutHolder.evaluateAsNumber(context.getScope());

            List<Behavior> children = parseContainerChildren(key, context);
            Behavior body = children.size() == 1 ? children.get(0)
                    : Sequence.createSequence(coalesceInstantBehaviors(children));
            return new TimeoutDecorator(body, ticks);
        }
        if (name.equalsIgnoreCase("invert")) {
            List<Behavior> children = parseContainerChildren(key, context);
            Behavior body = children.size() == 1 ? children.get(0)
                    : Sequence.createSequence(coalesceInstantBehaviors(children));
            return new InverterDecorator(body);
        }
        if (name.startsWith("loop ")) {
            String conditionStr = name.substring(5).trim();
            Supplier<Boolean> condition = parseCondition(conditionStr, context);

            List<Behavior> children = parseContainerChildren(key, context);
            Behavior body = children.size() == 1 ? children.get(0)
                    : Sequence.createSequence(coalesceInstantBehaviors(children));
            return Loop.createWithCondition(body, condition);
        }
        if (name.equalsIgnoreCase("sequence") || name.equalsIgnoreCase("seq")) {
            List<Behavior> children = parseContainerChildren(key, context);
            return Sequence.createSequence(coalesceInstantBehaviors(children));
        }
        if (name.equalsIgnoreCase("random") || name.equalsIgnoreCase("selector")) {
            List<Behavior> children = parseContainerChildren(key, context);
            return Selector.selecting(children).build();
        }
        if (name.equalsIgnoreCase("parallel")) {
            List<Behavior> parallelWrapped = Lists.newArrayList();
            for (Behavior child : parseContainerChildren(key, context)) {
                parallelWrapped.add(new ParallelBehaviorWrapper(child));
            }
            return new ParallelComposite(parallelWrapped);
        }
        if (name.equalsIgnoreCase("eval") || name.toLowerCase().startsWith("eval ")) {
            String language = null;
            if (name.length() > 5) {
                language = name.substring(5).trim();
            }
            return parseEvalBlock(key, language, context);
        }
        return parseLeaf(name, key, context);
    }

    private List<Behavior> parseContainerChildren(DataKey key, BehaviorRegistry.BehaviorContext context) {
        List<Behavior> children = Lists.newArrayList();
        DataKey ifKey = null;
        DataKey elseKey = null;
        for (DataKey sub : key.getIntegerSubKeys()) {
            if (sub.hasSubKeys()) {
                sub = sub.getSubKeys().iterator().next();
            }
            if (ifKey != null) {
                if (sub.name().equals("else")) {
                    elseKey = sub;
                }
                Behavior child = parseIfElse(ifKey, elseKey, context);
                if (child != null) {
                    children.add(child);
                }
                ifKey = null;
                elseKey = null;
                continue;
            }
            if (sub.name().startsWith("if ")) {
                ifKey = sub;
                continue;
            }
            Behavior child = parseNode(sub, context);
            if (child != null) {
                children.add(child);
            }
        }
        if (ifKey != null) {
            Behavior child = parseIfElse(ifKey, elseKey, context);
            if (child != null) {
                children.add(child);
            }
        }
        return children;
    }

    /**
     * Parses an eval block where each child line is evaluated as a raw expression. This allows multiple expressions to
     * be evaluated without needing expression wrappers.
     *
     * @param key
     *            the DataKey containing expressions
     * @param language
     *            the expression language (e.g., "molang"), or null for default
     * @param context
     *            the behavior context
     */
    private Behavior parseEvalBlock(DataKey key, String language, BehaviorRegistry.BehaviorContext context) {
        ExpressionRegistry exprRegistry = registry.getExpressionRegistry();
        List<CompiledExpression> expressions = Lists.newArrayList();

        String engineName = language != null ? language : exprRegistry.getDefaultEngineName();
        ExpressionEngine engine = exprRegistry.getEngine(engineName);
        if (engine == null) {
            Messaging.severe("Unknown expression language", engineName);
            return EMPTY;
        }
        for (DataKey sub : key.getIntegerSubKeys()) {
            String expr = sub.getString("");
            if (expr != null && !expr.isEmpty()) {
                try {
                    expressions.add(engine.compile(expr));
                } catch (ExpressionCompileException e) {
                    e.printStackTrace();
                }
            }
        }
        return (InstantBehavior) () -> {
            for (CompiledExpression expr : expressions) {
                expr.evaluate(context.getScope());
            }
            return BehaviorStatus.SUCCESS;
        };
    }

    private Behavior parseIfElse(DataKey ifKey, DataKey elseKey, BehaviorRegistry.BehaviorContext context) {
        String conditionStr = ifKey.name().substring(3).trim();
        Supplier<Boolean> condition = parseCondition(conditionStr, context);

        Behavior ifBehavior;
        if (ifKey.hasSubKeys()) {
            List<Behavior> children = Lists.newArrayList();
            for (DataKey sub : ifKey.getIntegerSubKeys()) {
                if (sub.hasSubKeys()) {
                    sub = sub.getSubKeys().iterator().next();
                }
                Behavior child = parseNode(sub, context);
                if (child != null) {
                    children.add(child);
                }
            }
            ifBehavior = children.size() == 1 ? children.get(0) : Sequence.createSequence(children);
        } else {
            String value = ifKey.getString("");
            ifBehavior = parseLeaf(value, null, context);
        }
        Behavior elseBehavior = null;
        if (elseKey != null) {
            if (elseKey.hasSubKeys()) {
                List<Behavior> children = Lists.newArrayList();
                for (DataKey sub : elseKey.getIntegerSubKeys()) {
                    if (sub.hasSubKeys()) {
                        sub = sub.getSubKeys().iterator().next();
                    }
                    Behavior child = parseNode(sub, context);
                    if (child != null) {
                        children.add(child);
                    }
                }
                elseBehavior = children.size() == 1 ? children.get(0) : Sequence.createSequence(children);
            } else {
                String value = elseKey.getString("");
                if (value != null && !value.isEmpty()) {
                    elseBehavior = parseLeaf(value, null, context);
                }
            }
        }
        return IfElse.create(condition, ifBehavior, elseBehavior);
    }

    /**
     * Parses inline arguments with support for quoted strings. Supports both single and double quotes. Examples: -
     * behavior arg1 arg2 arg3 - behavior "arg with spaces" key="value with spaces" - behavior key='single quoted'
     * other=normal
     *
     * @param input
     *            the input string to parse
     * @return array of parsed arguments
     */
    private String[] parseInlineArgs(String input) {
        List<String> args = Lists.newArrayList();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = 0;
        boolean escaped = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (escaped) {
                current.append(c);
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (!inQuotes && (c == '"' || c == '\'' || c == '`')) {
                inQuotes = true;
                quoteChar = c;
                if (c == '`') {
                    current.append(c);
                }
                continue;
            }
            if (inQuotes && c == quoteChar) {
                inQuotes = false;
                quoteChar = 0;
                if (c == '`') {
                    current.append(c);
                }
                continue;
            }
            if (!inQuotes && Character.isWhitespace(c)) {
                if (current.length() > 0) {
                    args.add(current.toString());
                    current = new StringBuilder();
                }
                continue;
            }
            current.append(c);
        }
        if (current.length() > 0) {
            args.add(current.toString());
        }
        return args.toArray(new String[0]);
    }

    private Behavior parseLeaf(String name, DataKey params, BehaviorRegistry.BehaviorContext context) {
        if (name == null || name.isEmpty())
            return EMPTY;

        if (name.startsWith("/"))
            return createCommandBehavior(name.substring(1), context);

        if (name.startsWith("`"))
            return createExpressionBehavior(name, context);

        String[] parts = parseInlineArgs(name);
        String behaviorName = parts[0];

        if (parts.length > 1) {
            String[] args = new String[parts.length - 1];
            System.arraycopy(parts, 1, args, 0, parts.length - 1);
            context.setArgs(args);
        } else {
            context.setArgs(null);
        }
        Behavior behavior = registry.createBehavior(behaviorName, params, context);
        if (behavior == null) {
            Messaging.severe("Unknown behavior", behaviorName);
            return EMPTY;
        }
        return behavior;
    }

    private Behavior parseNode(DataKey key, BehaviorRegistry.BehaviorContext context) {
        if (key.hasSubKeys()) {
            DataKey ifKey = null;
            DataKey elseKey = null;
            for (DataKey sub : key.getSubKeys()) {
                String name = sub.name();
                if (name.startsWith("if ")) {
                    ifKey = sub;
                } else if (name.equals("else")) {
                    elseKey = sub;
                }
            }
            if (ifKey != null)
                return parseIfElse(ifKey, elseKey, context);

            return parseContainer(key.name(), key, context);
        } else {
            String value = key.getString("");
            if (value != null && !value.isEmpty())
                return parseLeaf(value, null, context);
            return parseLeaf(key.name(), key, context);
        }
    }

    private static final Behavior EMPTY = new Behavior() {
        @Override
        public void reset() {
        }

        @Override
        public BehaviorStatus run() {
            return BehaviorStatus.SUCCESS;
        }

        @Override
        public boolean shouldExecute() {
            return true;
        }

        @Override
        public String toString() {
            return "EmptyBehavior";
        }
    };
}
