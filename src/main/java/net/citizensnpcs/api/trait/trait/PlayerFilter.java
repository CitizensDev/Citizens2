package net.citizensnpcs.api.trait.trait;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.google.common.collect.Sets;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPC.NPCUpdate;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.PermissionUtil;

@TraitName("playerfilter")
public class PlayerFilter extends Trait {
    @Persist
    private double applyRange = -1;
    private final Set<UUID> children = Sets.newHashSet();
    private Function<Player, Boolean> filter;
    @Persist
    private Set<String> groups = null;
    private final Set<UUID> hiddenPlayers = Sets.newHashSet();
    private BiConsumer<Player, Entity> hideFunction;
    @Persist
    private Mode mode = Mode.DENYLIST;
    @Persist
    private Set<String> permissions = null;
    @Persist
    private Set<UUID> players = null;
    private BiConsumer<Player, Entity> viewFunction;
    private final Set<UUID> viewingPlayers = Sets.newHashSet();

    public PlayerFilter() {
        super("playerfilter");
    }

    public PlayerFilter(BiConsumer<Player, Entity> hideFunction, BiConsumer<Player, Entity> viewFunction) {
        this();
        this.filter = p -> {
            if (applyRange != -1 && npc.isSpawned()) {
                if (p.getWorld() != npc.getEntity().getWorld())
                    return false;
                if (p.getLocation().distance(npc.getEntity().getLocation()) > applyRange)
                    return false;
            }
            switch (mode) {
                case DENYLIST:
                    if (players != null && players.contains(p.getUniqueId())
                            || groups != null && PermissionUtil.inGroup(groups, p) == true
                            || permissions != null && PermissionUtil.hasPermission(permissions, p))
                        return true;

                    break;
                case ALLOWLIST:
                    if (players != null && !players.contains(p.getUniqueId())
                            || groups != null && !PermissionUtil.inGroup(groups, p)
                            || permissions != null && !PermissionUtil.hasPermission(permissions, p))
                        return true;

                    break;
            }
            return false;
        };
        this.hideFunction = hideFunction;
        this.viewFunction = viewFunction;
    }

    public void addChildNPC(NPC npc) {
        children.add(npc.getUniqueId());
    }

    /**
     * Manages NPC hiding using the given permissions group
     */
    public void addGroup(String group) {
        if (groups == null) {
            groups = Sets.newHashSet();
        }
        groups.add(group);
        recalculate();
    }

    /**
     * Manages NPC hiding using the given permission
     */
    public void addPermission(String permission) {
        if (permissions == null) {
            permissions = Sets.newHashSet();
        }
        permissions.add(permission);
        recalculate();
    }

    /**
     * Manages NPC hiding from the provided UUID
     *
     * @param uuid
     */
    public void addPlayer(UUID uuid) {
        if (players == null) {
            players = Sets.newHashSet();
        }
        players.add(uuid);
        getSet().add(uuid);
        recalculate();
    }

    public boolean affectsGroup(String group) {
        return groups.contains(group);
    }

    public boolean affectsPlayer(UUID uuid) {
        return players.contains(uuid);
    }

    /**
     * Clears all set UUID filters.
     */
    public void clear() {
        players = null;
        groups = null;
        permissions = null;
    }

    public double getApplyRange() {
        return applyRange;
    }

    /**
     * Implementation detail: may change in the future.
     */
    public Set<String> getGroups() {
        return groups;
    }

    private Set<UUID> getInverseSet() {
        return mode == Mode.ALLOWLIST ? viewingPlayers : hiddenPlayers;
    }

    /**
     * Implementation detail: may change in the future.
     */
    public Set<String> getPermissions() {
        return permissions;
    }

    /**
     * Implementation detail: may change in the future.
     */
    public Set<UUID> getPlayerUUIDs() {
        return players;
    }

    private Set<UUID> getSet() {
        return mode == Mode.DENYLIST ? viewingPlayers : hiddenPlayers;
    }

