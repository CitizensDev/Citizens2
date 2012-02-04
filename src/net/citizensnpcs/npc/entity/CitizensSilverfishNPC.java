package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensNPCManager;
import net.minecraft.server.EntitySilverfish;
import net.minecraft.server.World;

import org.bukkit.entity.Silverfish;

public class CitizensSilverfishNPC extends CitizensMobNPC {

    public CitizensSilverfishNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntitySilverfishNPC.class);
    }

    @Override
    public Silverfish getBukkitEntity() {
        return (Silverfish) getHandle().getBukkitEntity();
    }

    public static class EntitySilverfishNPC extends EntitySilverfish {

        public EntitySilverfishNPC(World world) {
            super(world);
        }

        @Override
        public void m_() {
        }
    }
}