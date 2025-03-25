package net.citizensnpcs.api.npc;

import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;

import com.google.common.base.Optional;
import com.google.common.reflect.TypeToken;

import net.citizensnpcs.api.ai.GoalController;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.speech.SpeechController;
import net.citizensnpcs.api.astar.Agent;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.SpawnReason;
import net.citizensnpcs.api.npc.BlockBreaker.BlockBreakerConfiguration;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitFactory;
import net.citizensnpcs.api.util.BoundingBox;
import net.citizensnpcs.api.util.DataKey;
import net.kyori.adventure.text.Component;

/**
 * Represents an NPC with optional {@link Trait}s.
 */
public interface NPC extends Agent, Cloneable {
    /**
     * Adds a {@link Runnable} that will run every tick. Note that removal is not yet supported.
     *
     * @param runnable
     *            Runnable to be added
     */
    public void addRunnable(Runnable runnable);

    /**
     * Adds a trait to this NPC. This will use the {@link TraitFactory} defined for this NPC to construct and attach a
     * trait using {@link #addTrait(Trait)}.
     *
     * @param trait
     *            The class of the trait to add
     */
    public void addTrait(Class<? extends Trait> trait);

    /**
     * Adds a trait to this NPC.
     *
     * @param trait
     *            Trait to add
     */
    public void addTrait(Trait trait);

    /**
     * @return A clone of the NPC. May not be an exact copy depending on the {@link Trait}s installed.
     */
    public NPC clone();

    /**
     * @return A clone of the NPC. May not be an exact copy depending on the {@link Trait}s installed.
     */
    public NPC copy();

    /**
     * @return The metadata store of this NPC.
     */
    public MetadataStore data();

    /**
     * Despawns this NPC. This is equivalent to calling {@link #despawn(DespawnReason)} with
     * {@link DespawnReason#PLUGIN}.
     *
     * @return Whether this NPC was able to despawn
     */
    public boolean despawn();

    /**
     * Despawns this NPC.
     *
     * @param reason
     *            The reason for despawning, for use in {@link NPCDespawnEvent}
     * @return Whether this NPC was able to despawn
     */
    public boolean despawn(DespawnReason reason);

    /**
     * Permanently removes this NPC and all data about it from the registry it's attached to.
     */
    public void destroy();

    /**
     * Permanently removes this NPC and all data about it from the registry it's attached to.
     *
     * @param source
     *            The source of the removal
     */
    public void destroy(CommandSender source);

    /**
     * Faces a given {@link Location} if the NPC is spawned.
     */
    public void faceLocation(Location location);

    /**
     * Creates a {@link BlockBreaker} that allows you to break blocks using the Minecraft breaking algorithm.
     */
    public BlockBreaker getBlockBreaker(Block targetBlock, BlockBreakerConfiguration config);

    /**
     * Gets the default {@link GoalController} of this NPC.
     *
     * @return Default goal controller
     */
    public GoalController getDefaultGoalController();

    /**
     * Gets the default {@link SpeechController} of this NPC.
     *
     * @return Default speech controller
     */
    public SpeechController getDefaultSpeechController();

    /**
     * Gets the Bukkit entity associated with this NPC. This may be <code>null</code> if {@link #isSpawned()} is false.
     *
     * @return Entity associated with this NPC
     */
    public Entity getEntity();

    /**
     * Gets the full name of this NPC.
     *
     * @return Full name of this NPC
     */
    public String getFullName();

    /**
     * Gets the unique ID of this NPC. This is not guaranteed to be globally unique across server sessions.
     *
     * @return ID of this NPC
     */
    public int getId();

    /**
     * @see #setItemProvider(Supplier)
     */
    public Supplier<ItemStack> getItemProvider();

    /**
     * For certain mob types (currently, Players) it is beneficial to change the UUID slightly to signal to the client
     * that the mob is an NPC not a real mob. This will return {@link #getUniqueId()} with the necessary changes for the
     * current mob type.
     *
     * @return The client unique ID.
     */
    public UUID getMinecraftUniqueId();

    /**
     * Gets the name of this NPC with color codes stripped.
     *
     * @return Stripped name of this NPC
     */
    public String getName();

    /**
     * @return The {@link Navigator} of this NPC.
     */
    public Navigator getNavigator();

    /**
     * Gets a trait from the given class. If the NPC does not currently have the trait then it will be created and
     * attached using {@link #addTrait(Class)} .
     *
     * @param trait
     *            Trait to get
     * @return Trait with the given name
     */
    public <T extends Trait> T getOrAddTrait(Class<T> trait);

    /**
     * @return The {@link NPCRegistry} that created this NPC.
     */
    public NPCRegistry getOwningRegistry();

    String getRawName();

