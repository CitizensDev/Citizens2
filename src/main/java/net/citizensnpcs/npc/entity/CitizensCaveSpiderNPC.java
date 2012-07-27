package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.event.NPCCollisionEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.Util;
import net.minecraft.server.EntityCaveSpider;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.entity.CaveSpider;
import org.bukkit.util.Vector;

public class CitizensCaveSpiderNPC extends CitizensMobNPC {

    public CitizensCaveSpiderNPC(int id, String name) {
        super(id, name, EntityCaveSpiderNPC.class);
    }

    @Override
    public CaveSpider getBukkitEntity() {
        return (CaveSpider) getHandle().getBukkitEntity();
    }

    public static class EntityCaveSpiderNPC extends EntityCaveSpider implements NPCHolder {
        private final CitizensNPC npc;

        public EntityCaveSpiderNPC(World world, NPC npc) {
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
            npc.update();
        }
    }
}