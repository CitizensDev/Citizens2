package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.event.NPCCollisionEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.Util;
import net.minecraft.server.EntitySilverfish;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.entity.Silverfish;
import org.bukkit.util.Vector;

public class CitizensSilverfishNPC extends CitizensMobNPC {

    public CitizensSilverfishNPC(int id, String name) {
        super(id, name, EntitySilverfishNPC.class);
    }

    @Override
    public Silverfish getBukkitEntity() {
        return (Silverfish) getHandle().getBukkitEntity();
    }

    public static class EntitySilverfishNPC extends EntitySilverfish implements NPCHolder {
        private final CitizensNPC npc;

        public EntitySilverfishNPC(World world) {
            this(world, null);
        }

        public EntitySilverfishNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                goalSelector = new PathfinderGoalSelector();
                targetSelector = new PathfinderGoalSelector();
            }
        }

        @Override
        public void b_(double x, double y, double z) {
            if (npc == null) {
                super.b_(x, y, z);
                return;
            }
            if (NPCCollisionEvent.getHandlerList().getRegisteredListeners().length == 0)
                return;
            NPCCollisionEvent event = Util.callCollisionEvent(npc, new Vector(x, y, z));
            if (!event.isCancelled())
                super.b_(x, y, z);
            // when another entity collides, b_ is called to push the NPC
            // so we prevent b_ from doing anything.
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public void z_() {
            super.z_();
            if (npc != null)
                npc.update();
        }
    }
}