package net.citizensnpcs.api.trait.trait;

import java.util.Locale;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import net.citizensnpcs.api.event.NPCEvent;
import net.citizensnpcs.api.event.NPCSeenByPlayerEvent;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPC.NPCUpdate;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitEventHandler;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.ItemStorage;
import net.citizensnpcs.api.util.SpigotUtil;

/**
 * Represents an NPC's equipment.
 */
@TraitName("equipment")
public class Equipment extends Trait {
    private final ItemStack[] cosmetic = new ItemStack[7];
    private final ItemStack[] equipment = new ItemStack[7];

    public Equipment() {
        super("equipment");
    }

    private ItemStack clone(ItemStack item) {
        return item == null || item.getType() == Material.AIR ? null : item.clone();
    }

    /**
     * Get an NPC's equipment from the given slot.
     *
     * @param slot
     *            Slot where the equipment is located
     * @return ItemStack from the given equipment slot
     */
    public ItemStack get(EquipmentSlot eslot) {
        int slot = eslot.getIndex();
        if (npc.getEntity() instanceof Enderman && slot != 0) {
            throw new IllegalArgumentException("Slot must be 0 for enderman");
        }
        return equipment[slot] == null ? null : equipment[slot].clone();
    }

    /**
     * Gets the NPC's cosmetic equipment from the given slot. Nullable.
     *
     * @param slot
     *            Equipment slot
     * @return ItemStack or null in the given equipment slot
     */
    public ItemStack getCosmetic(EquipmentSlot slot) {
        return cosmetic[slot.getIndex()] == null ? null : cosmetic[slot.getIndex()].clone();
    }

