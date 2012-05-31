package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.abstraction.Equipment;
import net.citizensnpcs.api.abstraction.ItemStack;
import net.citizensnpcs.api.abstraction.Material;
import net.citizensnpcs.api.abstraction.World;
import net.citizensnpcs.api.abstraction.entity.Player;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.editor.Equipable;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.util.Messaging;

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
        return (Enderman) getHandle().getBukkitEntity();
    }

    public static class EntityEndermanNPC extends EntityEnderman implements NPCHandle {
        private final CitizensNPC npc;

        public EntityEndermanNPC(World world) {
            this(world, null);
        }

        public EntityEndermanNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            goalSelector = new PathfinderGoalSelector();
            targetSelector = new PathfinderGoalSelector();
        }

        @Override
        public void d_() {
        }

        @Override
        public void e() {
            if (npc != null)
                npc.update();
            else
                super.e();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}