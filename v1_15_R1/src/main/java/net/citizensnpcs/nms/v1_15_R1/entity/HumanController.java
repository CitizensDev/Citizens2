package net.citizensnpcs.nms.v1_15_R1.entity;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.mojang.authlib.GameProfile;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Colorizer;
import net.citizensnpcs.npc.AbstractEntityController;
import net.citizensnpcs.npc.skin.Skin;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_15_R1.PlayerInteractManager;
import net.minecraft.server.v1_15_R1.WorldServer;

public class HumanController extends AbstractEntityController {
    public HumanController() {
        super();
    }

    @Override
    protected Entity createEntity(final Location at, final NPC npc) {
        final WorldServer nmsWorld = ((CraftWorld) at.getWorld()).getHandle();
        String coloredName = Colorizer.parseColors(npc.getFullName());

        String[] nameSplit = Util.splitPlayerName(coloredName);
        String name = nameSplit[0];

        final String prefixCapture = nameSplit[1], suffixCapture = nameSplit[2];

        UUID uuid = npc.getUniqueId();
        if (uuid.version() == 4) { // clear version
            long msb = uuid.getMostSignificantBits();
            msb &= ~0x0000000000004000L;
            msb |= 0x0000000000002000L;
            uuid = new UUID(msb, uuid.getLeastSignificantBits());
        }

        final GameProfile profile = new GameProfile(uuid, name);

        final EntityHumanNPC handle = new EntityHumanNPC(nmsWorld.getServer().getServer(), nmsWorld, profile,
                new PlayerInteractManager(nmsWorld), npc);

        Skin skin = handle.getSkinTracker().getSkin();
        if (skin != null) {
            skin.apply(handle);
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (getBukkitEntity() == null || !getBukkitEntity().isValid())
                    return;
                boolean removeFromPlayerList = npc.data().get("removefromplayerlist",
                        Setting.REMOVE_PLAYERS_FROM_PLAYER_LIST.asBoolean());
                NMS.addOrRemoveFromPlayerList(getBukkitEntity(), removeFromPlayerList);

                if (Setting.USE_SCOREBOARD_TEAMS.asBoolean()) {
                    Scoreboard scoreboard = Util.getDummyScoreboard();
                    String teamName = Util.getTeamName(profile.getId());

                    Team team = scoreboard.getTeam(teamName);
                    int mode = 2;
                    if (team == null) {
                        team = scoreboard.registerNewTeam(teamName);
                        mode = 0;
                    }
                    if (prefixCapture != null) {
                        team.setPrefix(prefixCapture);
                    }
                    if (suffixCapture != null) {
                        team.setSuffix(suffixCapture);
                    }
                    team.addPlayer(handle.getBukkitEntity());

                    handle.getNPC().data().set(NPC.SCOREBOARD_FAKE_TEAM_NAME_METADATA, teamName);

                    Util.sendTeamPacketToOnlinePlayers(team, mode);
                }
            }
        }, 20);

        handle.getBukkitEntity().setSleepingIgnored(true);

        return handle.getBukkitEntity();
    }

    @Override
    public Player getBukkitEntity() {
        return (Player) super.getBukkitEntity();
    }

    @Override
    public void remove() {
        Player entity = getBukkitEntity();
        if (entity != null) {
            NMS.removeFromWorld(entity);
            SkinnableEntity npc = entity instanceof SkinnableEntity ? (SkinnableEntity) entity : null;
            npc.getSkinTracker().onRemoveNPC();
        }
        NMS.remove(entity);
        // Paper decided to break Spigot compatibility.
        // super.remove();
    }
}
