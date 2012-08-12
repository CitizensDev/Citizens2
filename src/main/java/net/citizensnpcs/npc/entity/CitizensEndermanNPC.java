package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.editor.Equipable;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.NMSReflection;
import net.citizensnpcs.util.Util;
import net.minecraft.server.EntityEnderman;
import net.minecraft.server.World;

import org.bukkit.Material;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

public class CitizensEndermanNPC extends CitizensMobNPC implements Equipable {

    public CitizensEndermanNPC(int id, String name) {
        super(id, name, EntityEndermanNPC.class);
    }

    @Override
    public void equip(Player equipper) {
        ItemStack hand = equipper.getItemInHand();
        if (!hand.getType().isBlock()) {
            Messaging.sendError(equipper, "Invalid block!");
            return;
        }

        MaterialData carried = getBukkitEntity().getCarriedMaterial();
        if (carried.getItemType() == Material.AIR) {
            if (hand.getType() == Material.AIR) {
                Messaging.sendError(equipper, "Invalid block!");
                return;
            }
        } else {
            equipper.getWorld().dropItemNaturally(getBukkitEntity().getLocation(), carried.toItemStack(1));
            getBukkitEntity().setCarriedMaterial(hand.getData());
        }

        ItemStack set = hand;
        if (set.getType() != Material.AIR) {
            if (hand.getAmount() > 1)
                hand.setAmount(hand.getAmount() - 1);
            else
                hand = null;
            equipper.setItemInHand(hand);
            set.setAmount(1);
        }
        getTrait(Equipment.class).set(0, set);
    }

    @Override
    public Enderman getBukkitEntity() {
        return (Enderman) super.getBukkitEntity();
    }

    public static class EntityEndermanNPC extends EntityEnderman implements NPCHolder {
        private final CitizensNPC npc;

        public EntityEndermanNPC(World world) {
            this(world, null);
        }

        public EntityEndermanNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                NMSReflection.clearGoals(goalSelector, targetSelector);
            }
        }

        @Override
        public void bc() {
            if (npc == null)
                super.bc();
            else
                npc.update();
        }

        @Override
        public void be() {
            if (npc == null)
                super.be();
            else
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
        public void d() {
            if (npc == null)
                super.d();
        }

        @Override
        public void g(double x, double y, double z) {
            if (npc == null) {
                super.g(x, y, z);
                return;
            }
            if (NPCPushEvent.getHandlerList().getRegisteredListeners().length == 0)
                return;
            Vector vector = new Vector(x, y, z);
            NPCPushEvent event = Util.callPushEvent(npc, vector);
            if (!event.isCancelled()) {
                vector = event.getCollisionVector();
                super.g(vector.getX(), vector.getY(), vector.getZ());
            }
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