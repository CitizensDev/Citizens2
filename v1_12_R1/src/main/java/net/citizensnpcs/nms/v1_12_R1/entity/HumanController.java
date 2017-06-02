package net.citizensnpcs.nms.v1_12_R1.entity; import net.minecraft.server.v1_12_R1.DamageSource;

import java.util.UUID;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
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
import net.minecraft.server.v1_12_R1.PlayerInteractManager;
import net.minecraft.server.v1_12_R1.WorldServer;

public class HumanController extends AbstractEntityController {
    public HumanController() {
        super();
    }

    @Override
    protected Entity createEntity(final Location at, final NPC npc) {
        final WorldServer nmsWorld = ((CraftWorld) at.getWorld()).getHandle();
        String coloredName = Colorizer.parseColors(npc.getFullName());

        String name = coloredName, prefix = null, suffix = null;
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
            coloredName = coloredName.substring(0, 16);
        }

        final String prefixCapture = prefix, suffixCapture = suffix;

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
                NMS.addOrRemoveFromPlayerList(getBukkitEntity(),
                        npc.data().get("removefromplayerlist", removeFromPlayerList));

                if (Setting.USE_SCOREBOARD_TEAMS.asBoolean()) {
                    Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
                    String teamName = profile.getId().toString().substring(0, 16);

                    Team team = scoreboard.getTeam(teamName);
                    if (team == null) {
                        team = scoreboard.registerNewTeam(teamName);
                        if (prefixCapture != null) {
                            team.setPrefix(prefixCapture);
                        }
                        if (suffixCapture != null) {
                            team.setSuffix(suffixCapture);
                        }
                    }
                    team.addPlayer(handle.getBukkitEntity());

                    handle.getNPC().data().set(NPC.SCOREBOARD_FAKE_TEAM_NAME_METADATA, teamName);
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
        super.remove();
    }

    private static Pattern NON_ALPHABET_MATCHER = Pattern.compile(".*[^A-Za-z0-9_].*");
}
