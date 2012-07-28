package net.citizensnpcs.trait;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntityPlayer;

import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

//TODO: reduce reliance on CitizensNPC
public class Controllable extends Trait implements Toggleable {
    private boolean enabled;

    public Controllable() {
        super("controllable");
    }

    private EntityLiving getHandle() {
        return ((CraftLivingEntity) npc.getBukkitEntity()).getHandle();
    }

    private void jump() {
        boolean allowed = getHandle().onGround;
        if (!allowed)
            return;
        getHandle().motY = JUMP_VELOCITY;
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
        if (performed == Action.PHYSICAL || !handle.equals(getHandle().passenger))
            return;
        if (performed == Action.LEFT_CLICK_AIR || performed == Action.LEFT_CLICK_BLOCK) {
            jump();
        }
    }

    @EventHandler
    public void onRightClick(NPCRightClickEvent event) {
        if (!enabled || !npc.isSpawned() || !event.getNPC().equals(npc))
            return;
        EntityPlayer handle = ((CraftPlayer) event.getClicker()).getHandle();
        if (getHandle().passenger != null) {
            if (getHandle().passenger == handle)
                event.getClicker().leaveVehicle();
            return;
        }
        handle.setPassengerOf(getHandle());
    }

    @Override
    public void run() {
        if (!enabled || !npc.isSpawned() || getHandle().passenger == null)
            return;
        EntityLiving handle = getHandle();
        boolean onGround = handle.onGround;
        handle.motX += handle.passenger.motX * (onGround ? GROUND_SPEED : AIR_SPEED);
        handle.motZ += handle.passenger.motZ * (onGround ? GROUND_SPEED : AIR_SPEED);
    }

    @Override
    public void save(DataKey key) {
        key.setBoolean("enabled", enabled);
    }

    @Override
    public boolean toggle() {
        enabled = !enabled;
        if (!enabled && getHandle().passenger != null)
            getHandle().passenger.getBukkitEntity().leaveVehicle();
        return enabled;
    }

    private static final double AIR_SPEED = 1.5;
    private static final double GROUND_SPEED = 4;

    private static final double JUMP_VELOCITY = 0.6;
}
