package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.minecraft.server.EntityGiantZombie;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.entity.Giant;

public class CitizensGiantNPC extends CitizensMobNPC {

    public CitizensGiantNPC(int id, String name) {
        super(id, name, EntityGiantNPC.class);
    }

    @Override
    public Giant getBukkitEntity() {
        return (Giant) getHandle().getBukkitEntity();
    }

    public static class EntityGiantNPC extends EntityGiantZombie implements NPCHolder {
        private final CitizensNPC npc;

        private boolean pushable = false;

        public EntityGiantNPC(World world, NPC npc) {
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
            npc.update();
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