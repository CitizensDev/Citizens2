package net.citizensnpcs.trait;

import java.io.File;
import java.util.Map;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.tree.Behavior;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.ai.tree.expr.ExpressionScope;
import net.citizensnpcs.api.ai.tree.expr.Memory;
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
import net.citizensnpcs.npc.ai.tree.NPCExpressionContext;

/**
 * Trait that allows NPCs to have behavior trees.
 */
@TraitName("behavior")
public class BehaviorTrait extends Trait {
    private Memory memory;
    private Behavior root;
    private ExpressionScope scope;
    private Map<String, Object> yamlCache;

    public BehaviorTrait() {
        super("behavior");
    }

    /**
     * @return the blackboard memory for this NPC's behavior tree
     */
    public Memory getMemory() {
        return memory;
    }

    /**
     * @return the expression scope for this NPC's behavior tree
     */
    public ExpressionScope getScope() {
        return scope;
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
            npc.getDefaultGoalController().removeBehavior(root);
            root = null;
        }
    }

    private void parse(DataKey yaml) {
        scope = NPCExpressionContext.createFor(npc);
        if (memory == null) {
            memory = new Memory();
        }
        BehaviorTreeParser parser = new BehaviorTreeParser(CitizensAPI.getBehaviorRegistry());
        root = parser.parse(yaml, npc, scope, memory);

        if (root == null)
            return;

        npc.getDefaultGoalController().addBehavior(new Behavior() {
            private BehaviorStatus lastStatus = null;

            @Override
            public void reset() {
                root.reset();
                lastStatus = null;
            }

            @Override
            public BehaviorStatus run() {
                scope.resetCache();
                lastStatus = root.run();

                if (lastStatus == BehaviorStatus.SUCCESS || lastStatus == BehaviorStatus.FAILURE) {
                    root.reset();
                }
                return lastStatus;
            }

            @Override
            public boolean shouldExecute() {
                return root.shouldExecute();
            }
        }, 1);
    }

    @Override
    public void save(DataKey key) {
        if (memory != null) {
            memory.saveTo(key.getRelative("memory"));
        }
        if (yamlCache != null) {
            key.setRaw("tree", yamlCache);
            yamlCache = null;
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
                    DataKey memory = new MemoryDataKey();
                    Map<String, Object> values = key.getRelative("tree").getValuesDeep();
                    memory.setRaw("tree", values);
                    trait.yamlCache = values;
                    trait.load(memory);
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
                YamlStorage storage = new YamlStorage(file);
                if (!storage.load()) {
                    Messaging.severe("Failed to load behavior tree from", fileName);
                    return null;
                }
                BehaviorTrait trait = npc.getOrAddTrait(BehaviorTrait.class);
                DataKey memory = new MemoryDataKey();
                Map<String, Object> values = storage.getKey("").getValuesDeep();
                memory.setRaw("tree", values);
                trait.yamlCache = values;
                trait.load(memory);

                return null;
            }
        };
    }
}
