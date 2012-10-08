package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.editor.Equipable;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.NMS;
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
    public void equip(Player equipper, ItemStack hand) {
        if (!hand.getType().isBlock()) {
            Messaging.sendErrorTr(equipper, Messages.EQUIPMENT_EDITOR_INVALID_BLOCK);
            return;
        }

        MaterialData carried = getBukkitEntity().getCarriedMaterial();
        if (carried.getItemType() == Material.AIR) {
            if (hand.getType() == Material.AIR) {
                Messaging.sendErrorTr(equipper, Messages.EQUIPMENT_EDITOR_INVALID_BLOCK);
                return;
            }
        } else {
            equipper.getWorld().dropItemNaturally(getBukkitEntity().getLocation(), carried.toItemStack(1));
            getBukkitEntity().setCarriedMaterial(hand.getData());
        }

        ItemStack set = hand;
        if (set.getType() != Material.AIR) {
            hand.setAmount(hand.getAmount() - 1);
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
                NMS.clearGoals(goalSelector, targetSelector);
            }
        }

        @Override
        public void bb() {
            if (npc == null)
                super.bb();
            // check despawn method, we only want to despawn on chunk unload.
        }

        @Override
        public void bc() {
            super.bc();
            if (npc != null)
                npc.update();
        }

        @Override
        public void be() {
            if (npc == null)
                super.be();
            else {
                NMS.updateAI(this);
                npc.update();
            }
        }

        @Override
        public void collide(net.minecraft.server.Entity entity) {
            // this method is called by both the entities involved - cancelling
            // it will not stop the NPC from moving.
            super.collide(entity);
            if (npc != null)
                Util.callCollisionEvent(npc, entity);
        }

        @Override
        public void d() {
            if (npc == null)
                super.d();
            else {
                NMS.updateAI(this);
                npc.update();
            }
        }

        @Override
        public void g(double x, double y, double z) {
            if (npc == null) {
                super.g(x, y, z);
                return;
            }
            if (NPCPushEvent.getHandlerList().getRegisteredListeners().length == 0) {
                if (!npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true))
                    super.g(x, y, z);
                return;
            }
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