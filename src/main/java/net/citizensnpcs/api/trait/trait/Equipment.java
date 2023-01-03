package net.citizensnpcs.api.trait.trait;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Maps;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC.NPCUpdate;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.ItemStorage;
import net.citizensnpcs.api.util.SpigotUtil;

/**
 * Represents an NPC's equipment.
 */
@TraitName("equipment")
public class Equipment extends Trait {
    private final ItemStack[] equipment = new ItemStack[6];

    public Equipment() {
        super("equipment");
    }

    private ItemStack clone(ItemStack item) {
        return item == null || item.getType() == Material.AIR ? null : item.clone();
    }

    /**
     * @see #get(int)
     */
    public ItemStack get(EquipmentSlot slot) {
        return get(slot.getIndex());
    }

    /**
     * Get an NPC's equipment from the given slot.
     *
     * @param slot
     *            Slot where the armor is located (0-5)
     * @return ItemStack from the given armor slot
     */
    public ItemStack get(int slot) {
        if (npc.getEntity() instanceof Enderman && slot != 0)
            throw new IllegalArgumentException("Slot must be 0 for enderman");
        else if (slot < 0 || slot > 5)
            throw new IllegalArgumentException("Slot must be between 0 and 5");

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

    /**
     * Get all of the equipment as a {@link Map}.
     *
     * @return A mapping of slot to item
     */
    public Map<EquipmentSlot, ItemStack> getEquipmentBySlot() {
        Map<EquipmentSlot, ItemStack> map = Maps.newEnumMap(EquipmentSlot.class);
        map.put(EquipmentSlot.HAND, equipment[0]);
        map.put(EquipmentSlot.HELMET, equipment[1]);
        map.put(EquipmentSlot.CHESTPLATE, equipment[2]);
        map.put(EquipmentSlot.LEGGINGS, equipment[3]);
        map.put(EquipmentSlot.BOOTS, equipment[4]);
        map.put(EquipmentSlot.OFF_HAND, equipment[5]);
        return map;
    }

    private EntityEquipment getEquipmentFromEntity(Entity entity) {
        if (entity instanceof LivingEntity) {
            return ((LivingEntity) entity).getEquipment();
        }
        throw new RuntimeException("Unsupported entity equipment");
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        if (key.keyExists("hand")) {
            equipment[0] = ItemStorage.loadItemStack(key.getRelative("hand"));
        }
        if (key.keyExists("helmet")) {
            equipment[1] = ItemStorage.loadItemStack(key.getRelative("helmet"));
        }
        if (key.keyExists("chestplate")) {
            equipment[2] = ItemStorage.loadItemStack(key.getRelative("chestplate"));
        }
        if (key.keyExists("leggings")) {
            equipment[3] = ItemStorage.loadItemStack(key.getRelative("leggings"));
        }
        if (key.keyExists("boots")) {
            equipment[4] = ItemStorage.loadItemStack(key.getRelative("boots"));
        }
        if (key.keyExists("offhand")) {
            equipment[5] = ItemStorage.loadItemStack(key.getRelative("offhand"));
        }
    }

    @Override
    public void onAttach() {
        npc.scheduleUpdate(NPCUpdate.PACKET);
        run();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onSpawn() {
        if (!(npc.getEntity() instanceof LivingEntity) && !(npc.getEntity() instanceof ArmorStand))
            return;
        if (npc.getEntity() instanceof Enderman) {
            Enderman enderman = (Enderman) npc.getEntity();
            if (equipment[0] != null) {
                if (SpigotUtil.isUsing1_13API()) {
                    enderman.setCarriedBlock(equipment[0].getType().createBlockData());
                } else {
                    enderman.setCarriedMaterial(equipment[0].getData());
                }
            }
        } else {
            EntityEquipment equip = getEquipmentFromEntity(npc.getEntity());
            if (equipment[0] != null) {
                equip.setItemInHand(equipment[0]);
            }
            equip.setHelmet(equipment[1]);
            equip.setChestplate(equipment[2]);
            equip.setLeggings(equipment[3]);
            equip.setBoots(equipment[4]);
            try {
                equip.setItemInOffHand(equipment[5]);
            } catch (NoSuchMethodError e) {
            }
        }
        if (npc.getEntity() instanceof Player) {
            ((Player) npc.getEntity()).updateInventory();
        }
    }

    @Override
    public void run() {
        if (!(npc.getEntity() instanceof LivingEntity) || !npc.isUpdating(NPCUpdate.PACKET))
            return;
        if (npc.getEntity() instanceof Enderman) {
            Enderman enderman = (Enderman) npc.getEntity();
            if (equipment[0] != null) {
                if (SpigotUtil.isUsing1_13API()) {
                    equipment[0] = new ItemStack(enderman.getCarriedBlock().getMaterial(), 1);
                } else {
                    equipment[0] = enderman.getCarriedMaterial().toItemStack(1);
                }
            }
        } else {
            EntityEquipment equip = getEquipmentFromEntity(npc.getEntity());
            equipment[0] = clone(equip.getItemInHand());
            equipment[1] = clone(equip.getHelmet());
            equipment[2] = clone(equip.getChestplate());
            equipment[3] = clone(equip.getLeggings());
            equipment[4] = clone(equip.getBoots());
            if (SUPPORT_OFFHAND) {
                try {
                    equipment[5] = clone(equip.getItemInOffHand());
                } catch (NoSuchMethodError e) {
                    SUPPORT_OFFHAND = false;
                }
            }
        }
    }

    @Override
    public void save(DataKey key) {
        saveOrRemove(key.getRelative("hand"), equipment[0]);
        saveOrRemove(key.getRelative("helmet"), equipment[1]);
        saveOrRemove(key.getRelative("chestplate"), equipment[2]);
        saveOrRemove(key.getRelative("leggings"), equipment[3]);
        saveOrRemove(key.getRelative("boots"), equipment[4]);
        saveOrRemove(key.getRelative("offhand"), equipment[5]);
    }

    private void saveOrRemove(DataKey key, ItemStack item) {
        if (item != null) {
            ItemStorage.saveItem(key, item);
        } else {
            if (key.keyExists("")) {
                key.removeKey("");
            }
        }
    }

    /**
     * @see #set(int, ItemStack)
     */
    public void set(EquipmentSlot slot, ItemStack item) {
        set(slot.getIndex(), item);
    }

    /**
     * Set the armor from the given slot as the given item.
     *
     * @param slot
     *            Slot of the armor (must be between 0 and 5)
     * @param item
     *            Item to set the armor as
     */
    @SuppressWarnings("deprecation")
    public void set(int slot, ItemStack item) {
        if (item != null) {
            item = item.getType() == Material.AIR ? null : item.clone();
        }
        equipment[slot] = item;
        if (slot == 0) {
            npc.getOrAddTrait(Inventory.class).setItemInHand(item);
        }
        if (!(npc.getEntity() instanceof LivingEntity) && !(npc.getEntity() instanceof ArmorStand))
            return;
        if (npc.getEntity() instanceof Enderman) {
            if (slot != 0)
                throw new UnsupportedOperationException("Slot can only be 0 for enderman");
            if (SpigotUtil.isUsing1_13API()) {
                ((Enderman) npc.getEntity()).setCarriedBlock(item.getType().createBlockData());
            } else {
                ((Enderman) npc.getEntity()).setCarriedMaterial(item.getData());
            }
        } else {
            EntityEquipment equip = getEquipmentFromEntity(npc.getEntity());
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
                case 5:
                    if (SUPPORT_OFFHAND) {
                        try {
                            equip.setItemInOffHand(item);
                        } catch (NoSuchMethodError e) {
                            SUPPORT_OFFHAND = false;
                        }
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Slot must be between 0 and 5");
            }
        }
        if (npc.getEntity() instanceof Player) {
            ((Player) npc.getEntity()).updateInventory();
        }
    }

    void setItemInHand(ItemStack item) {
        equipment[0] = item;
    }

    @Override
    public String toString() {
        return "{hand=" + equipment[0] + ",helmet=" + equipment[1] + ",chestplate=" + equipment[2] + ",leggings="
                + equipment[3] + ",boots=" + equipment[4] + ",offhand=" + equipment[5] + "}";
    }

    public enum EquipmentSlot {
        BOOTS(4),
        CHESTPLATE(2),
        HAND(0),
        HELMET(1),
        LEGGINGS(3),
        OFF_HAND(5);

        private int index;

        EquipmentSlot(int index) {
            this.index = index;
        }

        int getIndex() {
            return index;
        }
    }

    private static boolean SUPPORT_OFFHAND = true;
}