package net.citizensnpcs.trait;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.scripting.CompileCallback;
import net.citizensnpcs.api.scripting.ScriptFactory;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

import org.apache.commons.lang.Validate;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Behaviour extends Trait {
    private final Map<Goal, Integer> addedGoals = Maps.newHashMap();
    private final Function<String, File> fileConverterFunction = new Function<String, File>() {
        @Override
        public File apply(String arg0) {
            return new File(rootFolder, arg0);
        }
    };
    private final File rootFolder = new File(CitizensAPI.getScriptFolder(), "behaviours");
    private final List<File> scripts = Lists.newArrayList();
    {
        if (!rootFolder.exists())
            rootFolder.mkdirs();
    }

    public Behaviour() {
        super("behaviour");
    }

    public void addScripts(Iterable<String> scripts) {
        BehaviourCallback callback = new BehaviourCallback();
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
    public void onRemove() {
        removeGoals();
    }

    @Override
    public void onSpawn() {
        for (Entry<Goal, Integer> entry : addedGoals.entrySet()) {
            npc.getDefaultGoalController().addGoal(entry.getKey(), entry.getValue());
        }
    }

    private void removeGoals() {
        for (Goal entry : addedGoals.keySet()) {
            npc.getDefaultGoalController().removeGoal(entry);
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

    public class BehaviourCallback implements CompileCallback {
        private final Map<Goal, Integer> goals = Maps.newHashMap();

        public void addGoal(int priority, Goal goal) {
            Validate.notNull(goal);
            goals.put(goal, priority);
        }

        @Override
        public void onCompileTaskFinished() {
            addedGoals.putAll(goals);
            if (!npc.isSpawned())
                return;
            for (Entry<Goal, Integer> entry : goals.entrySet()) {
                npc.getDefaultGoalController().addGoal(entry.getKey(), entry.getValue());
            }
        }

        @Override
        public void onScriptCompiled(ScriptFactory script) {
            script.newInstance().invoke("addGoals", this, npc);
        }
    }
}