    public boolean isAllowlist() {
        return mode == Mode.ALLOWLIST;
    }

    public boolean isDenylist() {
        return mode == Mode.DENYLIST;
    }

    /**
     * Whether the NPC should be hidden from the given Player
     */
    public boolean isHidden(Player player) {
        return filter == null ? false : filter.apply(player);
    }

    @Override
    public void onDespawn() {
        hiddenPlayers.clear();
        viewingPlayers.clear();
    }

    /**
     * For internal use. Method signature may be changed at any time.
     */
    public boolean onSeenByPlayer(Player player) {
        if (isHidden(player)) {
            this.hiddenPlayers.add(player.getUniqueId());
            return true;
        }
        this.viewingPlayers.add(player.getUniqueId());
        return false;
    }

    /**
     * Explicit recalculation of which {@link Player}s should be viewing the {@link NPC}. Sends hide packets for players
     * that should no longer view the NPC.
     */
    public void recalculate() {
        NPC[] npcs = children.stream().map(u -> CitizensAPI.getNPCRegistry().getByUniqueIdGlobal(u))
                .filter(n -> n != null).toArray(NPC[]::new);
        for (Iterator<UUID> itr = viewingPlayers.iterator(); itr.hasNext();) {
            UUID uuid = itr.next();
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                itr.remove();
                continue;
            }
            if (hideFunction != null && filter.apply(player)) {
                hideFunction.accept(player, npc.getEntity());
                for (NPC npc : npcs) {
                    hideFunction.accept(player, npc.getEntity());
                }
                itr.remove();
            }
        }
        for (Iterator<UUID> itr = hiddenPlayers.iterator(); itr.hasNext();) {
            UUID uuid = itr.next();
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                itr.remove();
                continue;
            }
            if (viewFunction != null && !filter.apply(player)) {
                viewFunction.accept(player, npc.getEntity());
                for (NPC npc : npcs) {
                    viewFunction.accept(player, npc.getEntity());
                }
                itr.remove();
            }
        }
    }

    /**
     * Unhides the given permissions group
     */
    public void removeGroup(String group) {
        if (groups != null) {
            groups.remove(group);
        }
        recalculate();
    }

    /**
     * Unhides the given permission
     */
    public void removePermission(String permission) {
        if (permissions != null) {
            permissions.remove(permission);
        }
        recalculate();
    }

    /**
     * Unhides the given Player UUID
     */
    public void removePlayer(UUID uuid) {
        if (players != null) {
            players.remove(uuid);
        }
        getInverseSet().add(uuid);
        recalculate();
    }

    @Override
    public void run() {
        if (!npc.isSpawned() || !npc.isUpdating(NPCUpdate.PACKET))
            return;
        recalculate();
    }

    public void setAllowlist() {
        this.mode = Mode.ALLOWLIST;
        recalculate();
    }

    /**
     * Sets the range in blocks where the filter applies. For example, if the range is 25 blocks and the Player is more
     * than 25 blocks away, the filter is ignored and the Player will not be hidden.
     *
     * @param range
     *            The new range
     */
    public void setApplyRange(double range) {
        this.applyRange = range;
        recalculate();
    }

    /**
     * Implementation detail: may change in the future.
     */
    public void setDenylist() {
        this.mode = Mode.DENYLIST;
        recalculate();
    }

    /**
     * Sets the filter function, which returns {@code true} if the {@link NPC} should be hidden from the given
     * {@link Player}.
     */
    public void setPlayerFilter(Function<Player, Boolean> filter) {
        this.filter = filter;
        recalculate();
    }

    /**
     * Implementation detail: may change in the future.
     */
    public void setPlayers(Set<UUID> players) {
        this.players = players == null ? null : Sets.newHashSet(players);
    }

    public enum Mode {
        ALLOWLIST,
        DENYLIST;
    }
}