    /**
     * If the NPC is not spawned, then this method will return the last known location, or null if it has never been
     * spawned. Otherwise, it is equivalent to calling <code>npc.getBukkitEntity().getLocation()</code>.
     *
     * @return The stored location, or <code>null</code> if none was found.
     */
    public Location getStoredLocation();

    /**
     * Gets a trait from the given class. If the NPC does not currently have the trait then it will be created and
     * attached using {@link #addTrait(Class)} .
     *
     * @param trait
     *            Trait class
     * @return Trait with the given class
     *
     * @deprecated for intransparent naming. Use {@link #getOrAddTrait(Class)} for the same behavior.
     */
    @Deprecated
    public <T extends Trait> T getTrait(Class<T> trait);

    /**
     * Gets the trait instance with the given class. If the NPC does not currently have the trait, <code>null</code>
     * will be returned.
     *
     * @param trait
     *            Trait class
     * @return Trait with the given class
     */
    public <T extends Trait> T getTraitNullable(Class<T> trait);

    /**
     * Gets the trait instance with the given class. If the NPC does not currently have the trait,
     * <code>Optional.absent()</code> will be returned.
     *
     * @param trait
     *            Trait class
     * @return Trait with the given class
     */
    public default <T extends Trait> Optional<T> getTraitOptional(Class<T> trait) {
        return Optional.<T> fromNullable(getTraitNullable(trait));
    }

    /**
     * Returns the currently attached {@link Trait}s
     *
     * @return An Iterable of the current traits
     */
    public Iterable<Trait> getTraits();

    /**
     * Gets the unique id of this NPC. This is guaranteed to be unique for all NPCs.
     *
     * @return The unique id
     */
    public UUID getUniqueId();

    /**
     * Checks if this NPC has the given trait.
     *
     * @param trait
     *            Trait to check
     * @return Whether this NPC has the given trait
     */
    public boolean hasTrait(Class<? extends Trait> trait);

    /**
     * Returns whether this NPC is flyable or not.
     *
     * @return Whether this NPC is flyable
     */
    public boolean isFlyable();

    /**
     * Returns whether the given player can see the NPC (i.e. receive packets about it).
     *
     * @param player
     *            The player to check
     * @return Whether the NPC is hidden from the player
     */
    public boolean isHiddenFrom(Player player);

    /**
     * Gets whether this NPC is protected from damage, movement and other events that players and mobs use to change the
     * entity state of the NPC.
     *
     * @return Whether this NPC is protected
     */
    public boolean isProtected();

    /**
     * Gets whether this NPC is pushable by fluids.
     *
     * @return Whether this NPC is pushable by fluids
     */
    public boolean isPushableByFluids();

    /**
     * Gets whether this NPC is currently spawned.
     *
     * @return Whether this NPC is spawned
     */
    public boolean isSpawned();

    public boolean isUpdating(NPCUpdate update);

    /**
     * Loads the {@link NPC} from the given {@link DataKey}. This reloads all traits, respawns the NPC and sets it up
     * for execution. Should not be called often.
     *
     * @param key
     *            The root data key
     */
    public void load(DataKey key);

    /**
     * Removes a trait from this NPC.
     *
     * @param trait
     *            Trait to remove
     */
    public void removeTrait(Class<? extends Trait> trait);

    public boolean requiresNameHologram();

    /**
     * Saves the {@link NPC} to the given {@link DataKey}. This includes all metadata, traits, and spawn information
     * that will allow it to respawn at a later time via {@link #load(DataKey)}.
     *
     * @param key
     *            The root data key
     */
    public void save(DataKey key);

    public void scheduleUpdate(NPCUpdate update);

    /**
     * Sets whether to always use a name hologram instead of the in-built Minecraft name.
     *
     * @param use
     *            Whether to use a hologram
     */
    public void setAlwaysUseNameHologram(boolean use);

    /**
     * Sets the {@link EntityType} of this NPC. The NPC will respawned if currently spawned, or will remain despawned
     * otherwise.
     *
     * @param type
     *            The new mob type
     */
    public void setBukkitEntityType(EntityType type);

    /**
     * Sets whether this NPC is <tt>flyable</tt> or not. Note that this is intended for normally <em>ground-based</em>
     * entities only - it will generally have no effect on mob types that were originally flyable.
     *
     * @param flyable
     */
    public void setFlyable(boolean flyable);

    /**
     * For item-type NPCs, set a {@link Supplier} of the {@link ItemStack} to use when spawning the NPC.
     *
     * @param supplier
     *            The supplier
     */
    public void setItemProvider(Supplier<ItemStack> supplier);

    /**
     * Set the destination location to walk towards in a straight line using Minecraft movement. Should be called every
     * tick.
     *
     * @param destination
     *            The destination {@link Location}
     */
    public void setMoveDestination(Location destination);

