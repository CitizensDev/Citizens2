package net.citizensnpcs.util;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import net.citizensnpcs.api.event.NPCCollisionEvent;
import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.SpigotUtil;
import net.citizensnpcs.npc.ai.NPCHolder;

public class Util {
    // Static class for small (emphasis small) utility methods
    private Util() {
    }

    public static void assumePose(Entity entity, float yaw, float pitch) {
        NMS.look(entity, yaw, pitch);
    }

    public static void callCollisionEvent(NPC npc, Entity entity) {
        if (NPCCollisionEvent.getHandlerList().getRegisteredListeners().length > 0) {
            Bukkit.getPluginManager().callEvent(new NPCCollisionEvent(npc, entity));
        }
    }

    public static NPCPushEvent callPushEvent(NPC npc, Vector vector) {
        NPCPushEvent event = new NPCPushEvent(npc, vector);
        event.setCancelled(
                npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true) || !npc.data().get(NPC.COLLIDABLE_METADATA, true));
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static float clampYaw(float yaw) {
        while (yaw < -180.0F) {
            yaw += 360.0F;
        }

        while (yaw >= 180.0F) {
            yaw -= 360.0F;
        }
        return yaw;
    }

    public static void faceEntity(Entity entity, Entity at) {
        if (at == null || entity == null || entity.getWorld() != at.getWorld())
            return;
        if (at instanceof LivingEntity) {
            NMS.look(entity, at);
        } else {
            faceLocation(entity, at.getLocation(AT_LOCATION));
        }
    }

    public static void faceLocation(Entity entity, Location to) {
        faceLocation(entity, to, false);
    }

    public static void faceLocation(Entity entity, Location to, boolean headOnly) {
        faceLocation(entity, to, headOnly, true);
    }

    public static void faceLocation(Entity entity, Location to, boolean headOnly, boolean immediate) {
        if (to == null || entity.getWorld() != to.getWorld())
            return;
        NMS.look(entity, to, headOnly, immediate);
    }

    public static Scoreboard getDummyScoreboard() {
        return DUMMY_SCOREBOARD;
    }

    public static Location getEyeLocation(Entity entity) {
        return entity instanceof LivingEntity ? ((LivingEntity) entity).getEyeLocation() : entity.getLocation();
    }

    public static Random getFastRandom() {
        return new XORShiftRNG();
    }

    public static String getMinecraftRevision() {
        if (MINECRAFT_REVISION == null) {
            MINECRAFT_REVISION = Bukkit.getServer().getClass().getPackage().getName();
        }
        return MINECRAFT_REVISION.substring(MINECRAFT_REVISION.lastIndexOf('.') + 2);
    }

    public static String getTeamName(UUID id) {
        return "CIT-" + id.toString().replace("-", "").substring(0, 12);
    }

    public static boolean inBlock(Entity entity) {
        // TODO: bounding box aware?
        Location loc = entity.getLocation(AT_LOCATION);
        if (!Util.isLoaded(loc)) {
            return false;
        }
        Block in = loc.getBlock();
        return in.getType().isSolid() && in.getRelative(BlockFace.UP).getType().isSolid();
    }

    public static boolean isAlwaysFlyable(EntityType type) {
        if (type.name().toLowerCase().equals("vex") || type.name().toLowerCase().equals("parrot")
                || type.name().toLowerCase().equals("bee") || type.name().toLowerCase().equals("phantom"))
            // 1.8.8 compatibility
            return true;
        switch (type) {
            case BAT:
            case BLAZE:
            case ENDER_DRAGON:
            case GHAST:
            case WITHER:
                return true;
            default:
                return false;
        }
    }

    public static boolean isHorse(EntityType type) {
        String name = type.name();
        return type == EntityType.HORSE || name.contains("_HORSE") || name.equals("DONKEY") || name.equals("MULE")
                || name.equals("LLAMA") || name.equals("TRADER_LLAMA");
    }

    public static boolean isLoaded(Location location) {
        if (location.getWorld() == null)
            return false;
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        return location.getWorld().isChunkLoaded(chunkX, chunkZ);
    }

    public static boolean isOffHand(PlayerInteractEntityEvent event) {
        try {
            return event.getHand() == org.bukkit.inventory.EquipmentSlot.OFF_HAND;
        } catch (NoSuchMethodError e) {
            return false;
        } catch (NoSuchFieldError e) {
            return false;
        }
    }

    public static boolean isOffHand(PlayerInteractEvent event) {
        try {
            return event.getHand() == org.bukkit.inventory.EquipmentSlot.OFF_HAND;
        } catch (NoSuchMethodError e) {
            return false;
        } catch (NoSuchFieldError e) {
            return false;
        }
    }

    public static String listValuesPretty(Enum<?>[] values) {
        return "<e>" + Joiner.on("<a>, <e>").join(values).toLowerCase();
    }

    public static boolean locationWithinRange(Location current, Location target, double range) {
        if (current == null || target == null)
            return false;
        if (current.getWorld() != target.getWorld())
            return false;
        return current.distanceSquared(target) <= Math.pow(range, 2);
    }

    public static EntityType matchEntityType(String toMatch) {
        return matchEnum(EntityType.values(), toMatch);
    }

