package net.citizensnpcs.trait;

import java.util.Map;
import java.util.function.BiFunction;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.ImmutableMap;

import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.NPCSeenByPlayerEvent;
import net.citizensnpcs.api.event.SpawnReason;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitEventHandler;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Equipment.EquipmentSlot;

@TraitName("mirrortrait")
public class MirrorTrait extends Trait {
    @Persist
    private volatile boolean enabled;
    private volatile BiFunction<Player, EquipmentSlot, ItemStack> equipmentFunction;
    @Persist
    private volatile boolean mirrorEquipment;
    @Persist
    private volatile boolean mirrorName;

    public MirrorTrait() {
        super("mirrortrait");
    }

    public BiFunction<Player, EquipmentSlot, ItemStack> getEquipmentFunction() {
        return mirrorEquipment && equipmentFunction == null ? MIRROR_EQUIPMENT : equipmentFunction;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isMirroring(Player player) {
        return enabled;
    }

    public boolean isMirroringEquipment() {
        return mirrorEquipment;
    }

    public boolean mirrorName() {
        return mirrorName;
    }

    @TraitEventHandler(@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR))
    private void onSeenByPlayer(NPCSeenByPlayerEvent event) {
        if (!isMirroringEquipment() || !npc.hasTrait(Equipment.class))
            return;
        for (ItemStack stack : npc.getOrAddTrait(Equipment.class).getEquipment()) {
            if (stack != null && stack.getType() != Material.AIR)
                return;
        }
        event.getPlayer().sendEquipmentChange((LivingEntity) npc.getEntity(), EMPTY_EQUIPMENT_MAP);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (npc.isSpawned()) {
            npc.despawn(DespawnReason.PENDING_RESPAWN);
            npc.spawn(npc.getStoredLocation(), SpawnReason.RESPAWN);
        }
    }

    public void setEquipmentFunction(BiFunction<Player, EquipmentSlot, ItemStack> func) {
        this.equipmentFunction = func;
    }

    public void setMirrorEquipment(boolean mirrorEquipment) {
        this.mirrorEquipment = mirrorEquipment;
    }

    public void setMirrorName(boolean mirror) {
        mirrorName = mirror;
    }

    private static final Map<org.bukkit.inventory.EquipmentSlot, ItemStack> EMPTY_EQUIPMENT_MAP = ImmutableMap
            .of(org.bukkit.inventory.EquipmentSlot.HAND, new ItemStack(Material.AIR, 1));
    private static final BiFunction<Player, EquipmentSlot, ItemStack> MIRROR_EQUIPMENT = (player, slot) -> player
            .getInventory().getItem(slot.toBukkit());
}