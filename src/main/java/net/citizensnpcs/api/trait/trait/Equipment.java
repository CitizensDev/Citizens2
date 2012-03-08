package net.citizensnpcs.api.trait.trait;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.StorageUtils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Represents an NPC's equipment. This only is applicable to human NPCs.
 */
public class Equipment extends Trait {
    private final ItemStack[] equipment = new ItemStack[5];
    private final NPC npc;

    public Equipment(NPC npc) {
        this.npc = npc;
    }

    /**
     * Get an NPC's equipment from the given slot.
     * 
     * @param slot
     *            Slot where the armor is located (0, 1, 2, 3, or 4)
     * @return ItemStack from the given armor slot
     */
    public ItemStack get(int slot) {
        if (slot < 0 || slot > 4)
            throw new IllegalArgumentException("Slot must be between 0 and 4");
        return equipment[slot];
    }

    /**
     * Get all of an NPC's equipment.
     * 
     * @return An array of an NPC's equipment
     */
    public ItemStack[] getEquipment() {
        return equipment;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        if (key.keyExists("hand"))
            equipment[0] = StorageUtils.loadItemStack(key.getRelative("hand"));
        if (key.keyExists("helmet"))
            equipment[1] = StorageUtils.loadItemStack(key.getRelative("helmet"));
        if (key.keyExists("chestplate"))
            equipment[2] = StorageUtils.loadItemStack(key.getRelative("chestplate"));
        if (key.keyExists("leggings"))
            equipment[3] = StorageUtils.loadItemStack(key.getRelative("leggings"));
        if (key.keyExists("boots"))
            equipment[4] = StorageUtils.loadItemStack(key.getRelative("boots"));

        // Must set equipment after the NPC entity has been created
        Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("Citizens"), new Runnable() {
            @Override
            public void run() {
                if (npc.getBukkitEntity() instanceof Player) {
                    Player player = (Player) npc.getBukkitEntity();
                    if (equipment[0] != null)
                        player.setItemInHand(equipment[0]);
                    ItemStack[] armor = { equipment[1], equipment[2], equipment[3], equipment[4] };
                    player.getInventory().setArmorContents(armor);
                }
            }
        }, 1);
    }

    @Override
    public void save(DataKey key) {
        saveOrRemove(key.getRelative("hand"), equipment[0]);
        saveOrRemove(key.getRelative("helmet"), equipment[1]);
        saveOrRemove(key.getRelative("chestplate"), equipment[2]);
        saveOrRemove(key.getRelative("leggings"), equipment[3]);
        saveOrRemove(key.getRelative("boots"), equipment[4]);
    }

    private void saveOrRemove(DataKey key, ItemStack item) {
        if (item != null)
            StorageUtils.saveItem(key, item);
        else {
            if (key.keyExists(""))
                key.removeKey("");
        }
    }

    /**
     * Set the armor from the given slot as the given item.
     * 
     * @param slot
     *            Slot of the armor (must be between 0 and 4)
     * @param item
     *            Item to set the armor as
     */
    @SuppressWarnings("deprecation")
    public void set(int slot, ItemStack item) {
        if (!(npc.getBukkitEntity() instanceof Player))
            throw new UnsupportedOperationException("Only player NPCs can be equipped");

        Player player = (Player) npc.getBukkitEntity();
        switch (slot) {
        case 0:
            player.setItemInHand(item);
            break;
        case 1:
            player.getInventory().setHelmet(item);
            break;
        case 2:
            player.getInventory().setChestplate(item);
            break;
        case 3:
            player.getInventory().setLeggings(item);
            break;
        case 4:
            player.getInventory().setBoots(item);
            break;
        default:
            throw new IllegalArgumentException("Slot must be between 0 and 4");
        }
        equipment[slot] = item;
        player.updateInventory();
    }

    @Override
    public String toString() {
        return "{hand =" + equipment[0] + ",helmet=" + equipment[1] + ",chestplate=" + equipment[2] + ",leggings="
                + equipment[3] + ",boots=" + equipment[4] + "}";
    }
}