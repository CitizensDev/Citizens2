package net.citizensnpcs.editor;

import org.bukkit.entity.Player;

public class EquipmentEditor implements Editor {
    private Player player;

    public EquipmentEditor(Player player) {
        this.player = player;
    }

    @Override
    public void begin() {
    }

    @Override
    public void end() {
    }
}