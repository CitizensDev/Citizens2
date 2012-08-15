package net.citizensnpcs.trait;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Ageable;

public class Age extends Trait implements Toggleable {
    private int age = 0;
    private boolean ageable = false;
    private boolean locked = true;

    public Age() {
        super("age");
    }

    public void describe(CommandSender sender) {
        Messaging.sendF(sender, "%s's age is %s and %s locked.", StringHelper.wrap(npc.getName()),
                StringHelper.wrap(age), StringHelper.wrap(locked ? "is" : "isn't"));
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        if (npc.isSpawned() && !(npc.getBukkitEntity() instanceof Ageable))
            throw new NPCLoadException("NPC must be ageable");
        age = key.getInt("age");
        locked = key.getBoolean("locked");
    }

    @Override
    public void onSpawn() {
        if (npc instanceof Ageable) {
            Ageable entity = (Ageable) npc.getBukkitEntity();
            entity.setAge(age);
            entity.setAgeLock(locked);
            ageable = true;
        } else
            ageable = false;
    }

    @Override
    public void run() {
        if (!locked && ageable)
            age = ((Ageable) npc.getBukkitEntity()).getAge();
    }

    @Override
    public void save(DataKey key) {
        key.setInt("age", age);
        key.setBoolean("locked", locked);
    }

    public void setAge(int age) {
        this.age = age;
        if (ageable)
            ((Ageable) npc.getBukkitEntity()).setAge(age);
    }

    @Override
    public boolean toggle() {
        locked = !locked;
        if (ageable)
            ((Ageable) npc.getBukkitEntity()).setAgeLock(locked);
        return locked;
    }

    @Override
    public String toString() {
        return "Age{age=" + age + ",locked=" + locked + "}";
    }
}