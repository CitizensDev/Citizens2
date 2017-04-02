package net.citizensnpcs.trait;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Zombie;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;

@TraitName("age")
public class Age extends Trait implements Toggleable {
    @Persist
    private int age = 0;
    private Ageable ageable;
    @Persist
    private boolean locked = true;

    public Age() {
        super("age");
    }

    public void describe(CommandSender sender) {
        Messaging.sendTr(sender, Messages.AGE_TRAIT_DESCRIPTION, npc.getName(), age, locked);
    }

    private boolean isAgeable() {
        return ageable != null;
    }

    @Override
    public void onSpawn() {
        if (npc.getEntity() instanceof Ageable) {
            Ageable entity = (Ageable) npc.getEntity();
            entity.setAge(age);
            entity.setAgeLock(locked);
            ageable = entity;
        } else if (npc.getEntity() instanceof Zombie) {
            ((Zombie) npc.getEntity()).setBaby(age < 0);
            ageable = null;
        } else {
            ageable = null;
        }
    }

    @Override
    public void run() {
        if (!locked && isAgeable()) {
            age = ageable.getAge();
        }
    }

    public void setAge(int age) {
        this.age = age;
        if (isAgeable()) {
            ageable.setAge(age);
        } else if (npc.getEntity() instanceof Zombie) {
            ((Zombie) npc.getEntity()).setBaby(age < 0);
        }
    }

    @Override
    public boolean toggle() {
        locked = !locked;
        if (isAgeable()) {
            ageable.setAgeLock(locked);
        }
        return locked;
    }

    @Override
    public String toString() {
        return "Age{age=" + age + ",locked=" + locked + "}";
    }
}