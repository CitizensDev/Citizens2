package net.citizensnpcs.editor;

import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Inventory;
import net.citizensnpcs.util.NMS;

public class HorseEquipper implements Equipper {
    @Override
    public void equip(Player equipper, NPC toEquip) {
        toEquip.getOrAddTrait(Inventory.class);
        Tameable horse = (Tameable) toEquip.getEntity();
        NMS.openHorseScreen(horse, equipper);
    }
}
