package net.citizensnpcs.api.trait.trait;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.SaveId;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.ItemBuilder;

/**
 * Represents an NPC's armor. This only is applicable to human NPCs.
 */
@SaveId("armor")
public class Armor extends Trait {
    private final ItemStack[] armor = new ItemStack[4];
    private final NPC npc;

    public Armor(NPC npc) {
        this.npc = npc;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        if (key.keyExists("helmet"))
            armor[0] = ItemBuilder.getItemStack(key.getRelative("helmet"));
        if (key.keyExists("chestplate"))
            armor[1] = ItemBuilder.getItemStack(key.getRelative("chestplate"));
        if (key.keyExists("leggings"))
            armor[2] = ItemBuilder.getItemStack(key.getRelative("leggings"));
        if (key.keyExists("boots"))
            armor[3] = ItemBuilder.getItemStack(key.getRelative("boots"));
    }

    @Override
    public void save(DataKey key) {
        if (armor[0] != null)
            ItemBuilder.saveItem(armor[0], key.getRelative("helmet"));
        if (armor[1] != null)
            ItemBuilder.saveItem(armor[1], key.getRelative("chestplate"));
        if (armor[2] != null)
            ItemBuilder.saveItem(armor[2], key.getRelative("leggings"));
        if (armor[3] != null)
            ItemBuilder.saveItem(armor[3], key.getRelative("boots"));
    }

    /**
     * Get an NPC's armor from the given slot
     * 
     * @param slot
     *            Slot where the armor is located (0, 1, 2, or 3)
     * @return ItemStack from the given armor slot
     */
    public ItemStack getArmor(int slot) {
        if (slot < 0 || slot > 3)
            throw new IllegalArgumentException("Slot must be between 0 and 3");
        return armor[slot];
    }

    /**
     * Set the armor from the given slot as the given item
     * 
     * @param slot
     *            Slot of the armor
     * @param item
     *            Item to set the armor as
     */
    @SuppressWarnings("deprecation")
    public void setArmor(int slot, ItemStack item) {
        if (!(npc.getBukkitEntity() instanceof Player))
            throw new UnsupportedOperationException("Cannot set the armor of an NPC that is not a player.");

        Player player = (Player) npc.getBukkitEntity();
        switch (slot) {
        case 0:
            player.getInventory().setHelmet(item);
            break;
        case 1:
            player.getInventory().setChestplate(item);
            break;
        case 2:
            player.getInventory().setLeggings(item);
            break;
        case 3:
            player.getInventory().setBoots(item);
            break;
        default:
            throw new IllegalArgumentException("Slot must be between 0 and 3");
        }
        armor[slot] = item;
        player.updateInventory();
    }

    @Override
    public String toString() {
        return "{helmet=" + armor[0] + ",chestplate=" + armor[1] + ",leggings=" + armor[2] + ",boots=" + armor[3] + "}";
    }
}