package net.citizensnpcs.trait;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.attachment.Attachment;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.scripting.CompileCallback;
import net.citizensnpcs.api.scripting.ScriptFactory;
import net.citizensnpcs.api.util.DataKey;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Behaviour extends Attachment {
    private final Map<Goal, Integer> addedGoals = Maps.newHashMap();
    private final Function<String, File> fileConverterFunction = new Function<String, File>() {
        @Override
        public File apply(String arg0) {
            return new File(rootFolder, arg0);
        }
    };
    private final NPC npc;
    private final File rootFolder = new File(CitizensAPI.getScriptFolder(), "behaviours");
    private final List<File> scripts = Lists.newArrayList();
    {
        if (!rootFolder.exists())
            rootFolder.mkdirs();
    }

    public Behaviour(NPC npc) {
        this.npc = npc;
    }

    public void addScripts(Iterable<String> scripts) {
        BehaviourCallback callback = new BehaviourCallback(new Goals());
        Iterable<File> transformed = Iterables.transform(scripts, fileConverterFunction);
        CitizensAPI.getScriptCompiler().compile(transformed).withCallback(callback).begin();
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        reset();
        if (!key.keyExists("scripts"))
            return;
        String scripts = key.getString("scripts");
        addScripts(Splitter.on(",").split(scripts));
    }

    @Override
    public void onSpawn() {
        for (Entry<Goal, Integer> entry : addedGoals.entrySet()) {
            npc.getAI().addGoal(entry.getValue(), entry.getKey());
        }
    }

    @Override
    public void onRemove() {
        removeGoals();
    }

    private void removeGoals() {
        for (Goal entry : addedGoals.keySet()) {
            npc.getAI().removeGoal(entry);
        }
    }

    private void reset() {
        removeGoals();
        scripts.clear();
        addedGoals.clear();
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
        public void onCompileTaskFinished() {
            addedGoals.putAll(goals.goals);
            if (!npc.isSpawned())
                return;
            for (Entry<Goal, Integer> entry : goals.goals.entrySet()) {
                npc.getAI().addGoal(entry.getValue(), entry.getKey());
            }
        }

        @Override
        public void onScriptCompiled(ScriptFactory script) {
            script.newInstance().invoke("addGoals", goals, npc);
        }
    }

    public static class Goals {
        private final Map<Goal, Integer> goals = Maps.newHashMap();

        public void addGoal(int priority, Goal goal) {
            if (goal == null)
                throw new IllegalArgumentException("goal cannot be null");
            goals.put(goal, priority);
        }
    }
}
