package net.citizensnpcs.trait;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.EntityController;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.PlayerUpdateTask;

@TraitName("packet")
public class PacketNPC extends Trait {
    private EntityPacketTracker playerTracker;

    public PacketNPC() {
        super("packet");
    }

    @Override
    public void onSpawn() {
        playerTracker = NMS.getPlayerTracker(npc.getEntity());
    }

    @Override
    public void run() {
        wrap((CitizensNPC) npc);
        if (!npc.isSpawned())
            return;
        for (Player nearby : CitizensAPI.getLocationLookup().getNearbyPlayers(npc.getStoredLocation(), 64)) {
            if (!nearby.hasMetadata(npc.getUniqueId().toString())) {
                playerTracker.link(nearby);
                nearby.setMetadata(npc.getUniqueId().toString(), new FixedMetadataValue(CitizensAPI.getPlugin(), true));
            }
        }
        playerTracker.run();
    }

    private void wrap(CitizensNPC npc) {
        if (!(npc.getEntityController() instanceof PacketController)) {
            npc.setEntityController(new PacketController(npc.getEntityController()));
        }
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
        }

        @Override
        public Entity getBukkitEntity() {
            return base.getBukkitEntity();
        }

        @Override
        public void remove() {
            PlayerUpdateTask.deregisterPlayer(getBukkitEntity());
            playerTracker.remove();
            base.remove();
        }

        @Override
        public boolean spawn(Location at) {
            base.getBukkitEntity().teleport(at);
            PlayerUpdateTask.registerPlayer(getBukkitEntity());
            return true;
        }
    }

    public static interface EntityPacketTracker extends Runnable {
        public void link(Player player);

        public void remove();

        public void unlink(Player player);
    }
}