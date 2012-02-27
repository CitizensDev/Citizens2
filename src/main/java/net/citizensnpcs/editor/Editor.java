package net.citizensnpcs.editor;

import org.bukkit.event.Listener;

public abstract class Editor implements Listener {

    public abstract void begin();

    public abstract void end();
}