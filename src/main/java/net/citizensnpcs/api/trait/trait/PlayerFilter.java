package net.citizensnpcs.api.trait.trait;

import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.google.common.collect.Sets;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("playerfilter")
public class PlayerFilter extends Trait {
    @Persist
    private Set<UUID> allowlist = null;
    @Persist
    private Set<String> groupAllowlist = null;
    @Persist
    private Set<String> groupHidden = null;
    @Persist
    private Set<UUID> hidden = null;
    private Function<Player, Boolean> hideFunction = (p) -> {
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

    public PlayerFilter() {
        super("playerfilter");
    }

    public void clear() {
        hidden = allowlist = null;
        groupAllowlist = groupHidden = null;
    }

    public void hide(UUID uuid) {
        if (hidden == null) {
            hidden = Sets.newHashSet();
        }
        hidden.add(uuid);
    }

    public void hideGroup(String group) {
        if (groupHidden == null) {
            groupHidden = Sets.newHashSet();
        }
        groupHidden.add(group);
    }

    public boolean isHidden(Player player) {
        if (hideFunction == null)
            return false;
        return hideFunction.apply(player);
    }

    public void only(UUID uuid) {
        if (allowlist == null) {
            allowlist = Sets.newHashSet();
        }
        allowlist.add(uuid);
    }

    public void onlyGroup(String group) {
        if (groupAllowlist == null) {
            groupAllowlist = Sets.newHashSet();
        }
        groupAllowlist.add(group);
    }

    public void setPlayerFilter(Function<Player, Boolean> filter) {
        this.hideFunction = filter;
    }

    public void unhide(UUID uuid) {
        if (hidden != null) {
            hidden.remove(uuid);
        }
        if (hidden.size() == 0) {
            hidden = null;
        }
        if (allowlist != null) {
            allowlist.remove(uuid);
        }
    }

    public void unhideGroup(String group) {
        if (groupHidden != null) {
            groupHidden.remove(group);
        }
        if (groupHidden.size() == 0) {
            groupHidden = null;
        }
        if (groupAllowlist != null) {
            groupAllowlist = null;
        }
    }
}