    /**
     * Sets the name of this NPC.
     *
     * @param name
     *            Name to give this NPC
     */
    public void setName(String name);

    /**
     * A helper method to set the NPC as protected or not protected from damage/entity target events. Equivalent to
     * <code>npc.data().set(NPC.Metadata#DEFAULT_PROTECTED_METADATA, isProtected);</code>
     *
     * @param isProtected
     *            Whether the NPC should be protected
     */
    public void setProtected(boolean isProtected);

    public void setSneaking(boolean sneaking);

    /**
     * Set the NPC to use Minecraft AI where possible. Note that the NPC may not always behave exactly like a Minecraft
     * mob would because of additional Citizens APIs.
     */
    public void setUseMinecraftAI(boolean use);

    boolean shouldRemoveFromPlayerList();

    /**
     * @return Whether to remove the NPC from the tablist. Only applicable for {@link Player}-type NPCs.
     */
    public boolean shouldRemoveFromTabList();

    /**
     * Attempts to spawn this NPC.
     *
     * @param location
     *            Location to spawn this NPC
     * @return Whether this NPC was able to spawn at the location
     */
    public boolean spawn(Location location);

    /**
     * Attempts to spawn this NPC.
     *
     * @param location
     *            Location to spawn this NPC
     * @param reason
     *            Reason for spawning
     * @return Whether this NPC was able to spawn at the location
     */
    public boolean spawn(Location location, SpawnReason reason);

    /**
     * An alternative to <code>npc.getEntity().getLocation()</code> that teleports passengers as well.
     *
     * @param location
     *            The destination location
     * @param cause
     *            The cause for teleporting
     */
    public void teleport(Location location, TeleportCause cause);

    /**
     * Whether the NPC is currently set to use Minecraft AI. Defaults to false.
     */
    public boolean useMinecraftAI();

