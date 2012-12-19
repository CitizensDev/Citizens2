package net.citizensnpcs.api.trait.trait;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.ItemStorage;

import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

/**
 * Represents an NPC's equipment. This only is applicable to human and enderman
 * NPCs.
 */
public class Equipment extends Trait {
    private final ItemStack[] equipment = new ItemStack[5];

    public Equipment() {
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
        else if (slot < 0 || slot > 4)
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

    private EntityEquipment getEquipmentFromEntity(LivingEntity entity) {
        if (entity instanceof Player)
            return new PlayerEquipmentWrapper((Player) entity);
        return entity.getEquipment();
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
    @SuppressWarnings("deprecation")
    public void onSpawn() {
        if (npc.getBukkitEntity() instanceof Enderman) {
            Enderman enderman = (Enderman) npc.getBukkitEntity();
            if (equipment[0] != null)
                enderman.setCarriedMaterial(equipment[0].getData());
        } else {
            LivingEntity entity = npc.getBukkitEntity();
            EntityEquipment equip = getEquipmentFromEntity(entity);
            if (equipment[0] != null)
                equip.setItemInHand(equipment[0]);
            equip.setHelmet(equipment[1]);
            equip.setChestplate(equipment[2]);
            equip.setLeggings(equipment[3]);
            equip.setBoots(equipment[4]);
            if (entity instanceof Player)
                ((Player) entity).updateInventory();
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
        } else {
            EntityEquipment equip = getEquipmentFromEntity(npc.getBukkitEntity());
            switch (slot) {
                case 0:
                    equip.setItemInHand(item);
                    break;
                case 1:
                    equip.setHelmet(item);
                    break;
                case 2:
                    equip.setChestplate(item);
                    break;
                case 3:
                    equip.setLeggings(item);
                    break;
                case 4:
                    equip.setBoots(item);
                    break;
                default:
                    throw new IllegalArgumentException("Slot must be between 0 and 4");
            }
            if (npc.getBukkitEntity() instanceof Player)
                ((Player) npc.getBukkitEntity()).updateInventory();
        }
        equipment[slot] = item;
    }

    @Override
    public String toString() {
        return "{hand=" + equipment[0] + ",helmet=" + equipment[1] + ",chestplate=" + equipment[2]
                + ",leggings=" + equipment[3] + ",boots=" + equipment[4] + "}";
    }

    private static class PlayerEquipmentWrapper implements EntityEquipment {
        private final Player player;

        private PlayerEquipmentWrapper(Player player) {
            this.player = player;
        }

        @Override
        public void clear() {
            player.getInventory().clear();
        }

        @Override
        public ItemStack[] getArmorContents() {
            return player.getInventory().getArmorContents();
        }

        @Override
        public ItemStack getBoots() {
            return player.getInventory().getBoots();
        }

        @Override
        public float getBootsDropChance() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ItemStack getChestplate() {
            return player.getInventory().getChestplate();
        }

        @Override
        public float getChestplateDropChance() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ItemStack getHelmet() {
            return player.getInventory().getHelmet();
        }

        @Override
        public float getHelmetDropChance() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Entity getHolder() {
            return player;
        }

        @Override
        public ItemStack getItemInHand() {
            return player.getItemInHand();
        }

        @Override
        public float getItemInHandDropChance() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ItemStack getLeggings() {
            return player.getInventory().getLeggings();
        }

        @Override
        public float getLeggingsDropChance() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setArmorContents(ItemStack[] items) {
            player.getInventory().setArmorContents(items);
        }

        @Override
        public void setBoots(ItemStack boots) {
            player.getInventory().setBoots(boots);
        }

        @Override
        public void setBootsDropChance(float chance) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setChestplate(ItemStack chestplate) {
            player.getInventory().setChestplate(chestplate);
        }

        @Override
        public void setChestplateDropChance(float chance) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setHelmet(ItemStack helmet) {
            player.getInventory().setHelmet(helmet);
        }

        @Override
        public void setHelmetDropChance(float chance) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setItemInHand(ItemStack stack) {
            player.setItemInHand(stack);
        }

        @Override
        public void setItemInHandDropChance(float chance) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setLeggings(ItemStack leggings) {
            player.getInventory().setLeggings(leggings);
        }

        @Override
        public void setLeggingsDropChance(float chance) {
            throw new UnsupportedOperationException();
        }
    }
}