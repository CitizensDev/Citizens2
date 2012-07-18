package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.editor.Equipable;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.trait.Saddle;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;
import net.minecraft.server.EntityPig;
import net.minecraft.server.EntityWeatherLighting;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.Material;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
                goalSelector = new PathfinderGoalSelector();
                targetSelector = new PathfinderGoalSelector();
            }
        }

        @Override
        public void a(EntityWeatherLighting entityweatherlighting) {
            if (npc == null)
                super.a(entityweatherlighting);
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

        @Override
        public void b_(double x, double y, double z) {
            // when another entity collides, b_ is called to push the NPC
            // so we prevent b_ from doing anything.
        }
    }
}