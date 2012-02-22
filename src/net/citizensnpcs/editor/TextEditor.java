package net.citizensnpcs.editor;

import org.bukkit.entity.Player;

public class TextEditor implements Editor {
    private Player player;

    public TextEditor(Player player) {
        this.player = player;
    }

    @Override
    public void begin() {
    }

    @Override
    public void end() {
    }
}