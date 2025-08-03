package net.citizensnpcs.api.event;

import org.bukkit.event.HandlerList;

import net.citizensnpcs.api.npc.NPC;

public class NPCRenameEvent extends NPCEvent {
    private String newName;
    private final String oldName;

    public NPCRenameEvent(NPC npc, String oldName, String newName) {
        super(npc);
        this.oldName = oldName;
        this.setNewName(newName);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public String getNewName() {
        return newName;
    }

    public String getOldName() {
        return oldName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();
}
