package net.citizensnpcs.api.trait.trait;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.google.common.collect.Sets;

import net.citizensnpcs.api.npc.NPC.NPCUpdate;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("playerfilter")
public class PlayerFilter extends Trait {
    @Persist
    private Set<UUID> allowlist = null;
    private Function<Player, Boolean> filter;
    @Persist
    private Set<String> groupAllowlist = null;
    @Persist
    private Set<String> groupHidden = null;
    @Persist
    private Set<UUID> hidden = null;
    private final Set<UUID> hiddenPlayers = Sets.newHashSet();
    private BiConsumer<Player, Entity> hideFunction;
    private BiConsumer<Player, Entity> viewFunction;
    private final Set<UUID> viewingPlayers = Sets.newHashSet();

    public PlayerFilter() {
        super("playerfilter");
    }

    public PlayerFilter(BiConsumer<Player, Entity> hideFunction, BiConsumer<Player, Entity> viewFunction) {
        this();
        this.filter = p -> {
            if (allowlist != null && !allowlist.contains(p.getUniqueId()))
                return true;
            if (hidden != null && hidden.contains(p.getUniqueId()))
                return true;
            if (groupAllowlist != null || groupHidden != null) {
                RegisteredServiceProvider<net.milkbowl.vault.permission.Permission> groups = Bukkit.getServicesManager()
                        .getRegistration(net.milkbowl.vault.permission.Permission.class);
                if (groups != null
                        && !groupAllowlist.stream().anyMatch(group -> groups.getProvider().playerInGroup(p, group))) {
                    return true;
                }
                if (groups != null
                        && groupHidden.stream().anyMatch(group -> groups.getProvider().playerInGroup(p, group))) {
                    return true;
                }
            }
            return false;
        };
        this.hideFunction = hideFunction;
        this.viewFunction = viewFunction;
    }

    /**
     * Clears all set UUID filters.
     */
    public void clear() {
        hidden = allowlist = null;
        groupAllowlist = groupHidden = null;
    }

    /**
     * Implementation detail: may change in the future.
     */
    public Set<UUID> getAllowlist() {
        return allowlist;
    }

    /**
     * Implementation detail: may change in the future.
     */
    public Set<String> getGroupAllowlist() {
        return groupAllowlist;
    }

    /**
     * Implementation detail: may change in the future.
     */
    public Set<String> getGroupHidden() {
        return groupHidden;
    }

    /**
     * Implementation detail: may change in the future.
     */
    public Set<UUID> getHidden() {
        return hidden;
    }

    /**
     * Hides the NPC from the given Player UUID.
     *
     * @param uuid
     */
    public void hide(UUID uuid) {
        if (hidden == null) {
            hidden = Sets.newHashSet();
        }
        hidden.add(uuid);
        viewingPlayers.add(uuid);
        recalculate();
    }

    /**
     * Hides the NPC from the given permissions group
     */
    public void hideGroup(String group) {
        if (groupHidden == null) {
            groupHidden = Sets.newHashSet();
        }
        groupHidden.add(group);
        recalculate();
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
     * Only the given {@link Player} identified by their {@link UUID} should see the {@link NPC}.
     */
    public void only(UUID uuid) {
        if (allowlist == null) {
            allowlist = Sets.newHashSet();
        }
        allowlist.add(uuid);
        recalculate();
    }

    /**
     * Only the given permissions group should see the NPC.
     */
    public void onlyGroup(String group) {
        if (groupAllowlist == null) {
            groupAllowlist = Sets.newHashSet();
        }
        groupAllowlist.add(group);
        recalculate();
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
        for (Iterator<UUID> itr = viewingPlayers.iterator(); itr.hasNext();) {
            UUID uuid = itr.next();
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                itr.remove();
                continue;
            }
            if (hideFunction != null && filter.apply(player)) {
                hideFunction.accept(player, npc.getEntity());
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
                itr.remove();
            }
        }
    }

    @Override
    public void run() {
        if (!npc.isSpawned() || !npc.isUpdating(NPCUpdate.PACKET))
            return;
        recalculate();
    }

    /**
     * Implementation detail: may change in the future.
     */
    public void setAllowlist(Set<UUID> allowlist) {
        this.allowlist = allowlist == null ? null : Sets.newHashSet(allowlist);
    }

    /**
     * Implementation detail: may change in the future.
     */

    public void setHiddenFrom(Set<UUID> hidden) {
        this.hidden = hidden == null ? null : Sets.newHashSet(hidden);
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
     * Unhides the given Player UUID
     */
    public void unhide(UUID uuid) {
        if (hidden != null) {
            hidden.remove(uuid);
        }
        if (allowlist != null) {
            allowlist.remove(uuid);
        }
        hiddenPlayers.add(uuid);
        recalculate();
    }

    /**
     * Unhides the given permissions group
     */
    public void unhideGroup(String group) {
        if (groupHidden != null) {
            groupHidden.remove(group);
        }
        if (groupAllowlist != null) {
            groupAllowlist = null;
        }
        recalculate();
    }
}
