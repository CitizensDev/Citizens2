package net.citizensnpcs.trait;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.npc.CitizensNPC;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class Controllable extends Trait implements Runnable, Listener {
    private final CitizensNPC npc;
    private boolean mounted;

    public Controllable(CitizensNPC npc) {
        this.npc = npc;
    }

    @EventHandler
    public void onRightClick(NPCRightClickEvent event) {
        if (!event.getNPC().equals(npc) || npc.getBukkitEntity().getPassenger() != null)
            return;
        ((CraftPlayer) event.getClicker()).getHandle().setPassengerOf(npc.getHandle());
        mounted = true;
    }

    @Override
    public void run() {
        if (!mounted)
            return;
        npc.getHandle().motX += npc.getHandle().passenger.motX;
        npc.getHandle().motZ += npc.getHandle().passenger.motZ;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
    }

    @Override
    public void save(DataKey key) {
    }
}
