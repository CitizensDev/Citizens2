package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.abstraction.ItemStack;
import net.citizensnpcs.api.abstraction.Material;
import net.citizensnpcs.api.abstraction.World;
import net.citizensnpcs.api.abstraction.entity.Player;
import net.citizensnpcs.api.abstraction.entity.Sheep;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.editor.Equipable;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.trait.Sheared;
import net.citizensnpcs.trait.WoolColor;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;

public class CitizensSheepNPC extends CitizensMobNPC implements Equipable {

    public CitizensSheepNPC(int id, String name) {
        super(id, name, EntitySheepNPC.class);
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
        private final CitizensNPC npc;

        public EntitySheepNPC(World world) {
            this(world, null);
        }

        public EntitySheepNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            goalSelector = new PathfinderGoalSelector();
            targetSelector = new PathfinderGoalSelector();
        }

        @Override
        public void z_() {
            super.z_();
            if (npc != null)
                npc.update();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}