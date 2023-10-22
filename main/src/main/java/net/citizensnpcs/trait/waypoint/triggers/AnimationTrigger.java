package net.citizensnpcs.trait.waypoint.triggers;

import java.util.Collection;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.util.PlayerAnimation;

public class AnimationTrigger implements WaypointTrigger {
    @Persist(required = true)
    private List<PlayerAnimation> animations = Lists.newArrayList();
    @Persist
    private Location at;

    public AnimationTrigger() {
    }

    public AnimationTrigger(Collection<PlayerAnimation> collection, Location loc) {
        animations = Lists.newArrayList(collection);
        at = loc;
    }

    @Override
    public String description() {
        return String.format("[[Animation]] animating %s", Joiner.on(", ").join(animations));
    }

    @Override
    public void onWaypointReached(NPC npc, Location waypoint) {
        if (npc.getEntity().getType() != EntityType.PLAYER)
            return;

        if (at != null) {
            npc.teleport(at, TeleportCause.PLUGIN);
        }

        Player player = (Player) npc.getEntity();
        for (PlayerAnimation animation : animations) {
            animation.play(player);
        }
    }
}
