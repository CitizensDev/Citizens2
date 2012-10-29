package net.citizensnpcs.npc.entity;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.editor.Equipable;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.StringHelper;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.WorldServer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CitizensHumanNPC extends CitizensNPC implements Equipable {
    public CitizensHumanNPC(int id, String name) {
        super(id, name);
    }

    @Override
    protected EntityLiving createHandle(final Location loc) {
        WorldServer ws = ((CraftWorld) loc.getWorld()).getHandle();
        final EntityHumanNPC handle = new EntityHumanNPC(ws.getServer().getServer(), ws,
                StringHelper.parseColors(getFullName()), new ItemInWorldManager(ws), this);
        handle.getBukkitEntity().teleport(loc);
        Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
            @Override
            public void run() {
                NMS.setHeadYaw(handle, loc.getYaw() % 360);
                // set the head yaw in another tick - if done immediately,
                // minecraft will not update it.
                boolean removeFromPlayerList = Setting.REMOVE_PLAYERS_FROM_PLAYER_LIST.asBoolean();
                NMS.addOrRemoveFromPlayerList(getBukkitEntity(),
                        data().get("removefromplayerlist", removeFromPlayerList));
            }
        }, 5);
        handle.getBukkitEntity().setSleepingIgnored(true);
        return handle;
    }

    @Override
    public void equip(Player equipper, ItemStack hand) {
        Equipment trait = getTrait(Equipment.class);
        int slot = 0;
        Material type = hand == null ? Material.AIR : hand.getType();
        // First, determine the slot to edit
        switch (type) {
            case SKULL_ITEM:
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
                Messaging.sendTr(equipper, Messages.EQUIPMENT_EDITOR_ALL_ITEMS_REMOVED, getName());
                break;
            default:
                break;
        }
        // Drop any previous equipment on the ground
        ItemStack equippedItem = trait.get(slot);
        if (equippedItem != null && equippedItem.getType() != Material.AIR)
            equipper.getWorld().dropItemNaturally(getBukkitEntity().getLocation(), equippedItem);

        // Now edit the equipment based on the slot
        if (type != Material.AIR) {
            // Set the proper slot with one of the item
            ItemStack clone = hand.clone();
            clone.setAmount(1);
            trait.set(slot, clone);

            hand.setAmount(hand.getAmount() - 1);
        }
    }

    @Override
    public Player getBukkitEntity() {
        if (getHandle() == null)
            return null;
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
            if (NMS.inWater(mcEntity)) {
                mcEntity.motY += 0.08F;
            } else
                mcEntity.move(0, -0.2, 0);
            // gravity! also works around an entity.onGround not updating issue
            // (onGround is normally updated by the client)
        }
    }
}