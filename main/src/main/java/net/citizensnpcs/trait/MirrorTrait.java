package net.citizensnpcs.trait;

import java.util.function.BiFunction;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.SpawnReason;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
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

    private static final BiFunction<Player, EquipmentSlot, ItemStack> MIRROR_EQUIPMENT = (player, slot) -> player
            .getInventory().getItem(slot.toBukkit());
}