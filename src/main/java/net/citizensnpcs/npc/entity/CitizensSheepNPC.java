package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.editor.Equipable;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.npc.ai.NPCHandle;
import net.citizensnpcs.trait.Sheared;
import net.citizensnpcs.trait.WoolColor;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;
import net.minecraft.server.EntitySheep;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.ItemStack;

public class CitizensSheepNPC extends CitizensMobNPC implements Equipable {

    public CitizensSheepNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntitySheepNPC.class);
    }

    @Override
    public void equip(Player equipper) {
        ItemStack hand = equipper.getItemInHand();
        if (hand.getType() == Material.SHEARS) {
            Messaging.send(equipper, StringHelper.wrap(getName()) + " is "
                    + (getTrait(Sheared.class).toggle() ? "now" : "no longer") + " sheared.");
        } else if (hand.getType() == Material.INK_SACK) {
            if (getBukkitEntity().getColor() == DyeColor.getByData((byte) (15 - hand.getData().getData())))
                return;

            DyeColor color = DyeColor.getByData((byte) (15 - hand.getData().getData()));
            getTrait(WoolColor.class).setColor(color);
            Messaging.send(
                    equipper,
                    StringHelper.wrap(getName()) + " is now "
                            + StringHelper.wrap(color.name().toLowerCase().replace("_", " ")) + ".");

            if (hand.getAmount() > 1)
                hand.setAmount(hand.getAmount() - 1);
            else
                hand = null;
            equipper.setItemInHand(hand);
        } else {
            getTrait(WoolColor.class).setColor(DyeColor.WHITE);
            Messaging.send(equipper, StringHelper.wrap(getName()) + " is now " + StringHelper.wrap("white") + ".");
        }
    }

    @Override
    public Sheep getBukkitEntity() {
        return (Sheep) getHandle().getBukkitEntity();
    }

    public static class EntitySheepNPC extends EntitySheep implements NPCHandle {
        private final NPC npc;

        public EntitySheepNPC(World world, NPC npc) {
            super(world);
            this.npc = npc;
            goalSelector = new PathfinderGoalSelector();
            targetSelector = new PathfinderGoalSelector();
        }

        @Override
        public void d_() {
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}