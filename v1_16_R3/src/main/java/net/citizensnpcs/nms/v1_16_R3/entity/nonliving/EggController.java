package net.citizensnpcs.nms.v1_16_R3.entity.nonliving;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEgg;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_16_R3.util.ForwardingNPCHolder;
import net.citizensnpcs.nms.v1_16_R3.util.NMSImpl;
import net.citizensnpcs.npc.AbstractEntityController;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_16_R3.EntityEgg;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.World;
import net.minecraft.server.v1_16_R3.WorldServer;

public class EggController extends AbstractEntityController {
    public EggController() {
        super(EntityEggNPC.class);
    }

    @Override
    protected Entity createEntity(Location at, NPC npc) {
        WorldServer ws = ((CraftWorld) at.getWorld()).getHandle();
        final EntityEggNPC handle = new EntityEggNPC(ws, npc, at.getX(), at.getY(), at.getZ());
        return handle.getBukkitEntity();
    }

    @Override
    public Egg getBukkitEntity() {
        return (Egg) super.getBukkitEntity();
    }

    public static class EggNPC extends CraftEgg implements ForwardingNPCHolder {
        public EggNPC(EntityEggNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
        }
    }

    public static class EntityEggNPC extends EntityEgg implements NPCHolder {
        private final CitizensNPC npc;

        public EntityEggNPC(EntityTypes<? extends EntityEgg> types, World world) {
            this(types, world, null);
        }

        public EntityEggNPC(EntityTypes<? extends EntityEgg> types, World world, NPC npc) {
            super(types, world);
            this.npc = (CitizensNPC) npc;
        }

        public EntityEggNPC(World world, NPC npc, double d0, double d1, double d2) {
            super(world, d0, d1, d2);
            this.npc = (CitizensNPC) npc;
        }

        @Override
        public void collide(net.minecraft.server.v1_16_R3.Entity entity) {
            // this method is called by both the entities involved - cancelling
            // it will not stop the NPC from moving.
            super.collide(entity);
            if (npc != null) {
                Util.callCollisionEvent(npc, entity.getBukkitEntity());
            }
        }

        @Override
        public boolean d(NBTTagCompound save) {
            return npc == null ? super.d(save) : false;
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(super.getBukkitEntity() instanceof NPCHolder)) {
                NMSImpl.setBukkitEntity(this, new EggNPC(this));
            }
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public void i(double x, double y, double z) {
            Vector vector = Util.callPushEvent(npc, x, y, z);
            if (vector != null) {
                super.i(vector.getX(), vector.getY(), vector.getZ());
            }
        }

        @Override
        public void tick() {
            if (npc != null) {
                npc.update();
                if (!npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true)) {
                    super.tick();
                }
            } else {
                super.tick();
            }
        }
    }
}
