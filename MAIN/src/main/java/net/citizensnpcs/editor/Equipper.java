package net.citizensnpcs.editor;

import org.bukkit.entity.Player;

import net.citizensnpcs.api.npc.NPC;

public interface Equipper {
    public void equip(Player equipper, NPC toEquip);
}