    /**
     * Get all of an NPC's cosmetic equipment.
     *
     * @return An array of an NPC's cosmetic equipment
     */
    public ItemStack[] getCosmeticEquipment() {
        return cosmetic;
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
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            map.put(slot, equipment[slot.getIndex()] == null ? null : equipment[slot.getIndex()].clone());
        }
        return map;
    }

    private EntityEquipment getEquipmentFromEntity(Entity entity) {
        if (entity instanceof LivingEntity)
            return ((LivingEntity) entity).getEquipment();
        throw new IllegalStateException("Unsupported entity equipment");
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            String name = slot.name().toLowerCase(Locale.ROOT);
            if (key.keyExists(name)) {
                equipment[slot.getIndex()] = ItemStorage.loadItemStack(key.getRelative(name));
            }
            if (key.keyExists("cosmetic_" + name)) {
                cosmetic[slot.getIndex()] = ItemStorage.loadItemStack(key.getRelative("cosmetic_" + name));
            }
        }
    }

    @Override
    public void onAttach() {
        npc.scheduleUpdate(NPCUpdate.PACKET);
        run();
    }

    @TraitEventHandler(@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR))
    private void onSeenByPlayer(NPCSeenByPlayerEvent event) {
        for (ItemStack stack : equipment) {
            if (stack != null && stack.getType() != Material.AIR)
                return;

        }
        boolean hasCosmetic = false;
        for (ItemStack stack : cosmetic) {
            if (stack != null && stack.getType() != Material.AIR) {
                hasCosmetic = true;
                break;
            }
        }
        if (!hasCosmetic)
            return;
        event.getPlayer().sendEquipmentChange((LivingEntity) npc.getEntity(), EMPTY_EQUIPMENT_MAP);
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
            if (SUPPORT_OFFHAND) {
                equip.setItemInOffHand(equipment[5]);
            }
            if (SUPPORT_BODY
                    && (npc.getEntity().getType() == EntityType.WOLF || npc.getEntity() instanceof AbstractHorse)) {
                equip.setItem(org.bukkit.inventory.EquipmentSlot.BODY, equipment[6]);
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
                equipment[5] = clone(equip.getItemInOffHand());
            }
            if (SUPPORT_BODY
                    && (npc.getEntity().getType() == EntityType.WOLF || npc.getEntity() instanceof AbstractHorse)) {
                equipment[6] = clone(equip.getItem(org.bukkit.inventory.EquipmentSlot.BODY));
            }
        }
    }

    @Override
    public void save(DataKey key) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            saveOrRemove(key.getRelative(slot.name().toLowerCase(Locale.ROOT)), equipment[slot.getIndex()]);
            saveOrRemove(key.getRelative("cosmetic_" + slot.name().toLowerCase(Locale.ROOT)),
                    cosmetic[slot.getIndex()]);
        }
    }

    private void saveOrRemove(DataKey key, ItemStack item) {
        if (item != null) {
            ItemStorage.saveItem(key, item);
        } else if (key.keyExists("")) {
            key.removeKey("");
        }
    }

    /**
     * Set the armor from the given slot as the given item.
     *
     * @param slot
     *            Equipment slot
     * @param item
     *            Item to set the armor as
     */
    @SuppressWarnings("deprecation")
    public void set(EquipmentSlot eslot, ItemStack item) {
        if (NPCChangeEquipmentEvent.getHandlerList().getRegisteredListeners().length > 0) {
            Bukkit.getPluginManager().callEvent(new NPCChangeEquipmentEvent(npc, eslot, item));
        }
        int slot = eslot.getIndex();
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
                        equip.setItemInOffHand(item);
                    }
                    break;
                case 6:
                    if (SUPPORT_BODY && (npc.getEntity().getType() == EntityType.WOLF
                            || npc.getEntity() instanceof AbstractHorse)) {
                        equip.setItem(org.bukkit.inventory.EquipmentSlot.BODY, item);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Slot must be between 0 and 6");
            }
        }
        if (npc.getEntity() instanceof Player) {
            ((Player) npc.getEntity()).updateInventory();
        }
    }

    /**
     * Set the cosmetic equipment in the given slot
     *
     * @param slot
     *            The equipment slot
     * @param stack
     *            Thew new itemstack
     */
    public void setCosmetic(EquipmentSlot slot, ItemStack stack) {
        cosmetic[slot.getIndex()] = stack == null ? null : stack.clone();
    }

    public enum EquipmentSlot {
        BODY(6),
        BOOTS(4),
        CHESTPLATE(2),
        HAND(0),
        HELMET(1),
        LEGGINGS(3),
        OFF_HAND(5);

        private final int index;

        EquipmentSlot(int index) {
            this.index = index;
        }

        int getIndex() {
            return index;
        }

        public org.bukkit.inventory.EquipmentSlot toBukkit() {
            switch (this) {
                case BODY:
                    return org.bukkit.inventory.EquipmentSlot.BODY;
                case BOOTS:
                    return org.bukkit.inventory.EquipmentSlot.FEET;
                case CHESTPLATE:
                    return org.bukkit.inventory.EquipmentSlot.CHEST;
                case HAND:
                    return org.bukkit.inventory.EquipmentSlot.HAND;
                case HELMET:
                    return org.bukkit.inventory.EquipmentSlot.HEAD;
                case LEGGINGS:
                    return org.bukkit.inventory.EquipmentSlot.LEGS;
                case OFF_HAND:
                    return org.bukkit.inventory.EquipmentSlot.OFF_HAND;
                default:
                    break;

            }
            return null;
        }
    }

    public static class NPCChangeEquipmentEvent extends NPCEvent {
        private final EquipmentSlot slot;
        private final ItemStack stack;

        public NPCChangeEquipmentEvent(NPC npc, EquipmentSlot slot, ItemStack stack) {
            super(npc);
            this.slot = slot;
            this.stack = stack;
        }

        @Override
        public HandlerList getHandlers() {
            return handlers;
        }

        public EquipmentSlot getSlot() {
            return slot;
        }

        public ItemStack getStack() {
            return stack;
        }

        public static HandlerList getHandlerList() {
            return handlers;
        }

        private static final HandlerList handlers = new HandlerList();
    }

    private static final Map<org.bukkit.inventory.EquipmentSlot, ItemStack> EMPTY_EQUIPMENT_MAP = ImmutableMap
            .of(org.bukkit.inventory.EquipmentSlot.HAND, new ItemStack(Material.AIR, 1));
    private static boolean SUPPORT_BODY = false;
    private static boolean SUPPORT_OFFHAND = true;
    static {
        try {
            EntityEquipment.class.getMethod("setItemInOffHand", ItemStack.class);
        } catch (Exception e) {
            SUPPORT_OFFHAND = false;
        }
        for (org.bukkit.inventory.EquipmentSlot value : org.bukkit.inventory.EquipmentSlot.values()) {
            if (value.name().equals("BODY")) {
                SUPPORT_BODY = true;
                break;
            }
        }
    }
}