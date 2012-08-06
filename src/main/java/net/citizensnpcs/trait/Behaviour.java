package net.citizensnpcs.trait;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.ai.GoalController.GoalEntry;
import net.citizensnpcs.api.ai.SimpleGoalEntry;
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

public class Behaviour extends Trait {
    private final List<BehaviourGoalEntry> addedGoals = Lists.newArrayList();
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
        for (GoalEntry entry : addedGoals) {
            npc.getDefaultGoalController().addGoal(entry.getGoal(), entry.getPriority());
        }
    }

    private void removeGoals() {
        for (GoalEntry entry : addedGoals) {
            npc.getDefaultGoalController().removeGoal(entry.getGoal());
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

    private static class BehaviourGoalEntry extends SimpleGoalEntry {
        private final File file;

        private BehaviourGoalEntry(Goal goal, int priority, File file) {
            super(goal, priority);
            this.file = file;
        }
    }

    public class BehaviourCallback implements CompileCallback {
        private final List<BehaviourGoalEntry> goals = Lists.newArrayList();
        private File fileInUse;

        public void addGoal(int priority, Goal goal) {
            Validate.notNull(goal);
            goals.add(new BehaviourGoalEntry(goal, priority, fileInUse));
        }

        @Override
        public void onCompileTaskFinished() {
            addedGoals.addAll(goals);
            if (!npc.isSpawned())
                return;
            for (GoalEntry entry : goals) {
                npc.getDefaultGoalController().addGoal(entry.getGoal(), entry.getPriority());
            }
        }

        @Override
        public void onScriptCompiled(File file, ScriptFactory script) {
            synchronized (goals) {
                fileInUse = file;
                script.newInstance().invoke("addGoals", this, npc);
                scripts.add(file);
                fileInUse = null;
            }
        }
    }

    public void removeScripts(Iterable<String> files) {
        Iterable<File> transformed = Iterables.transform(files, fileConverterFunction);
        boolean isSpawned = npc.isSpawned();
        for (File file : transformed) {
            if (isSpawned) {
                Iterator<BehaviourGoalEntry> itr = addedGoals.iterator();
                while (itr.hasNext()) {
                    BehaviourGoalEntry entry = itr.next();
                    if (file.equals(entry.file)) {
                        itr.remove();
                        npc.getDefaultGoalController().removeGoal(entry.getGoal());
                    }
                }
            }
            scripts.remove(file);
        }
    }
}
