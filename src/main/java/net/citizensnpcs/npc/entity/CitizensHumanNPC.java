package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.editor.Equipable;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.WorldServer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CitizensHumanNPC extends CitizensNPC implements Equipable {

    public CitizensHumanNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name);
    }

    @Override
    protected EntityLiving createHandle(Location loc) {
        WorldServer ws = ((CraftWorld) loc.getWorld()).getHandle();
        EntityHumanNPC handle = new EntityHumanNPC(ws.getServer().getServer(), ws,
                StringHelper.parseColors(getFullName()), new ItemInWorldManager(ws), this);
        handle.removeFromPlayerMap(getFullName());
        handle.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        return handle;
    }

    @Override
    public void equip(Player equipper) {
        ItemStack hand = equipper.getItemInHand();
        Equipment trait = getTrait(Equipment.class);
        int slot = 0;
        // First, determine the slot to edit
        switch (hand.getType()) {
        case PUMPKIN:
        case JACK_O_LANTERN:
        case LEATHER_HELMET:
        case CHAINMAIL_HELMET:
        case GOLD_HELMET:
        case IRON_HELMET:
        case DIAMOND_HELMET:
            if (!equipper.isSneaking())
                slot = 1;
            break;
        case LEATHER_CHESTPLATE:
        case CHAINMAIL_CHESTPLATE:
        case GOLD_CHESTPLATE:
        case IRON_CHESTPLATE:
        case DIAMOND_CHESTPLATE:
            if (!equipper.isSneaking())
                slot = 2;
            break;
        case LEATHER_LEGGINGS:
        case CHAINMAIL_LEGGINGS:
        case GOLD_LEGGINGS:
        case IRON_LEGGINGS:
        case DIAMOND_LEGGINGS:
            if (!equipper.isSneaking())
                slot = 3;
            break;
        case LEATHER_BOOTS:
        case CHAINMAIL_BOOTS:
        case GOLD_BOOTS:
        case IRON_BOOTS:
        case DIAMOND_BOOTS:
            if (!equipper.isSneaking())
                slot = 4;
            break;
        case AIR:
            for (int i = 0; i < 5; i++) {
                if (trait.get(i) != null && trait.get(i).getType() != Material.AIR) {
                    equipper.getWorld().dropItemNaturally(getBukkitEntity().getLocation(), trait.get(i));
                    trait.set(i, null);
                }
            }
            Messaging.send(equipper, "<e>" + getName() + " <a>had all of its items removed.");
        }
        // Now edit the equipment based on the slot
        if (trait.get(slot) != null && trait.get(slot).getType() != Material.AIR)
            equipper.getWorld().dropItemNaturally(getBukkitEntity().getLocation(), trait.get(slot));

        ItemStack set = hand;
        if (set != null && set.getType() != Material.AIR) {
            if (hand.getAmount() > 1)
                hand.setAmount(hand.getAmount() - 1);
            else
                hand = null;
            equipper.setItemInHand(hand);
            set.setAmount(1);
        }
        trait.set(slot, set);
    }

    @Override
    public Player getBukkitEntity() {
        return getHandle().getBukkitEntity();
    }

    @Override
    public EntityHumanNPC getHandle() {
        return (EntityHumanNPC) mcEntity;
    }

    @Override
    public void setName(String name) {
        super.setName(name);
        Location prev = getBukkitEntity().getLocation();
        despawn();
        spawn(prev);
    }

    @Override
    public void update() {
        super.update();
        if (isSpawned() && getBukkitEntity().getLocation().getChunk().isLoaded()) {
            mcEntity.move(0, -0.1, 0);
            // gravity! also works around an entity.onGround not updating issue
            // (onGround is normally updated by the client)
        }
    }
}