package net.citizensnpcs.trait;

import net.citizensnpcs.api.DataKey;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.trait.SaveId;
import net.citizensnpcs.api.npc.trait.Trait;

@SaveId("look-close")
public class LookClose implements Trait, Runnable {
    private final NPC npc;
    private boolean shouldLookClose;

    public LookClose(NPC npc) {
        this.npc = npc;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        try {
            shouldLookClose = key.getBoolean("");
        } catch (Exception ex) {
            throw new NPCLoadException("Invalid value. Valid values: true or false");
        }
    }

    @Override
    public void save(DataKey key) {
        key.setBoolean("", shouldLookClose);
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub

    }

    public void setLookClose(boolean shouldLookClose) {
        this.shouldLookClose = shouldLookClose;
    }

    public boolean shouldLookClose() {
        return shouldLookClose;
    }

    public void toggle() {
        shouldLookClose = !shouldLookClose;
    }

    @Override
    public String toString() {
        return "LookClose{" + shouldLookClose + "}";
    }
}