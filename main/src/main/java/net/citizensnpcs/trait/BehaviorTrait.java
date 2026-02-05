package net.citizensnpcs.trait;

import java.io.File;
import java.util.Map;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.tree.Behavior;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.expr.ExpressionScope;
import net.citizensnpcs.api.expr.Memory;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.templates.TemplateWorkspace;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.trait.TraitTemplateParser;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.MemoryDataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.YamlStorage;
import net.citizensnpcs.npc.ai.tree.BehaviorTreeParser;
import net.citizensnpcs.npc.ai.tree.NPCExpressionScope;

/**
 * Trait that allows NPCs to have behavior trees.
 */
@TraitName("behavior")
public class BehaviorTrait extends Trait {
    private Memory memory;
    private Behavior root;
    private Map<String, Object> yamlCache;

    public BehaviorTrait() {
        super("behavior");
    }

    public boolean applyBehaviorsFromFile(File file) {
        YamlStorage storage = new YamlStorage(file);
        if (!storage.load())
            return false;

        applyBehaviorsFromKey(storage.getKey(""));
        return true;
    }

    public void applyBehaviorsFromKey(DataKey key) {
        if (root != null) {
            npc.getDefaultBehaviorController().removeBehavior(root);
            root = null;
        }
        DataKey load = new MemoryDataKey();
        Map<String, Object> values = key.getValuesDeep();
        load.setMap("tree", values);
        yamlCache = values;
        load(load);
    }

    /**
     * @return the blackboard memory for this NPC's behavior tree
     */
    public Memory getMemory() {
        return memory;
    }

    @Override
    public void load(DataKey key) {
        if (key.keyExists("tree")) {
            parse(key.getRelative("tree"));
        }
        if (key.keyExists("memory")) {
            if (memory == null) {
                memory = new Memory();
            }
            memory.loadFrom(key.getRelative("memory"));
        }
    }

    @Override
    public void onDespawn() {
        if (root != null) {
            npc.getDefaultBehaviorController().removeBehavior(root);
            root = null;
        }
    }

    private void parse(DataKey yaml) {
        if (!yaml.hasSubKeys())
            return;
        if (memory == null) {
            memory = new Memory();
        }
        ExpressionScope scope = NPCExpressionScope.createFor(npc);
        BehaviorTreeParser parser = new BehaviorTreeParser(CitizensAPI.getBehaviorRegistry());
        Behavior parsed = parser.parse(yaml.getSubKeys().iterator().next(), npc, scope, memory);
        if (parsed == null)
            return;
        npc.getDefaultBehaviorController().addBehavior(root = new RootBehavior(parsed, scope));
    }

    @Override
    public void save(DataKey key) {
        if (memory != null) {
            memory.saveTo(key.getRelative("memory"));
        }
        if (yamlCache != null) {
            key.setMap("tree", yamlCache);
            yamlCache = null;
        }
    }

    private static class RootBehavior implements Behavior {
        boolean failed;
        BehaviorStatus lastStatus;
        final Behavior root;
        final ExpressionScope scope;

        public RootBehavior(Behavior root, ExpressionScope scope) {
            this.root = root;
            this.scope = scope;
        }

        @Override
        public void reset() {
            root.reset();
            lastStatus = null;
        }

        @Override
        public BehaviorStatus run() {
            scope.resetCache();
            try {
                lastStatus = root.run();
            } catch (Throwable t) {
                if (!failed) {
                    Messaging.severe("Error while running behavior tree:");
                    t.printStackTrace();
                    failed = true;
                }
                lastStatus = BehaviorStatus.FAILURE;
            }
            if (lastStatus == BehaviorStatus.SUCCESS || lastStatus == BehaviorStatus.FAILURE) {
                root.reset();
            }
            return lastStatus;
        }

        @Override
        public boolean shouldExecute() {
            return !failed && root != null && root.shouldExecute();
        }
    }

    /**
     * Creates a template parser for BehaviorTrait that supports:
     * <ul>
     * <li>Short form: "tree.yml" - loads tree from file</li>
     * <li>Long form: tree as child</li>
     * </ul>
     */
    public static TraitTemplateParser createTemplateParser() {
        return new TraitTemplateParser() {
            @Override
            public ShortTemplateParser getShortTemplateParser() {
                return (ctx, cmdCtx) -> {
                    if (cmdCtx.argsLength() < 2)
                        return null;

                    String fileName = cmdCtx.getJoinedStrings(1);
                    return loadFromFile(ctx.npc, ctx.workspace, fileName);
                };
            }

            @Override
            public TemplateParser getTemplateParser() {
                return (ctx, key) -> {
                    BehaviorTrait trait = ctx.npc.getOrAddTrait(BehaviorTrait.class);
                    trait.applyBehaviorsFromKey(key.getRelative("tree"));
                    return null;
                };
            }

            private BehaviorTrait loadFromFile(NPC npc, TemplateWorkspace workspace, String fileName) {
                File file = workspace != null ? workspace.getFile(fileName) : null;
                if (file == null) {
                    file = new File(CitizensAPI.getDataFolder(), "behaviors/" + fileName);
                }
                if (!file.exists()) {
                    Messaging.severe("Behavior file not found", fileName);
                    return null;
                }
                BehaviorTrait trait = npc.getOrAddTrait(BehaviorTrait.class);
                if (!trait.applyBehaviorsFromFile(file)) {
                    Messaging.severe("Failed to load behavior tree from", fileName);
                }
                return null;
            }
        };
    }
}
