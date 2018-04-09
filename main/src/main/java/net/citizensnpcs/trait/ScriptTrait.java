package net.citizensnpcs.trait;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.scripting.CompileCallback;
import net.citizensnpcs.api.scripting.Script;
import net.citizensnpcs.api.scripting.ScriptFactory;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;

@TraitName("scripttrait")
public class ScriptTrait extends Trait {
    @Persist
    public List<String> files = new ArrayList<String>();
    private final List<RunnableScript> runnableScripts = new ArrayList<RunnableScript>();

    public ScriptTrait() {
        super("scripttrait");
    }

    public void addScripts(List<String> scripts) {
        for (String f : scripts) {
            if (!files.contains(f) && validateFile(f)) {
                loadScript(f);
                files.add(f);
            }
        }
    }

    public List<String> getScripts() {
        return files;
    }

    @Override
    public void load(DataKey key) {
        for (String file : files) {
            if (validateFile(file)) {
                loadScript(file);
            }
        }
    }

    public void loadScript(final String file) {
        File f = new File(JavaPlugin.getPlugin(Citizens.class).getScriptFolder(), file);
        CitizensAPI.getScriptCompiler().compile(f).cache(true).withCallback(new CompileCallback() {
            @Override
            public void onScriptCompiled(String sourceDescriptor, ScriptFactory compiled) {
                final Script newInstance = compiled.newInstance();
                Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
                    @Override
                    public void run() {
                        try {
                            newInstance.invoke("onLoad", npc);
                        } catch (RuntimeException e) {
                            if (!(e.getCause() instanceof NoSuchMethodException)) {
                                throw e;
                            }
                        }
                        runnableScripts.add(new RunnableScript(newInstance, file));
                    }
                });
            }
        }).beginWithFuture();
    }

    public void removeScripts(List<String> scripts) {
        files.removeAll(scripts);
        Iterator<RunnableScript> itr = runnableScripts.iterator();
        while (itr.hasNext()) {
            if (scripts.remove(itr.next().file)) {
                itr.remove();
            }
        }
    }

    @Override
    public void run() {
        Iterator<RunnableScript> itr = runnableScripts.iterator();
        while (itr.hasNext()) {
            try {
                itr.next().script.invoke("run", npc);
            } catch (RuntimeException e) {
                if (e.getCause() instanceof NoSuchMethodException) {
                    itr.remove();
                } else {
                    throw e;
                }
            }
        }
    }

    public boolean validateFile(String file) {
        File f = new File(JavaPlugin.getPlugin(Citizens.class).getScriptFolder(), file);
        if (!f.exists() || !f.getParentFile().equals(JavaPlugin.getPlugin(Citizens.class).getScriptFolder())) {
            return false;
        }
        return CitizensAPI.getScriptCompiler().canCompile(f);
    }

    private static class RunnableScript {
        String file;
        Script script;

        public RunnableScript(Script script, String file) {
            this.script = script;
            this.file = file;
        }
    }
}
