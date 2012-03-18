package net.citizensnpcs.trait;

import org.bukkit.entity.Animals;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

public class Age extends Trait implements Runnable, Toggleable {
    private int age = 0;
    private boolean locked = true;
    private final NPC npc;

    public Age(NPC npc) {
        this.npc = npc;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        age = key.getInt("age");
        locked = key.getBoolean("locked");
    }

    @Override
    public void save(DataKey key) {
        key.setInt("age", age);
        key.setBoolean("locked", locked);
    }

    @Override
    public void onNPCSpawn() {
        // TODO: Switch to use Ageable when that is implemented
        if (npc.getBukkitEntity() instanceof Animals) {
            Animals animal = (Animals) npc.getBukkitEntity();
            animal.setAge(age);
            animal.setAgeLock(locked);
        }
    }

    @Override
    public void run() {
        if (!locked)
            age = ((Animals) npc.getBukkitEntity()).getAge();
    }

    @Override
    public boolean toggle() {
        locked = !locked;
        ((Animals) npc.getBukkitEntity()).setAgeLock(locked);
        return locked;
    }

    public void setAge(int age) {
        this.age = age;
        if (npc.getBukkitEntity() instanceof Animals)
            ((Animals) npc.getBukkitEntity()).setAge(age);
    }

    @Override
    public String toString() {
        return "Age{age=" + age + ",locked=" + locked + "}";
    }
}