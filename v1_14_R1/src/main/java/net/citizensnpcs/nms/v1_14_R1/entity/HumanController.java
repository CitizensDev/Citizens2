package net.citizensnpcs.nms.v1_14_R1.entity;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.mojang.authlib.GameProfile;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.AbstractEntityController;
import net.citizensnpcs.npc.skin.Skin;
import net.citizensnpcs.trait.ScoreboardTrait;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_14_R1.PlayerInteractManager;
import net.minecraft.server.v1_14_R1.WorldServer;

public class HumanController extends AbstractEntityController {
    public HumanController() {
    }

    @Override
    protected Entity createEntity(final Location at, final NPC npc) {
        final WorldServer nmsWorld = ((CraftWorld) at.getWorld()).getHandle();
        String coloredName = npc.getFullName();
        String name = coloredName.length() > 16 ? coloredName.substring(0, 16) : coloredName;
        UUID uuid = npc.getMinecraftUniqueId();
        String teamName = Util.getTeamName(uuid);
        if (npc.requiresNameHologram()) {
            name = teamName;
        }
        if (Setting.USE_SCOREBOARD_TEAMS.asBoolean()) {
            npc.getOrAddTrait(ScoreboardTrait.class).createTeam(name);
        }
        final GameProfile profile = new GameProfile(uuid, name);
        final EntityHumanNPC handle = new EntityHumanNPC(nmsWorld.getServer().getServer(), nmsWorld, profile,
                new PlayerInteractManager(nmsWorld), npc);
        Skin skin = handle.getSkinTracker().getSkin();
        if (skin != null) {
            skin.apply(handle);
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), () -> {
            if (getBukkitEntity() == null || !getBukkitEntity().isValid()
                    || getBukkitEntity() != handle.getBukkitEntity())
                return;
            boolean removeFromPlayerList = npc.data().get("removefromplayerlist",
                    Setting.REMOVE_PLAYERS_FROM_PLAYER_LIST.asBoolean());
            NMS.addOrRemoveFromPlayerList(getBukkitEntity(), removeFromPlayerList);
        }, 20);
        handle.getBukkitEntity().setSleepingIgnored(true);
        return handle.getBukkitEntity();
    }

    @Override
    public Player getBukkitEntity() {
        return (Player) super.getBukkitEntity();
    }
}
