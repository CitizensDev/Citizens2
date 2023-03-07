package net.citizensnpcs.api.npc;

import java.util.UUID;
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
import net.citizensnpcs.api.util.DataKey;

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
    boolean despawn(DespawnReason reason);

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
     * @see #getItemProvider()
     */
    public Supplier<ItemStack> getItemProvider();

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
     *            Trait to get
     * @return Trait with the given name
     *
     * @deprecated for intransparent naming. Use {@link #getOrAddTrait(Class)} for the same behavior.
     */
    @Deprecated
    public <T extends Trait> T getTrait(Class<T> trait);

    /**
     * Gets a trait from the given class. If the NPC does not currently have the trait, <code>null</code> will be
     * returned.
     *
     * @param trait
     *            Trait to get
     * @return Trait with the given name
     */
    public <T extends Trait> T getTraitNullable(Class<T> trait);

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
     * Set the target movement destination location to walk towards using Minecraft movement. Should be set every tick.
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
     * A helper method for using {@link #DEFAULT_PROTECTED_METADATA} to set the NPC as protected or not protected from
     * damage/entity target events. Equivalent to
     * <code>npc.data().set(NPC.DEFAULT_PROTECTED_METADATA, isProtected);</code>
     *
     * @param isProtected
     *            Whether the NPC should be protected
     */
    public void setProtected(boolean isProtected);

    /**
     * Set the NPC to use Minecraft AI where possible. Note that the NPC may not always behave exactly like a Minecraft
     * mob would because of additional Citizens APIs.
     */
    public void setUseMinecraftAI(boolean use);

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

    void updateCustomName();

    /**
     * Whether the NPC is currently set to use Minecraft AI. Defaults to false.
     */
    public boolean useMinecraftAI();

    public enum Metadata {
        /** The activation range. Integer, defaults to the configured activation range. */
        ACTIVATION_RANGE("activation-range"),
        AGGRESSIVE("entity-aggressive"),
        ALWAYS_USE_NAME_HOLOGRAM("always-use-name-hologram"),
        /**
         * The Minecraft ambient sound played. String - Minecraft sound name
         */
        AMBIENT_SOUND("ambient-sound"),
        BOUNDING_BOX_FUNCTION("bounding-box-function"),
        /**
         * Whether the NPC is collidable with Players or not. Boolean.
         */
        COLLIDABLE("collidable"),
        /**
         * Whether the NPC can damage other Entities. Boolean.
         */
        DAMAGE_OTHERS("damage-others"),
        /**
         * The Minecraft sound played when the NPC dies. String - Minecraft sound name.
         */
        DEATH_SOUND("death-sound"),
        /**
         * Whether the NPC is 'protected' i.e. invulnerable to damage. Boolean.
         */
        DEFAULT_PROTECTED("protected"),
        DISABLE_DEFAULT_STUCK_ACTION("disable-default-stuck-action"),
        /**
         * Whether the NPC drops its inventory after death. Boolean.
         */
        DROPS_ITEMS("drops-items"),
        /**
         * Whether the NPC is pushable by fluids. Boolean.
         */
        FLUID_PUSHABLE("fluid-pushable"),
        /**
         * Whether the NPC is 'flyable' i.e. will fly when pathfinding. Boolean.
         */
        FLYABLE("flyable"),
        /** Forces a singular packet update. Boolean. */
        FORCE_PACKET_UPDATE("force-packet-update"),
        /**
         * Whether the NPC is currently glowing. Boolean.
         */
        GLOWING("glowing"),
        /**
         * The Minecraft sound to play when hurt. String - Minecraft sound name.
         */
        HURT_SOUND("hurt-sound"),
        /**
         * The Item amount. Integer.
         */
        ITEM_AMOUNT("item-type-amount"),
        /**
         * The Item data. Byte.
         */
        ITEM_DATA("item-type-data"),
        /**
         * The Item ID. String.
         */
        ITEM_ID("item-type-id"),
        /**
         * Whether to keep chunk loaded. Boolean.
         */
        KEEP_CHUNK_LOADED("keep-chunk-loaded"),
        /**
         * Whether the NPC is leashable. Boolean.
         */
        LEASH_PROTECTED("protected-leash"),
        /**
         * The Minecart item name.
         */
        MINECART_ITEM("minecart-item-name"),
        /**
         * The Minecart item data. Byte.
         */
        MINECART_ITEM_DATA("minecart-item-data"),

        /**
         * The Minecart item offset as defined by Minecraft. {@link Minecart#setDisplayBlockOffset(int)}
         */
        MINECART_OFFSET("minecart-item-offset"),
        /**
         * Whether the NPC's nameplate should be visible. Boolean.
         */
        NAMEPLATE_VISIBLE("nameplate-visible"),
        /**
         * The packet update delay. Integer defaults to setting value.
         */
        PACKET_UPDATE_DELAY("packet-update-delay"),
        /**
         * Whether to open doors while pathfinding. Boolean.
         */
        PATHFINDER_OPEN_DOORS("pathfinder-open-doors"),
        /**
         * Whether to pick up items. Boolean defaults to isProtected().
         */
        PICKUP_ITEMS("pickup-items"),
        /**
         * @see SkinTrait
         */
        @Deprecated
        PLAYER_SKIN_TEXTURE_PROPERTIES("player-skin-textures"),
        /**
         * @see SkinTrait
         */
        @Deprecated
        PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN("player-skin-signature"),
        /**
         * @see SkinTrait
         */
        @Deprecated
        PLAYER_SKIN_USE_LATEST("player-skin-use-latest-skin"),
        /**
         * @see SkinTrait
         */
        @Deprecated
        PLAYER_SKIN_UUID("player-skin-name"),
        /**
         * Whether to remove players from the player list. Boolean defaults to true.
         */
        REMOVE_FROM_PLAYERLIST("removefromplayerlist"),
        /**
         * The Integer delay to respawn in ticks after death. Only works if non-zero.
         */
        RESPAWN_DELAY("respawn-delay"),
        /**
         * The fake NPC scoreboard team name because Minecraft requires a team name. Usually will be a random UUID in
         * String form.
         */
        SCOREBOARD_FAKE_TEAM_NAME("fake-scoreboard-team-name"),
        /**
         * Whether to save / persist across server restarts. Boolean.
         */
        SHOULD_SAVE("should-save"),
        /**
         * Whether to suppress sounds. Boolean.
         */
        SILENT("silent-sounds"),
        /**
         * Whether to sneak. Boolean.
         */
        SNEAKING("citizens-sneaking"),
        /**
         * The initial no damage ticks on spawn, defaults to 20. Integer
         */
        SPAWN_NODAMAGE_TICKS("spawn-nodamage-ticks"),
        /**
         * Whether to allow swimming. Boolean.
         */
        SWIMMING("swim"),
        /**
         * Whether to prevent NPC being targeted by hostile mobs. Boolean.
         */
        TARGETABLE("protected-target"),
        /**
         * The tracking distance for packets. Integer, defaults to the default tracking distance defined by the server
         */
        TRACKING_RANGE("tracking-distance"),
        /**
         * Whether to use Minecraft AI. Boolean.
         */
        USE_MINECRAFT_AI("minecraft-ai"),
        /**
         * Whether player is actively using held item. Boolean defaults to false.
         */
        USING_HELD_ITEM("using-held-item"),
        /**
         * Whether player is actively using offhand item. Boolean defaults to false.
         */
        USING_OFFHAND_ITEM("using-offhand-item"),
        /**
         * Whether to block Minecraft villager trades. Boolean defaults to true.
         */
        VILLAGER_BLOCK_TRADES("villager-trades"),
        /**
         * Speed modifier in water, percentage.
         */
        WATER_SPEED_MODIFIER("water-speed-modifier");

        private final String key;

        Metadata(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
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