    public static <T extends Enum<?>> T matchEnum(T[] values, String toMatch) {
        toMatch = toMatch.toLowerCase().replace('-', '_').replace(' ', '_');
        for (T check : values) {
            if (toMatch.equals(check.name().toLowerCase())
                    || (toMatch.equals("item") && check == EntityType.DROPPED_ITEM)) {
                return check; // check for an exact match first
            }
        }
        for (T check : values) {
            String name = check.name().toLowerCase();
            if (name.replace("_", "").equals(toMatch) || name.startsWith(toMatch)) {
                return check;
            }
        }
        return null;
    }

    public static boolean matchesItemInHand(Player player, String setting) {
        String parts = setting;
        if (parts.contains("*") || parts.isEmpty())
            return true;
        for (String part : Splitter.on(',').split(parts)) {
            Material matchMaterial = SpigotUtil.isUsing1_13API() ? Material.matchMaterial(part, false)
                    : Material.matchMaterial(part);
            if (matchMaterial == null) {
                if (part.equals("280")) {
                    matchMaterial = Material.STICK;
                } else if (part.equals("340")) {
                    matchMaterial = Material.BOOK;
                }
            }
            if (matchMaterial == player.getInventory().getItemInHand().getType()) {
                return true;
            }
        }
        return false;
    }

    public static Set<EntityType> optionalEntitySet(String... types) {
        Set<EntityType> list = EnumSet.noneOf(EntityType.class);
        for (String type : types) {
            try {
                list.add(EntityType.valueOf(type));
            } catch (IllegalArgumentException e) {
            }
        }
        return list;
    }

    public static String prettyEnum(Enum<?> e) {
        return e.name().toLowerCase().replace('_', ' ');
    }

    public static String prettyPrintLocation(Location to) {
        return String.format("%s at %d, %d, %d (%d, %d)", to.getWorld().getName(), to.getBlockX(), to.getBlockY(),
                to.getBlockZ(), (int) to.getYaw(), (int) to.getPitch());
    }

    /**
     * @param mode
     *            0 for create, 1 for remove, 2 for update
     */
    public static void sendTeamPacketToOnlinePlayers(Team team, int mode) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            NMS.sendTeamPacket(player, team, mode);
        }
    }

    public static String[] splitPlayerName(String coloredName) {
        String name = coloredName, prefix = null, suffix = null;
        if (coloredName.length() > 16) {
            if (coloredName.length() >= 30) {
                prefix = coloredName.substring(0, 16);
                int len = 30;
                name = coloredName.substring(16, 30);
                String prefixColors = ChatColor.getLastColors(prefix);
                if (prefixColors.isEmpty()) {
                    if (NON_ALPHABET_MATCHER.matcher(name).matches()) {
                        if (coloredName.length() >= 32) {
                            len = 32;
                            name = coloredName.substring(16, 32);
                        } else if (coloredName.length() == 31) {
                            len = 31;
                            name = coloredName.substring(16, 31);
                        }
                    } else {
                        prefixColors = ChatColor.RESET.toString();
                    }
                } else if (prefixColors.length() > 2) {
                    prefixColors = prefixColors.substring(prefixColors.length() - 2);
                }
                name = prefixColors + name;
                suffix = coloredName.substring(len);
            } else {
                prefix = coloredName.substring(0, coloredName.length() - 16);
                name = coloredName.substring(prefix.length());
                if (prefix.endsWith(String.valueOf(ChatColor.COLOR_CHAR))) {
                    prefix = prefix.substring(0, prefix.length() - 1);
                    name = ChatColor.COLOR_CHAR + name;
                }
                String prefixColors = ChatColor.getLastColors(prefix);
                if (prefixColors.isEmpty() && !NON_ALPHABET_MATCHER.matcher(name).matches()) {
                    prefixColors = ChatColor.RESET.toString();
                } else if (prefixColors.length() > 2) {
                    prefixColors = prefixColors.substring(prefixColors.length() - 2);
                }
                name = prefixColors + name;
                if (name.length() > 16) {
                    suffix = name.substring(16);
                    name = name.substring(0, 16);
                }
            }
        }
        return new String[] { name, prefix, suffix };
    }

    public static void updateNPCTeams(Player toUpdate, int mode) {
        for (Player player : PlayerUpdateTask.getRegisteredPlayerNPCs()) {
            NPC npc = ((NPCHolder) player).getNPC();

            String teamName = npc.data().get(NPC.SCOREBOARD_FAKE_TEAM_NAME_METADATA, "");
            Team team = null;
            if (teamName.length() == 0 || (team = Util.getDummyScoreboard().getTeam(teamName)) == null)
                continue;

            NMS.sendTeamPacket(toUpdate, team, mode);
        }
    }

    private static final Location AT_LOCATION = new Location(null, 0, 0, 0);
    private static final Scoreboard DUMMY_SCOREBOARD = Bukkit.getScoreboardManager().getNewScoreboard();
    private static String MINECRAFT_REVISION;
    private static final Pattern NON_ALPHABET_MATCHER = Pattern.compile(".*[^A-Za-z0-9_].*");
}
