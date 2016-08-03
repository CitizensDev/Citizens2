package net.citizensnpcs.editor;

import net.citizensnpcs.api.npc.NPC;

import org.bukkit.entity.Player;

public interface Equipper {
    public void equip(Player equipper, NPC toEquip);
}