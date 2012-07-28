package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.editor.Equipable;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.trait.Sheared;
import net.citizensnpcs.trait.WoolColor;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;
import net.citizensnpcs.util.Util;
import net.minecraft.server.EntitySheep;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

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
            Messaging.send(equipper, StringHelper.wrap(getName()) + " is now " + StringHelper.wrap("white")
                    + ".");
        }
    }

    @Override
    public Sheep getBukkitEntity() {
        return (Sheep) getHandle().getBukkitEntity();
    }

    public static class EntitySheepNPC extends EntitySheep implements NPCHolder {
        private final CitizensNPC npc;

        public EntitySheepNPC(World world) {
            this(world, null);
        }

        public EntitySheepNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                goalSelector = new PathfinderGoalSelector();
                targetSelector = new PathfinderGoalSelector();
            }
        }

        @Override
        public void b_(double x, double y, double z) {
            if (npc == null) {
                super.b_(x, y, z);
                return;
            }
            if (NPCPushEvent.getHandlerList().getRegisteredListeners().length == 0)
                return;
            NPCPushEvent event = Util.callPushEvent(npc, new Vector(x, y, z));
            if (!event.isCancelled())
                super.b_(x, y, z);
            // when another entity collides, b_ is called to push the NPC
            // so we prevent b_ from doing anything if the event is cancelled.
        }

        @Override
        public void collide(net.minecraft.server.Entity entity) {
            // this method is called by both the entities involved - cancelling
            // it will not stop the NPC from moving.
            super.collide(entity);
            Util.callCollisionEvent(npc, entity);
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public void z_() {
            super.z_();
            if (npc != null)
                npc.update();
        }
    }
}