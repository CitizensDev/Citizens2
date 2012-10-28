package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.editor.Equipable;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.trait.Sheared;
import net.citizensnpcs.trait.WoolColor;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.EntitySheep;
import net.minecraft.server.World;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftSheep;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class CitizensSheepNPC extends CitizensMobNPC implements Equipable {
    public CitizensSheepNPC(int id, String name) {
        super(id, name, EntitySheepNPC.class);
    }

    @Override
    public void equip(Player equipper, ItemStack hand) {
        if (hand.getType() == Material.SHEARS) {
            Messaging.sendTr(equipper, getTrait(Sheared.class).toggle() ? Messages.SHEARED_SET
                    : Messages.SHEARED_STOPPED, getName());
        } else if (hand.getType() == Material.INK_SACK) {
            if (getBukkitEntity().getColor() == DyeColor.getByData((byte) (15 - hand.getData().getData())))
                return;

            DyeColor color = DyeColor.getByData((byte) (15 - hand.getData().getData()));
            getTrait(WoolColor.class).setColor(color);
            Messaging.sendTr(equipper, Messages.EQUIPMENT_EDITOR_SHEEP_COLOURED, getName(), color.name()
                    .toLowerCase().replace("_", " "));

            hand.setAmount(hand.getAmount() - 1);
        } else {
            getTrait(WoolColor.class).setColor(DyeColor.WHITE);
            Messaging.sendTr(equipper, Messages.EQUIPMENT_EDITOR_SHEEP_COLOURED, getName(), "white");
        }
    }

    @Override
    public Sheep getBukkitEntity() {
        return (Sheep) super.getBukkitEntity();
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
                NMS.clearGoals(goalSelector, targetSelector);
                NMS.setPersistent(this);
            }
        }

        @Override
        public void bi() {
            super.bi();
            if (npc != null)
                npc.update();
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
        public Entity getBukkitEntity() {
            if (bukkitEntity == null && npc != null)
                bukkitEntity = new SheepNPC(this);
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }

    public static class SheepNPC extends CraftSheep implements NPCHolder {
        private final CitizensNPC npc;

        public SheepNPC(EntitySheepNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}