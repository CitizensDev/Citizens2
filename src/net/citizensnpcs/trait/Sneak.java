package net.citizensnpcs.trait;

import net.citizensnpcs.api.DataKey;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.trait.SaveId;
import net.citizensnpcs.api.npc.trait.Trait;
import net.citizensnpcs.npc.entity.CitizensHumanNPC;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.Packet40EntityMetadata;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

@SaveId("sneak")
public class Sneak extends Trait implements Runnable {
    private final NPC npc;
    private boolean sneak;

    public Sneak(NPC npc) {
        this.npc = npc;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        sneak = key.getBoolean("");
    }

    @Override
    public void save(DataKey key) {
        key.setBoolean("", sneak);
    }

    @Override
    public void run() {
        if (npc instanceof CitizensHumanNPC) {
            EntityPlayer handle = ((CitizensHumanNPC) npc).getHandle();
            handle.setSneak(sneak);
            for (Player player : Bukkit.getOnlinePlayers())
                ((CraftPlayer) player).getHandle().netServerHandler.sendPacket(new Packet40EntityMetadata(handle.id,
                        handle.getDataWatcher()));
        }
    }

    public void setSneaking(boolean sneak) {
        this.sneak = sneak;
    }

    public boolean isSneaking() {
        return sneak;
    }

    public void toggle() {
        sneak = !sneak;
    }

    @Override
    public String toString() {
        return "Sneak{" + sneak + "}";
    }
}