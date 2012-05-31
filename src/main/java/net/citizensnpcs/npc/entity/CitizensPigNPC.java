package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.abstraction.ItemStack;
import net.citizensnpcs.api.abstraction.Material;
import net.citizensnpcs.api.abstraction.World;
import net.citizensnpcs.api.abstraction.entity.Player;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.editor.Equipable;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.trait.Saddle;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;

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

    public static class EntityPigNPC extends EntityPig implements NPCHandle {
        private final CitizensNPC npc;

        public EntityPigNPC(World world) {
            this(world, null);
        }

        public EntityPigNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            goalSelector = new PathfinderGoalSelector();
            targetSelector = new PathfinderGoalSelector();
        }

        @Override
        public void a(EntityWeatherLighting entityweatherlighting) {
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