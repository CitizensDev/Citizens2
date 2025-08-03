package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;

public abstract class NPCTraitEvent extends NPCEvent {
    private final Trait trait;

    protected NPCTraitEvent(NPC npc, Trait trait) {
        super(npc);
        this.trait = trait;
    }

    public Trait getTrait() {
        return trait;
    }
}
