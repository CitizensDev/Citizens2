package net.citizensnpcs.editor;

import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.NMS;

public class HorseEquipper implements Equipper {
    @Override
    public void equip(Player equipper, NPC toEquip) {
        Horse horse = (Horse) toEquip.getEntity();
        NMS.openHorseScreen(horse, equipper);
    }
}
