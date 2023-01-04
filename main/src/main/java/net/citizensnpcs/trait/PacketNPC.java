package net.citizensnpcs.trait;

import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.LocationLookup.PerPlayerMetadata;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.npc.EntityController;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.PlayerUpdateTask;

@TraitName("packet")
public class PacketNPC extends Trait {
    private EntityPacketTracker playerTracker;
    private boolean spawned = false;

    public PacketNPC() {
        super("packet");
    }

    @Override
    public void onSpawn() {
        playerTracker = NMS.getPlayerTracker(npc.getEntity());
        spawned = true;
    }

    @Override
    public void run() {
        if (!spawned)
            return;
        PerPlayerMetadata<Boolean> ppm = CitizensAPI.getLocationLookup().registerMetadata("packetnpc", null);
        for (Player nearby : CitizensAPI.getLocationLookup().getNearbyPlayers(npc.getStoredLocation(), 64)) {
            if (!ppm.has(nearby.getUniqueId(), npc.getUniqueId().toString())) {
                playerTracker.link(nearby);
                ppm.set(nearby.getUniqueId(), npc.getUniqueId().toString(), true);
            }
        }
        playerTracker.run();
    }

    public EntityController wrap(EntityController controller) {
        if (!(controller instanceof PacketController)) {
            return new PacketController(controller);
        }
        return controller;
    }

    public static interface EntityPacketTracker extends Runnable {
        public void link(Player player);

        public void unlinkAll(Consumer<Player> callback);

        public void unlink(Player player);
    }

    private class PacketController implements EntityController {
        private final EntityController base;

        public PacketController(EntityController controller) {
            this.base = controller;
        }

        @Override
        public void create(Location at, NPC npc) {
            base.create(at, npc);
        }

        @Override
        public void die() {
            base.die();
            if (!spawned)
                return;
            PlayerUpdateTask.deregisterPlayer(getBukkitEntity());
            PerPlayerMetadata<Boolean> ppm = CitizensAPI.getLocationLookup().registerMetadata("packetnpc", null);
            playerTracker.unlinkAll(player -> ppm.remove(player.getUniqueId(), npc.getUniqueId().toString()));
            spawned = false;
        }

        @Override
        public Entity getBukkitEntity() {
            return base.getBukkitEntity();
        }

        @Override
        public void remove() {
            if (!spawned)
                return;
            PlayerUpdateTask.deregisterPlayer(getBukkitEntity());
            PerPlayerMetadata<Boolean> ppm = CitizensAPI.getLocationLookup().registerMetadata("packetnpc", null);
            playerTracker.unlinkAll(player -> ppm.remove(player.getUniqueId(), npc.getUniqueId().toString()));
            base.remove();
            spawned = false;
        }

        @Override
        public boolean spawn(Location at) {
            base.getBukkitEntity().teleport(at);
            PlayerUpdateTask.registerPlayer(getBukkitEntity());
            return true;
        }
    }
}