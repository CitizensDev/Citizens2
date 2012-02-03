package net.citizensnpcs.trait;

import net.citizensnpcs.api.DataKey;
import net.citizensnpcs.api.npc.trait.Trait;

public class LookClose implements Trait {
    private boolean shouldLookClose;

    public LookClose() {
    }

    public LookClose(boolean shouldLookClose) {
        this.shouldLookClose = shouldLookClose;
    }

    @Override
    public String getName() {
        return "look-close";
    }

    @Override
    public void load(DataKey key) {
        shouldLookClose = key.getBoolean("");
    }

    @Override
    public void save(DataKey key) {
        key.setBoolean("", shouldLookClose);
    }

    public boolean shouldLookClose() {
        return shouldLookClose;
    }

    public void setLookClose(boolean shouldLookClose) {
        this.shouldLookClose = shouldLookClose;
    }

    @Override
    public String toString() {
        return "LookClose{" + shouldLookClose + "}";
    }
}