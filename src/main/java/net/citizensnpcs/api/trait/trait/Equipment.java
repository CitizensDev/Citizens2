package net.citizensnpcs.api.trait.trait;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.ItemStorage;

import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Represents an NPC's equipment. This only is applicable to human and enderman
 * NPCs.
 */
public class Equipment extends Trait {
    private final ItemStack[] equipment = new ItemStack[5];

    public Equipment(NPC npc) {
        super("equipment");
    }

    /**
     * Get an NPC's equipment from the given slot.
     * 
     * @param slot
     *            Slot where the armor is located (0, 1, 2, 3, or 4)
     * @return ItemStack from the given armor slot
     */
    public ItemStack get(int slot) {
        if (npc.getBukkitEntity() instanceof Enderman && slot != 0)
            throw new IllegalArgumentException("Slot must be 0 for enderman");
        else if (npc.getBukkitEntity() instanceof Player && (slot < 0 || slot > 4))
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
            equipment[0] = ItemStorage.loadItemStack(key.getRelative("hand"));
        if (key.keyExists("helmet"))
            equipment[1] = ItemStorage.loadItemStack(key.getRelative("helmet"));
        if (key.keyExists("chestplate"))
            equipment[2] = ItemStorage.loadItemStack(key.getRelative("chestplate"));
        if (key.keyExists("leggings"))
            equipment[3] = ItemStorage.loadItemStack(key.getRelative("leggings"));
        if (key.keyExists("boots"))
            equipment[4] = ItemStorage.loadItemStack(key.getRelative("boots"));
    }

    @Override
    public void onNPCSpawn() {
        if (npc.getBukkitEntity() instanceof Enderman) {
            Enderman enderman = (Enderman) npc.getBukkitEntity();
            if (equipment[0] != null)
                enderman.setCarriedMaterial(equipment[0].getData());
        } else if (npc.getBukkitEntity() instanceof Player) {
            Player player = (Player) npc.getBukkitEntity();
            if (equipment[0] != null)
                player.setItemInHand(equipment[0]);
            ItemStack[] armor = { equipment[4], equipment[3], equipment[2], equipment[1] };
            // bukkit ordering is boots, leggings, chestplate, helmet
            player.getInventory().setArmorContents(armor);
        }
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
            ItemStorage.saveItem(key, item);
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
        if (npc.getBukkitEntity() instanceof Enderman) {
            if (slot != 0)
                throw new UnsupportedOperationException("Slot can only be 0 for enderman");

            ((Enderman) npc.getBukkitEntity()).setCarriedMaterial(item.getData());
        } else if (npc.getBukkitEntity() instanceof Player) {
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
            player.updateInventory();
        }
        equipment[slot] = item;
    }

    @Override
    public String toString() {
        return "{hand =" + equipment[0] + ",helmet=" + equipment[1] + ",chestplate=" + equipment[2] + ",leggings="
                + equipment[3] + ",boots=" + equipment[4] + "}";
    }
}