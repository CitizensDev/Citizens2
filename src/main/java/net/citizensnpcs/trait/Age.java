package net.citizensnpcs.trait;

import net.citizensnpcs.api.abstraction.entity.Ageable;
import net.citizensnpcs.api.attachment.Attachment;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;

public class Age extends Attachment implements Runnable, Toggleable {
    private int age = 0;
    private boolean ageable = false;
    private final NPC npc;

    public Age(NPC npc) {
        this.npc = npc;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        if (npc.isSpawned() && !(npc.getEntity() instanceof Ageable))
            throw new NPCLoadException("NPC must be ageable");
        age = key.getInt("age");
    }

    @Override
    public void onSpawn() {
        if (npc.getEntity() instanceof Ageable) {
            Ageable entity = (Ageable) npc.getEntity();
            entity.setAge(age);
            ageable = true;
        } else
            ageable = false;
    }

    @Override
    public void run() {
        if (ageable)
            age = ((Ageable) npc.getEntity()).getAge();
    }

    @Override
    public void save(DataKey key) {
        key.setInt("age", age);
    }

    public void setAge(int age) {
        this.age = age;
        if (ageable)
            ((Ageable) npc.getEntity()).setAge(age);
    }

    @Override
    public boolean toggle() {
        ageable = !ageable;
        return ageable;
    }

    @Override
    public String toString() {
        return "Age{age=" + age + "}";
    }
}