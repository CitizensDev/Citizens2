package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.editor.Equipable;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.trait.Saddle;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;
import net.citizensnpcs.util.Util;
import net.minecraft.server.EntityLightning;
import net.minecraft.server.EntityPig;
import net.minecraft.server.World;

import org.bukkit.Material;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class CitizensPigNPC extends CitizensMobNPC implements Equipable {

    public CitizensPigNPC(int id, String name) {
        super(id, name, EntityPigNPC.class);
    }

    @Override
    public void equip(Player equipper) {
        ItemStack hand = equipper.getItemInHand();
        if (hand.getType() == Material.SADDLE) {
            if (!getBukkitEntity().hasSaddle()) {
                getTrait(Saddle.class).toggle();
                equipper.setItemInHand(null);
                Messaging.send(equipper, StringHelper.wrap(getName()) + " is now saddled.");
            }
        } else {
            if (getBukkitEntity().hasSaddle()) {
                equipper.getWorld().dropItemNaturally(getBukkitEntity().getLocation(),
                        new ItemStack(Material.SADDLE, 1));
                getTrait(Saddle.class).toggle();
                Messaging.send(equipper, StringHelper.wrap(getName()) + " is no longer saddled.");
            }
        }
    }

    @Override
    public Pig getBukkitEntity() {
        return (Pig) getHandle().getBukkitEntity();
    }

    public static class EntityPigNPC extends EntityPig implements NPCHolder {
        private final CitizensNPC npc;

        public EntityPigNPC(World world) {
            this(world, null);
        }

        public EntityPigNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                Util.clearGoals(goalSelector, targetSelector);
            }
        }

        @Override
        public void a(EntityLightning entitylightning) {
            if (npc == null)
                super.a(entitylightning);
        }

        @Override
        public void bc() {
            super.bc();
            if (npc != null)
                npc.update();
        }

        @Override
        public void collide(net.minecraft.server.Entity entity) {
            // this method is called by both the entities involved - cancelling
            // it will not stop the NPC from moving.
            super.collide(entity);
            Util.callCollisionEvent(npc, entity);
        }

        @Override
        public void g(double x, double y, double z) {
            if (npc == null) {
                super.g(x, y, z);
                return;
            }
            if (NPCPushEvent.getHandlerList().getRegisteredListeners().length == 0)
                return;
            NPCPushEvent event = Util.callPushEvent(npc, new Vector(x, y, z));
            if (!event.isCancelled())
                super.g(x, y, z);
            // when another entity collides, this method is called to push the
            // NPC so we prevent it from doing anything if the event is
            // cancelled.
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}