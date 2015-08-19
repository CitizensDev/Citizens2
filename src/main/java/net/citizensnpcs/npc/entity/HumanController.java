package net.citizensnpcs.npc.entity;

import java.util.UUID;
import java.util.regex.Pattern;

import com.mojang.authlib.GameProfile;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Colorizer;
import net.citizensnpcs.npc.AbstractEntityController;
import net.citizensnpcs.npc.skin.NPCSkin;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;
import net.minecraft.server.v1_8_R3.WorldServer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class HumanController extends AbstractEntityController {

    public HumanController() {
        super();
    }

    @Override
    protected boolean couldSpawn(Location at, NPC npc, net.minecraft.server.v1_8_R3.Entity entity) {
        if (!super.couldSpawn(at, npc, entity)) {
            return false;
        }

        EntityHumanNPC human = (EntityHumanNPC)entity;

        human.packetTracker.sendAddPacketNearby(200.0);

        return true;
    }

    @Override
    protected Entity createEntity(final Location at, final NPC npc) {
        final WorldServer nmsWorld = ((CraftWorld) at.getWorld()).getHandle();
        String coloredName = Colorizer.parseColors(npc.getFullName());
        if (coloredName.length() > 16) {
            coloredName = coloredName.substring(0, 16);
        }

        String name, prefix = null, suffix = null;
        if (coloredName.length() > 16) {
            prefix = coloredName.substring(0, 16);
            if (coloredName.length() > 30) {
                int len = 30;
                name = coloredName.substring(16, 30);
                if (NON_ALPHABET_MATCHER.matcher(name).matches()) {
                    if (coloredName.length() >= 32) {
                        len = 32;
                        name = coloredName.substring(16, 32);
                    } else if (coloredName.length() == 31) {
                        len = 31;
                        name = coloredName.substring(16, 31);
                    }
                } else {
                    name = ChatColor.RESET + name;
                }
                suffix = coloredName.substring(len);
            } else {
                name = coloredName.substring(16);
                if (!NON_ALPHABET_MATCHER.matcher(name).matches()) {
                    name = ChatColor.RESET + name;
                }
                if (name.length() > 16) {
                    suffix = name.substring(16);
                    name = name.substring(0, 16);
                }
            }
        }
        final String prefixCapture = prefix, suffixCapture = suffix, coloredNameCapture = coloredName;

        UUID uuid = npc.getUniqueId();
        if (uuid.version() == 4) { // clear version
            long msb = uuid.getMostSignificantBits();
            msb &= ~0x0000000000004000L;
            msb |= 0x0000000000002000L;
            uuid = new UUID(msb, uuid.getLeastSignificantBits());
        }

        final GameProfile profile = new GameProfile(uuid, coloredName);

        new NPCSkin(npc).setSkin(getSkinName(npc), nmsWorld, profile);

        final EntityHumanNPC handle = new EntityHumanNPC(nmsWorld.getServer().getServer(), nmsWorld, profile,
                new PlayerInteractManager(nmsWorld), npc);

        handle.setPositionRotation(at.getX(), at.getY(), at.getZ(), at.getYaw(), at.getPitch());

        Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {

            @Override
            public void run() {

                if (getBukkitEntity() == null || !getBukkitEntity().isValid())
                    return;

                // set skin again in case the entity was in a currently loading chunk
                // when first set.
                new NPCSkin(npc).setSkin(getSkinName(npc), nmsWorld, profile);

                if (prefixCapture != null) {
                    Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
                    String teamName = UUID.randomUUID().toString().substring(0, 16);

                    Team team = scoreboard.getTeam(teamName);
                    if (team == null) {
                        team = scoreboard.registerNewTeam(teamName);
                        team.setPrefix(prefixCapture);
                        if (suffixCapture != null) {
                            team.setSuffix(suffixCapture);
                        }
                    }
                    team.addPlayer(handle.getBukkitEntity());

                    handle.getNPC().data().set(NPC.SCOREBOARD_FAKE_TEAM_NAME_METADATA, teamName);
                }
            }
        }, 5);

        handle.getBukkitEntity().setSleepingIgnored(true);

        return handle.getBukkitEntity();
    }

    @Override
    public Player getBukkitEntity() {
        return (Player) super.getBukkitEntity();
    }

    @Override
    public void remove() {
        EntityHumanNPC handle = (EntityHumanNPC)((CraftPlayer)getBukkitEntity()).getHandle();
        handle.world.removeEntity(handle);
        handle.packetTracker.sendRemovePacket();
        super.remove();
    }

    private static String getSkinName(NPC npc) {
        String skinName = npc.data().get(NPC.PLAYER_SKIN_UUID_METADATA);
        if (skinName == null) {
            skinName = ChatColor.stripColor(npc.getName());
        }
        return skinName;
    }

    private static Pattern NON_ALPHABET_MATCHER = Pattern.compile(".*[^A-Za-z0-9_].*");
}
