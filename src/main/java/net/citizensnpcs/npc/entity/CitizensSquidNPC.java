package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.minecraft.server.EntitySquid;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.entity.Squid;

public class CitizensSquidNPC extends CitizensMobNPC {

    public CitizensSquidNPC(int id, String name) {
        super(id, name, EntitySquidNPC.class);
    }

    @Override
    public Squid getBukkitEntity() {
        return (Squid) getHandle().getBukkitEntity();
    }

    public static class EntitySquidNPC extends EntitySquid implements NPCHolder {
        private final CitizensNPC npc;

        private boolean pushable = false;

        public EntitySquidNPC(World world) {
            this(world, null);
        }

        public EntitySquidNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                goalSelector = new PathfinderGoalSelector();
                targetSelector = new PathfinderGoalSelector();
            }
        }

        @Override
        public void b_(double x, double y, double z) {
            if (npc == null || pushable)
                super.b_(x, y, z);
            // when another entity collides, b_ is called to push the NPC
            // so we prevent b_ from doing anything.
        }

        @Override
        public void d_() {
            if (npc != null)
                npc.update();
            else
                super.d_();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public boolean isPushable() {
            return pushable;
        }

        @Override
        public void setPushable(boolean pushable) {
            this.pushable = pushable;
        }
    }
}