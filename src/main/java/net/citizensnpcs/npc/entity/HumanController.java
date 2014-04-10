package net.citizensnpcs.npc.entity;

import java.util.UUID;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Colorizer;
import net.citizensnpcs.npc.AbstractEntityController;
import net.citizensnpcs.trait.PlayerSkin;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_7_R2.PlayerInteractManager;
import net.minecraft.server.v1_7_R2.WorldServer;
import net.minecraft.util.com.mojang.authlib.GameProfile;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R2.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class HumanController extends AbstractEntityController {
    public HumanController() {
        super();
    }

    @Override
    protected Entity createEntity(final Location at, final NPC npc) {
        WorldServer ws = ((CraftWorld) at.getWorld()).getHandle();
        String parseColors = Colorizer.parseColors(npc.getTrait(PlayerSkin.class).getSkinName());
        if (parseColors.length() > 16) {
            parseColors = parseColors.substring(0, 16);
        }
        final EntityHumanNPC handle = new EntityHumanNPC(ws.getServer().getServer(), ws, new GameProfile(UUID
                .nameUUIDFromBytes(parseColors.getBytes()).toString(), parseColors), new PlayerInteractManager(ws), npc);
        handle.setPositionRotation(at.getX(), at.getY(), at.getZ(), at.getYaw(), at.getPitch());
        Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
            @Override
            public void run() {
                boolean removeFromPlayerList = Setting.REMOVE_PLAYERS_FROM_PLAYER_LIST.asBoolean();
                NMS.addOrRemoveFromPlayerList(getBukkitEntity(),
                        npc.data().get("removefromplayerlist", removeFromPlayerList));
            }
        }, 1);
        handle.getBukkitEntity().setSleepingIgnored(true);
        return handle.getBukkitEntity();
    }

    @Override
    public Player getBukkitEntity() {
        return (Player) super.getBukkitEntity();
    }
}