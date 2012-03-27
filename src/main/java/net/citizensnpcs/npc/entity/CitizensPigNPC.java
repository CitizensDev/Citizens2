package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.editor.Equipable;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.npc.ai.NPCHandle;
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

    public CitizensPigNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityPigNPC.class);
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
        private final NPC npc;

        public EntityPigNPC(World world, NPC npc) {
            super(world);
            this.npc = npc;
            goalSelector = new PathfinderGoalSelector();
            targetSelector = new PathfinderGoalSelector();
        }

        @Override
        public void a(EntityWeatherLighting entityweatherlighting) {
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