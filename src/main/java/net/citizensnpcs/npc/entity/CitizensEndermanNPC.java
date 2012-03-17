package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.editor.Equipable;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.npc.ai.NPCHandle;
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

    public CitizensEndermanNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityEndermanNPC.class);
    }

    @Override
    public Enderman getBukkitEntity() {
        return (Enderman) getHandle().getBukkitEntity();
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

    public static class EntityEndermanNPC extends EntityEnderman implements NPCHandle {
        private final NPC npc;

        @Override
        public NPC getNPC() {
            return npc;
        }

        public EntityEndermanNPC(World world, NPC npc) {
            super(world);
            this.npc = npc;
            goalSelector = new PathfinderGoalSelector();
            targetSelector = new PathfinderGoalSelector();
        }

        @Override
        public void d_() {
        }

        @Override
        public void e() {
        }
    }
}