package net.citizensnpcs.api.trait.trait;

import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import org.bukkit.entity.Player;

import com.google.common.collect.Sets;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("playerfilter")
public class PlayerFilter extends Trait {
    @Persist
    private Set<UUID> allowlist = null;
    @Persist
    private Set<UUID> hidden = null;
    private Function<Player, Boolean> hideFunction = (p) -> {
        if (allowlist != null && !allowlist.contains(p.getUniqueId()))
            return true;
        if (hidden != null && hidden.contains(p.getUniqueId()))
            return true;
        return false;
    };

    public PlayerFilter() {
        super("playerfilter");
    }

    public void clear() {
        hidden = allowlist = null;
    }

    public void hide(UUID uuid) {
        if (hidden == null) {
            hidden = Sets.newHashSet();
        }
        hidden.add(uuid);
    }

    public boolean isHidden(Player player) {
        return hideFunction == null ? false : hideFunction.apply(player);
    }

    public void only(UUID uuid) {
        if (allowlist == null) {
            allowlist = Sets.newHashSet();
        }
        allowlist.add(uuid);
    }

    public void setPlayerFilter(Function<Player, Boolean> filter) {
        this.hideFunction = filter;
    }

    public void unhide(UUID uuid) {
        if (hidden != null) {
            hidden.remove(uuid);
        }
        if (allowlist != null) {
            allowlist.remove(uuid);
        }
    }
}
