package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.event.NPCCollisionEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.Util;
import net.minecraft.server.EntitySlime;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.entity.Slime;
import org.bukkit.util.Vector;

public class CitizensSlimeNPC extends CitizensMobNPC {

    public CitizensSlimeNPC(int id, String name) {
        super(id, name, EntitySlimeNPC.class);
    }

    @Override
    public Slime getBukkitEntity() {
        return (Slime) getHandle().getBukkitEntity();
    }

    public static class EntitySlimeNPC extends EntitySlime implements NPCHolder {
        private final CitizensNPC npc;

        public EntitySlimeNPC(World world) {
            this(world, null);
        }

        public EntitySlimeNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                setSize(3);
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