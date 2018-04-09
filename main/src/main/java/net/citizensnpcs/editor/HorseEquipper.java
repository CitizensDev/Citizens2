package net.citizensnpcs.editor;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.NMS;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;

public class HorseEquipper implements Equipper {
    @Override
    public void equip(Player equipper, NPC toEquip) {
        Tameable horse = (Tameable) toEquip.getEntity();
        NMS.openHorseScreen(horse, equipper);
    }
}
