package net.citizensnpcs.trait;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.npc.CitizensNPC;
import net.minecraft.server.EntityPlayer;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

//TODO: reduce reliance on CitizensNPC
public class Controllable extends Trait implements Runnable, Listener, Toggleable {
    private final CitizensNPC npc;
    private boolean enabled;

    public Controllable(NPC npc) {
        this.npc = (CitizensNPC) npc;
    }

    private void jump() {
        if (!npc.getHandle().onGround)
            return;
        npc.getHandle().motY = JUMP_VELOCITY;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        enabled = key.getBoolean("enabled");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        EntityPlayer handle = ((CraftPlayer) event.getPlayer()).getHandle();
        Action performed = event.getAction();
        if (performed == Action.PHYSICAL || !handle.equals(npc.getHandle().passenger))
            return;
        if (performed == Action.LEFT_CLICK_AIR || performed == Action.LEFT_CLICK_BLOCK) {
            jump();
        } else if (-170F >= event.getPlayer().getLocation().getPitch()) {
            event.getPlayer().leaveVehicle();
        }
    }

    @EventHandler
    public void onRightClick(NPCRightClickEvent event) {
        if (!event.getNPC().equals(npc) || npc.getHandle().passenger != null)
            return;
        EntityPlayer handle = ((CraftPlayer) event.getClicker()).getHandle();
        handle.setPassengerOf(npc.getHandle());
    }

    @Override
    public void run() {
        if (!npc.isSpawned() || npc.getHandle().passenger == null)
            return;
        npc.getHandle().motX += npc.getHandle().passenger.motX;
        npc.getHandle().motZ += npc.getHandle().passenger.motZ;
    }

    @Override
    public void save(DataKey key) {
        key.setBoolean("enabled", enabled);
    }

    private static final double JUMP_VELOCITY = 0.6;

    @Override
    public boolean toggle() {
        return (enabled = !enabled);
    }
}
