package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.editor.Equipable;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.Messaging;
import net.minecraft.server.EntityEnderman;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.Material;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

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

    public static class EntityEndermanNPC extends EntityEnderman implements NPCHolder {
        private final CitizensNPC npc;

        public EntityEndermanNPC(World world) {
            this(world, null);
        }

        public EntityEndermanNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                goalSelector = new PathfinderGoalSelector();
                targetSelector = new PathfinderGoalSelector();
            }
        }

        @Override
        public void b_(double x, double y, double z) {
            // when another entity collides, b_ is called to push the NPC
            // so we prevent b_ from doing anything.
        }

        @Override
        public void d_() {
            if (npc == null)
                super.d_();
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