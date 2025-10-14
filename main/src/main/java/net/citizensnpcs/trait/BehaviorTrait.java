package net.citizensnpcs.trait;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.Lists;

import net.citizensnpcs.api.ai.tree.Behavior;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.ai.tree.IfElse;
import net.citizensnpcs.api.ai.tree.Loop;
import net.citizensnpcs.api.ai.tree.Selector;
import net.citizensnpcs.api.ai.tree.Sequence;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.trait.TraitTemplateParser;
import net.citizensnpcs.api.util.DataKey;

@TraitName("behavior")
public class BehaviorTrait extends Trait {
    // trigger -> execution -> exit.
    // needs some standard exit and conditionals (memory)
    // needs a registry
    private final Behavior root = new Behavior() {
        @Override
        public void reset() {
        }

        @Override
        public BehaviorStatus run() {
            return null;
        }

        @Override
        public boolean shouldExecute() {
            return false;
        }
    };

    public BehaviorTrait() {
        super("behavior");
    }

    @Override
    public void onDespawn() {
        npc.getDefaultGoalController().removeBehavior(root);
    }

    @Override
    public void onSpawn() {
        npc.getDefaultGoalController().addBehavior(root, 1);
    }

    public static TraitTemplateParser createTemplateParser() {
        return TraitTemplateParser.createDefault(BehaviorTrait.class);
    }

    private static Supplier<Boolean> parseCondition(String replaceFirst) {
        return null;
    }

    private static Behavior parseContainer(String name, DataKey key) {
        if (name.equalsIgnoreCase("sequence")) {
            List<Behavior> behaviors = Lists.newArrayList();
            for (DataKey sub : key.getSubKeys()) {
                behaviors.add(recursiveParse(sub));
            }
            return Sequence.createSequence(behaviors);
        } else if (name.equalsIgnoreCase("random")) {
            List<Behavior> behaviors = Lists.newArrayList();
            for (DataKey sub : key.getSubKeys()) {
                behaviors.add(recursiveParse(sub));
            }
            return Selector.selecting(behaviors).build();
        } else if (name.startsWith("loop ")) {
            Supplier<Boolean> condition = parseCondition(name.replaceFirst("loop ", ""));
            Behavior wrapping = parseContainer("sequence", key);
            return Loop.createWithCondition(wrapping, condition);
        }
        return null;
    }

    private static Behavior parseIfElse(DataKey ifKey, DataKey elseKey) {
        return IfElse.create(parseCondition(ifKey.name().replaceFirst("if ", "")), recursiveParse(ifKey),
                elseKey == null ? null : recursiveParse(elseKey));
    }

    private static Behavior parseLeaf(String name) {
        return null;
    }

    private static Behavior recursiveParse(DataKey key) {
        if (key.getSubKeys().iterator().hasNext()) {
            DataKey ifKey = null;
            DataKey elseKey = key.getRelative("else");
            for (DataKey sub : key.getSubKeys()) {
                if (sub.name().startsWith("if ")) {
                    ifKey = sub;
                }
            }
            if (ifKey != null) {
                return parseIfElse(ifKey, elseKey);
            }
            return parseContainer(key.name(), key);
        } else {
            return parseLeaf(key.getString(""));
        }
    }
}
