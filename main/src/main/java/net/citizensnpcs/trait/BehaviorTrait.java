package net.citizensnpcs.trait;

import net.citizensnpcs.api.ai.tree.Behavior;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.trait.TraitTemplateParser;

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
}