    public enum Metadata {
        /** The activation range. Integer, defaults to the server's configured activation range. */
        ACTIVATION_RANGE("activation-range", Integer.class),
        AGGRESSIVE("entity-aggressive", Boolean.class),
        ALWAYS_USE_NAME_HOLOGRAM("always-use-name-hologram", Boolean.class),
        /**
         * The Minecraft ambient sound played.
         */
        AMBIENT_SOUND("ambient-sound", String.class),
        @SuppressWarnings("serial")
        BOUNDING_BOX_FUNCTION("bounding-box-function", new TypeToken<Supplier<BoundingBox>>() {
        }, false),
        /**
         * Whether the NPC is collidable with Players or not.
         */
        COLLIDABLE("collidable", Boolean.class),
        /**
         * Whether the NPC can damage other Entities.
         */
        DAMAGE_OTHERS("damage-others", Boolean.class),
        /**
         * The Minecraft sound played when the NPC dies. String - Minecraft sound name.
         */
        DEATH_SOUND("death-sound", String.class),
        /**
         * Whether the NPC is 'protected' i.e. invulnerable to damage.
         */
        DEFAULT_PROTECTED("protected", Boolean.class),
        /**
         * Whether to disable the default stuck action (teleport to destination is default).
         */
        DISABLE_DEFAULT_STUCK_ACTION("disable-default-stuck-action", Boolean.class),
        /**
         * Whether the NPC drops its inventory after death.
         */
        DROPS_ITEMS("drops-items", Boolean.class),
        /**
         * Whether the NPC is pushable by fluids.
         */
        FLUID_PUSHABLE("fluid-pushable", Boolean.class),
        /**
         * Whether the NPC is 'flyable' i.e. will fly when pathfinding.
         */
        FLYABLE("flyable", Boolean.class),
        /** Forces a singular packet update. */
        FORCE_PACKET_UPDATE("force-packet-update", Boolean.class),
        /**
         * Whether the NPC is currently glowing.
         */
        GLOWING("glowing", Boolean.class),
        HOLOGRAM_RENDERER("hologram-renderer", TypeToken.of(Object.class), false),
        /**
         * The Minecraft sound to play when hurt.
         */
        HURT_SOUND("hurt-sound", String.class),
        /**
         * The Item amount.
         */
        ITEM_AMOUNT("item-type-amount", Integer.class),
        /**
         * The Item data.
         */
        ITEM_DATA("item-type-data", Byte.class),
        /**
         * The Item ID. String.
         */
        ITEM_ID("item-type-id", String.class),
        @SuppressWarnings("serial")
        JUMP_POWER_SUPPLIER("jump-power-supplier", new TypeToken<Function<NPC, Float>>() {
        }, false),
        /**
         * Whether to keep chunk loaded.
         */
        KEEP_CHUNK_LOADED("keep-chunk-loaded", Boolean.class),
        /** Simple knockback toggle. Not set by default. */
        KNOCKBACK("knockback", Boolean.class),
        /**
         * Whether the NPC is leashable.
         */
        LEASH_PROTECTED("protected-leash", Boolean.class),
        /**
         * The Minecart item offset as defined by Minecraft. {@link Minecart#setDisplayBlockOffset(int)}
         */
        MINECART_OFFSET("minecart-item-offset", Integer.class),
        /**
         * Whether the NPC's nameplate should be visible.
         */
        NAMEPLATE_VISIBLE("nameplate-visible", TypeToken.of(Boolean.class), false),
        /** Internal use only */
        NPC_SPAWNING_IN_PROGRESS("citizens-internal-spawning-npc", Boolean.class),
        /**
         * The packet update delay in ticks. Defaults to setting value.
         */
        PACKET_UPDATE_DELAY("packet-update-delay", Integer.class),
        /**
         * Whether to open doors while pathfinding.
         */
        PATHFINDER_OPEN_DOORS("pathfinder-open-doors", Boolean.class),
        /**
         * Whether to pick up items. Defaults to !isProtected().
         */
        PICKUP_ITEMS("pickup-items", Boolean.class),
        /**
         * Whether to remove players from the player list. Defaults to true.
         */
        REMOVE_FROM_PLAYERLIST("removefromplayerlist", Boolean.class),
        /** Whether to remove the NPC from the tablist. Defaults to the value in config.yml */
        REMOVE_FROM_TABLIST("removefromtablist", Boolean.class),
        /**
         * Whether to reset entity pitch to <code>0</code> every tick (default Minecraft behaviour). Defaults to false.
         */
        RESET_PITCH_ON_TICK("reset-pitch-on-tick", Boolean.class),
        /**
         * Whether to reset NPC yaw on spawn. Defaults to the config value (true by default).
         */
        RESET_YAW_ON_SPAWN("reset-yaw-on-spawn", Boolean.class),
        /**
         * The Integer delay to respawn in ticks after death. Only works if non-zero.
         */
        RESPAWN_DELAY("respawn-delay", Integer.class),
        /**
         * The fake NPC scoreboard team name because Minecraft requires a team name. Usually will be a random UUID in
         * String form.
         */
        SCOREBOARD_FAKE_TEAM_NAME("fake-scoreboard-team-name", String.class),
        /**
         * Whether to save / persist across server restarts.
         */
        SHOULD_SAVE("should-save", Boolean.class),
        /**
         * Whether to suppress sounds.
         */
        SILENT("silent-sounds", Boolean.class),
        /**
         * The initial no damage ticks on spawn, defaults to 20. Integer
         */
        SPAWN_NODAMAGE_TICKS("spawn-nodamage-ticks", Integer.class),
        /**
         * Whether to allow swimming. Boolean.
         */
        SWIM("swim", Boolean.class),
        TEXT_DISPLAY_COMPONENT("text-display-component", TypeToken.of(Component.class), false),
        /**
         * The tracking distance for packets. Defaults to the default tracking distance defined by the server
         */
        TRACKING_RANGE("tracking-distance", Integer.class),
        /**
         * Whether to use Minecraft AI.
         */
        USE_MINECRAFT_AI("minecraft-ai", Boolean.class),
        /**
         * Whether player is actively using held item. Defaults to false.
         */
        USING_HELD_ITEM("using-held-item", Boolean.class),
        /**
         * Whether player is actively using offhand item. Defaults to false.
         */
        USING_OFFHAND_ITEM("using-offhand-item", Boolean.class),
        /**
         * Whether to block Minecraft villager trades. Defaults to true.
         */
        VILLAGER_BLOCK_TRADES("villager-trades", Boolean.class),
        /**
         * Speed modifier in water, percentage.
         */
        WATER_SPEED_MODIFIER("water-speed-modifier", Float.class);

        private final String key;
        private final boolean strict;
        private final TypeToken<?> type;

        Metadata(String key, Class<?> type) {
            this(key, TypeToken.of(type), true);
        }

        Metadata(String key, TypeToken<?> type) {
            this(key, type, true);
        }

        Metadata(String key, TypeToken<?> type, boolean strict) {
            this.key = key;
            this.type = type;
            this.strict = strict;
        }

        public boolean accepts(Class<? extends Object> clazz) {
            return !strict || type.isSupertypeOf(clazz);
        }

        public String getKey() {
            return key;
        }

        public TypeToken<?> getType() {
            return type;
        }

        public static Metadata byKey(String name) {
            for (Metadata v : NPC.Metadata.values()) {
                if (v.key.equals(name))
                    return v;
            }
            return null;
        }

        public static Metadata byName(String name) {
            try {
                return valueOf(name);
            } catch (IllegalArgumentException iae) {
                return null;
            }
        }
    }

    public enum NPCUpdate {
        PACKET;
    }
}