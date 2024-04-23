package net.citizensnpcs.util;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.common.primitives.Ints;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.speech.SpeechContext;
import net.citizensnpcs.api.ai.speech.Talkable;
import net.citizensnpcs.api.ai.speech.TalkableEntity;
import net.citizensnpcs.api.event.NPCCollisionEvent;
import net.citizensnpcs.api.event.NPCPistonPushEvent;
import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.BoundingBox;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.Placeholders;
import net.citizensnpcs.api.util.SpigotUtil;

public class Util {
    private Util() {
    }

    public static void callCollisionEvent(NPC npc, Entity entity) {
        if (NPCCollisionEvent.getHandlerList().getRegisteredListeners().length > 0) {
            Bukkit.getPluginManager().callEvent(new NPCCollisionEvent(npc, entity));
        }
    }

    public static boolean callPistonPushEvent(NPC npc) {
        if (npc == null)
            return false;
        NPCPistonPushEvent event = new NPCPistonPushEvent(npc);
        if (npc.isProtected()) {
            event.setCancelled(true);
        }
        Bukkit.getPluginManager().callEvent(event);
        return event.isCancelled();
    }

    public static <T> T callPossiblySync(Callable<T> callable, boolean sync) {
        if (!sync) {
            try {
                return callable.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            return Bukkit.getScheduler().callSyncMethod(CitizensAPI.getPlugin(), callable).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Vector callPushEvent(NPC npc, double x, double y, double z) {
        boolean allowed = npc == null || !npc.isProtected()
                || npc.data().has(NPC.Metadata.COLLIDABLE) && npc.data().<Boolean> get(NPC.Metadata.COLLIDABLE);
        if (NPCPushEvent.getHandlerList().getRegisteredListeners().length == 0)
            return allowed ? new Vector(x, y, z) : null;

        // when another entity collides, this method is called to push the NPC so we prevent it from
        // doing anything if the event is cancelled.
        Vector vector = new Vector(x, y, z);
        NPCPushEvent event = new NPCPushEvent(npc, vector, null);
        event.setCancelled(!allowed);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled() ? event.getCollisionVector() : null;
    }

    /**
     * Clamps the rotation angle to [-180, 180]
     */
    public static float clamp(float angle) {
        while (angle < -180.0F) {
            angle += 360.0F;
        }
        while (angle >= 180.0F) {
            angle -= 360.0F;
        }
        return angle;
    }

    public static float clamp(float angle, float min, float max, float d) {
        while (angle < min) {
            angle += d;
        }
        while (angle >= max) {
            angle -= d;
        }
        return angle;
    }

    public static int convert(TimeUnit unit, Duration delay) {
        return (int) (unit.convert(delay.getSeconds(), TimeUnit.SECONDS)
                + unit.convert(delay.getNano(), TimeUnit.NANOSECONDS));
    }

    public static ItemStack createItem(Material mat, String name) {
        return createItem(mat, name, null);
    }

    public static ItemStack createItem(Material mat, String name, String description) {
        ItemStack stack = new ItemStack(mat, 1);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(ChatColor.RESET + Messaging.parseComponents(name));
        if (description != null) {
            meta.setLore(Arrays.asList(Messaging.parseComponents(description).split("\n")));
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        stack.setItemMeta(meta);
        return stack;
    }

    public static ItemStack editTitle(ItemStack item, Function<String, String> transform) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(transform.apply(meta.hasDisplayName() ? meta.getDisplayName() : ""));
        item.setItemMeta(meta);
        return item;
    }

    public static void face(Entity entity, float yaw, float pitch) {
        double pitchCos = Math.cos(Math.toRadians(pitch));
        Vector vector = new Vector(Math.sin(Math.toRadians(yaw)) * -pitchCos, -Math.sin(Math.toRadians(pitch)),
                Math.cos(Math.toRadians(yaw)) * pitchCos).normalize();
        faceLocation(entity, entity.getLocation().clone().add(vector));
    }

    public static void faceEntity(Entity entity, Entity to) {
        if (to == null || entity == null || entity.getWorld() != to.getWorld())
            return;
        if (to instanceof LivingEntity) {
            NMS.look(entity, to);
        } else {
            faceLocation(entity, to.getLocation());
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

    public static Location getCenterLocation(Block block) {
        Location bloc = block.getLocation();
        Location center = new Location(bloc.getWorld(), bloc.getBlockX() + 0.5, bloc.getBlockY(),
                bloc.getBlockZ() + 0.5);
        BoundingBox bb = NMS.getCollisionBox(block);
        if (bb != null && bb.maxY - bb.minY < 0.6D) {
            center.setY(center.getY() + (bb.maxY - bb.minY));
        }
        return center;
    }

    /**
     * Returns the yaw to face along the given velocity (corrected for dragon yaw i.e. facing backwards)
     */
    public static float getDragonYaw(Entity entity, double motX, double motZ) {
        Location location = entity.getLocation();
        double x = location.getX();
        double z = location.getZ();
        double tX = x + motX;
        double tZ = z + motZ;
        if (z > tZ)
            return (float) -Math.toDegrees(Math.atan((x - tX) / (z - tZ)));
        if (z < tZ)
            return (float) -Math.toDegrees(Math.atan((x - tX) / (z - tZ))) + 180.0F;

        return location.getYaw();
    }

    public static Scoreboard getDummyScoreboard() {
        return DUMMY_SCOREBOARD;
    }

    public static Entity getEntity(UUID uuid) {
        if (SUPPORTS_BUKKIT_GETENTITY) {
            try {
                return Bukkit.getEntity(uuid);
            } catch (Throwable t) {
                SUPPORTS_BUKKIT_GETENTITY = false;
            }
        }
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getUniqueId().equals(uuid))
                    return entity;
            }
        }
        return null;
    }

    public static Location getEyeLocation(Entity entity) {
        return entity instanceof LivingEntity ? ((LivingEntity) entity).getEyeLocation() : entity.getLocation();
    }

    public static EntityType getFallbackEntityType(String first, String second) {
        for (EntityType type : EntityType.values()) {
            if (type.name().equals(first))
                return type;
            if (type.name().equals(second))
                return type;
        }
        return null;
    }

    public static Material getFallbackMaterial(String first, String... second) {
        try {
            return Material.valueOf(first);
        } catch (IllegalArgumentException e) {
            for (String s : second) {
                try {
                    return Material.valueOf(s);
                } catch (IllegalArgumentException iae) {
                }
            }
            return null;
        }
    }

    public static Random getFastRandom() {
        return new XORShiftRNG();
    }

    public static String getTeamName(UUID id) {
        return "CIT-" + id.toString().replace("-", "").substring(0, 12);
    }

    public static boolean inBlock(Entity entity) {
        // TODO: bounding box aware?
        Location loc = entity.getLocation();
        if (!Util.isLoaded(loc))
            return false;

        Block in = loc.getBlock();
        Block above = in.getRelative(BlockFace.UP);
        return in.getType().isSolid() && above.getType().isSolid() && NMS.isSolid(in) && NMS.isSolid(above);
    }

    public static boolean isAlwaysFlyable(EntityType type) {
        if (type.name().equals("VEX") || type.name().equals("PARROT") || type.name().equals("ALLAY")
                || type.name().equals("BEE") || type.name().equals("PHANTOM") || type.name().equals("BREEZE"))
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
                || name.equals("LLAMA") || name.equals("TRADER_LLAMA") || name.equals("CAMEL");
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
        return "<yellow>" + Joiner.on("<green>, <yellow>").join(values).toLowerCase();
    }

    public static boolean locationWithinRange(Location current, Location target, double range) {
        if (current == null || target == null || (current.getWorld() != target.getWorld()))
            return false;
        return current.distance(target) <= range;
    }

    public static <T extends Enum<?>> T matchEnum(T[] values, String toMatch) {
        toMatch = toMatch.toLowerCase().replace('-', '_').replace(' ', '_');
        for (T check : values) {
            if (toMatch.equals(check.name().toLowerCase())
                    || toMatch.equals("item") && check.name().equals("DROPPED_ITEM"))
                return check; // check for an exact match first

        }
        for (T check : values) {
            String name = check.name().toLowerCase();
            if (name.replace("_", "").equals(toMatch) || name.startsWith(toMatch))
                return check;

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
            if (matchMaterial == player.getInventory().getItemInHand().getType())
                return true;

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

    public static ItemStack parseItemStack(ItemStack stack, String item) {
        if (stack == null || stack.getType() == Material.AIR) {
            stack = new ItemStack(Material.STONE, 1);
        }
        if (item.charAt(0) == '{') {
            try {
                Bukkit.getUnsafe().modifyItemStack(stack, item);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        } else if (!item.isEmpty()) {
            String[] parts = Iterables.toArray(Splitter.on(',').split(item), String.class);
            if (parts.length == 0)
                return stack;
            stack.setType(Material.matchMaterial(parts[0]));
            if (parts.length > 1) {
                stack.setAmount(Ints.tryParse(parts[1]));
            }
            if (parts.length > 2) {
                Integer durability = Ints.tryParse(parts[2]);
                stack.setDurability(durability.shortValue());
            }
        }
        return stack;
    }

    public static int parseTicks(String raw) {
        Duration duration = SpigotUtil.parseDuration(raw, null);
        return duration == null ? -1 : toTicks(duration);
    }

    public static String prettyEnum(Enum<?> e) {
        return e.name().toLowerCase().replace('_', ' ');
    }

    public static String prettyPrintLocation(Location to) {
        return String.format("%s at %s, %s, %s (%s, %s)", to.getWorld().getName(), TWO_DIGIT_DECIMAL.format(to.getX()),
                TWO_DIGIT_DECIMAL.format(to.getY()), TWO_DIGIT_DECIMAL.format(to.getZ()),
                TWO_DIGIT_DECIMAL.format(to.getYaw()), TWO_DIGIT_DECIMAL.format(to.getPitch()));
    }

    public static String rawtype(Enum<?>[] values) {
        return "<yellow>" + Joiner.on("<green>, <yellow>").join(values).toLowerCase();
    }

    public static void runCommand(NPC npc, Player clicker, String command, boolean op, boolean player) {
        List<String> split = Splitter.on(' ').omitEmptyStrings().trimResults().limit(2).splitToList(command);
        String bungeeServer = split.size() == 2 && split.get(0).equalsIgnoreCase("server") ? split.get(1) : null;
        String cmd = command;
        if (command.startsWith("say")) {
            cmd = "npc speak " + command.replaceFirst("say", "").trim() + " --target <p>";
        }
        if ((cmd.startsWith("npc ") || cmd.startsWith("waypoints ") || cmd.startsWith("wp "))
                && !cmd.contains("--id ")) {
            cmd += " --id <id>";
        }
        String interpolatedCommand = Placeholders.replace(cmd, clicker, npc);
        Messaging.idebug(() -> "Running command " + interpolatedCommand + " on NPC " + (npc == null ? -1 : npc.getId())
                + " clicker " + clicker);

        if (!player) {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), interpolatedCommand);
            return;
        }
        boolean wasOp = clicker.isOp();
        if (op) {
            clicker.setOp(true);
        }
        try {
            if (bungeeServer != null) {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Connect");
                out.writeUTF(bungeeServer);

                clicker.sendPluginMessage(CitizensAPI.getPlugin(), "BungeeCord", out.toByteArray());
            } else {
                clicker.chat("/" + interpolatedCommand);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            if (op) {
                clicker.setOp(wasOp);
            }
        }
    }

    public static void sendBlockChanges(List<Block> blocks, Material type) {
        if (blocks.isEmpty())
            return;
        Location loc = new Location(null, 0, 0, 0);
        for (Player player : blocks.get(0).getWorld().getPlayers()) {
            for (Block block : blocks) {
                if (type != null) {
                    player.sendBlockChange(block.getLocation(loc), type, (byte) 0);
                } else if (SpigotUtil.isUsing1_13API()) {
                    player.sendBlockChange(block.getLocation(loc), block.getBlockData());
                } else {
                    player.sendBlockChange(block.getLocation(loc), block.getType(), block.getData());
                }
            }
        }
    }

    public static void talk(SpeechContext context) {
        if (context.getTalker() == null)
            return;
        NPC npc = CitizensAPI.getNPCRegistry().getNPC(context.getTalker().getEntity());
        if (npc == null)
            return;

        // chat to the world with CHAT_FORMAT and CHAT_RANGE settings
        if (!context.hasRecipients()) {
            String text = Setting.CHAT_FORMAT.asString().replace("<text>", context.getMessage());
            talkToBystanders(npc, text, context);
            return;
        }

        // Assumed recipients at this point
        else if (context.size() <= 1) {
            String text = Setting.CHAT_FORMAT_TO_TARGET.asString().replace("<text>", context.getMessage());
            String targetName = "";
            // For each recipient
            for (Talkable talkable : context) {
                talkable.talkTo(context, text);
                targetName = talkable.getName();
            }
            // Check if bystanders hear targeted chat
            if (!Setting.CHAT_BYSTANDERS_HEAR_TARGETED_CHAT.asBoolean())
                return;
            // Format message with config setting and send to bystanders
            String bystanderText = Setting.CHAT_FORMAT_TO_BYSTANDERS.asString().replace("<target>", targetName)
                    .replace("<text>", context.getMessage());
            talkToBystanders(npc, bystanderText, context);
            return;
        }

        else { // Multiple recipients
            String text = Setting.CHAT_FORMAT_TO_TARGET.asString().replace("<text>", context.getMessage());
            List<String> targetNames = new ArrayList<>();
            // Talk to each recipient
            for (Talkable talkable : context) {
                talkable.talkTo(context, text);
                targetNames.add(talkable.getName());
            }
            if (!Setting.CHAT_BYSTANDERS_HEAR_TARGETED_CHAT.asBoolean())
                return;
            String targets = "";
            int max = Setting.CHAT_MAX_NUMBER_OF_TARGETS.asInt();
            String[] format = Setting.CHAT_MULTIPLE_TARGETS_FORMAT.asString().split("\\|");
            if (format.length != 4) {
                Messaging.severe("npc.chat.options.multiple-targets-format invalid!");
            }
            if (max == 1) {
                targets = format[0].replace("<target>", targetNames.get(0)) + format[3];
            } else if (max == 2 || targetNames.size() == 2) {
                if (targetNames.size() == 2) {
                    targets = format[0].replace("<target>", targetNames.get(0))
                            + format[2].replace("<target>", targetNames.get(1));
                } else {
                    targets = format[0].replace("<target>", targetNames.get(0))
                            + format[1].replace("<target>", targetNames.get(1)) + format[3];
                }
            } else if (max >= 3) {
                targets = format[0].replace("<target>", targetNames.get(0));

                int x = 1;
                for (x = 1; x < max - 1; x++) {
                    if (targetNames.size() - 1 == x) {
                        break;
                    }
                    targets = targets + format[1].replace("<npc>", targetNames.get(x));
                }
                if (targetNames.size() == max) {
                    targets = targets + format[2].replace("<npc>", targetNames.get(x));
                } else {
                    targets = targets + format[3];
                }
            }
            String bystanderText = Setting.CHAT_FORMAT_WITH_TARGETS_TO_BYSTANDERS.asString()
                    .replace("<targets>", targets).replace("<text>", context.getMessage());
            talkToBystanders(npc, bystanderText, context);
        }
    }

    private static void talkToBystanders(NPC npc, String text, SpeechContext context) {
        // Get list of nearby entities
        List<Entity> bystanderEntities = npc.getEntity().getNearbyEntities(Setting.CHAT_RANGE.asDouble(),
                Setting.CHAT_RANGE.asDouble(), Setting.CHAT_RANGE.asDouble());
        for (Entity bystander : bystanderEntities) {
            boolean shouldTalk = true;
            if (!Setting.TALK_CLOSE_TO_NPCS.asBoolean() && CitizensAPI.getNPCRegistry().isNPC(bystander)) {
                shouldTalk = false;
            }
            if (context.hasRecipients()) {
                for (Talkable target : context) {
                    if (target.getEntity().equals(bystander)) {
                        shouldTalk = false;
                        break;
                    }
                }
            }
            if (shouldTalk) {
                new TalkableEntity(bystander).talkNear(context, text);
            }
        }
    }

    public static int toTicks(Duration delay) {
        return (int) (TimeUnit.MILLISECONDS.convert(delay.getSeconds(), TimeUnit.SECONDS)
                + TimeUnit.MILLISECONDS.convert(delay.getNano(), TimeUnit.NANOSECONDS)) / 50;
    }

    private static final Scoreboard DUMMY_SCOREBOARD = Bukkit.getScoreboardManager().getNewScoreboard();
    private static boolean SUPPORTS_BUKKIT_GETENTITY = true;
    private static boolean SUPPORTS_ENTITY_CANSEE = true;

    private static final DecimalFormat TWO_DIGIT_DECIMAL = new DecimalFormat();

    static {
        TWO_DIGIT_DECIMAL.setMaximumFractionDigits(2);
    }
}
