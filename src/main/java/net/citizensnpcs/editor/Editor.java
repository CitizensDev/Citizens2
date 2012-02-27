package net.citizensnpcs.editor;

import org.bukkit.event.Listener;

public interface Editor extends Listener {

    public void begin();

    public void end();

    public String getName();
}