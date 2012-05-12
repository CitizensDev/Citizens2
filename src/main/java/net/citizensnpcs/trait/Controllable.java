package net.citizensnpcs.trait;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.npc.CitizensNPC;
import net.minecraft.server.EntityLiving;
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
        boolean allowed = npc.getHandle().onGround;
        if (!allowed)
            return;
        npc.getHandle().motY = JUMP_VELOCITY;
        // TODO: make jumping work in liquid or make liquids float the npc
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        enabled = key.getBoolean("");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!npc.isSpawned() || !enabled)
            return;
        EntityPlayer handle = ((CraftPlayer) event.getPlayer()).getHandle();
        Action performed = event.getAction();
        if (performed == Action.PHYSICAL || !handle.equals(npc.getHandle().passenger))
            return;
        if (performed == Action.LEFT_CLICK_AIR || performed == Action.LEFT_CLICK_BLOCK) {
            jump();
        }
    }

    @EventHandler
    public void onRightClick(NPCRightClickEvent event) {
        if (!npc.isSpawned() || !event.getNPC().equals(npc))
            return;
        EntityPlayer handle = ((CraftPlayer) event.getClicker()).getHandle();
        if (npc.getHandle().passenger != null) {
            if (npc.getHandle().passenger == handle) {
                event.getClicker().leaveVehicle();
            }
            return;
        }
        handle.setPassengerOf(npc.getHandle());
    }

    @Override
    public void run() {
        if (!npc.isSpawned() || npc.getHandle().passenger == null)
            return;
        EntityLiving handle = npc.getHandle();
        boolean onGround = handle.onGround;
        handle.motX += handle.passenger.motX * (onGround ? GROUND_SPEED : AIR_SPEED);
        handle.motZ += handle.passenger.motZ * (onGround ? GROUND_SPEED : AIR_SPEED);
    }

    @Override
    public void save(DataKey key) {
        key.setBoolean("enabled", enabled);
    }

    private static final double GROUND_SPEED = 4;
    private static final double AIR_SPEED = 1.5;
    private static final double JUMP_VELOCITY = 0.6;

    @Override
    public boolean toggle() {
        return (enabled = !enabled);
    }
}
