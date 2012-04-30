package net.citizensnpcs.trait;

import java.io.File;
import java.util.List;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.scripting.CompileCallback;
import net.citizensnpcs.api.scripting.ScriptFactory;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.npc.ai.CitizensAI.GoalEntry;

import org.apache.commons.lang.Validate;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class Behaviour extends Trait {
    private final List<File> scripts = Lists.newArrayList();
    private final List<GoalEntry> addedGoals = Lists.newArrayList();
    private final File rootFolder = new File(CitizensAPI.getScriptFolder(), "behaviours");
    private final NPC npc;

    public Behaviour(NPC npc) {
        this.npc = npc;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        reset();
        if (!key.keyExists("scripts"))
            return;
        String scripts = key.getString("scripts");
        addScripts(Splitter.on(",").split(scripts));
    }

    private void reset() {
        removeGoals();
        scripts.clear();
        addedGoals.clear();
    }

    private void removeGoals() {
        for (GoalEntry entry : addedGoals) {
            npc.getAI().removeGoal(entry.getGoal());
        }
    }

    @Override
    public void onNPCSpawn() {
        for (GoalEntry entry : addedGoals) {
            npc.getAI().addGoal(entry.getPriority(), entry.getGoal());
        }
    }

    @Override
    public void onRemove() {
        removeGoals();
    }

    public void addScripts(Iterable<String> scripts) {
        BehaviourCallback callback = new BehaviourCallback(new Goals());
        for (String script : scripts) {
            File file = new File(rootFolder, script);
            if (!file.exists())
                continue;
            CitizensAPI.getScriptCompiler().compile(file).withCallback(callback).begin();
            this.scripts.add(file);
        }
        List<GoalEntry> added = callback.goals.goals;
        for (GoalEntry entry : added) {
            npc.getAI().addGoal(entry.getPriority(), entry.getGoal());
        }
        addedGoals.addAll(added);
    }

    @Override
    public void save(DataKey key) {
        key.setString("scripts", Joiner.on(",").join(scripts));
    }

    private class BehaviourCallback implements CompileCallback {
        private final Goals goals;

        private BehaviourCallback(Goals goals) {
            this.goals = goals;
        }

        @Override
        public void onScriptCompiled(ScriptFactory script) {
            script.newInstance().invoke("addGoals", goals, npc);
        }
    }

    public static class Goals {
        private final List<GoalEntry> goals = Lists.newArrayList();

        public void addGoal(int priority, Goal goal) {
            Validate.notNull(goal);
            this.goals.add(new GoalEntry(priority, goal));
        }
    }
}
