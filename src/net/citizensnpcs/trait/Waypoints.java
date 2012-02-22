package net.citizensnpcs.trait;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.trait.SaveId;
import net.citizensnpcs.api.npc.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

@SaveId("waypoints")
public class Waypoints extends Trait {
    private final NPC npc;
    private WaypointProvider provider;

    public Waypoints(NPC npc) {
        this.npc = npc;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
    }

    @Override
    public void save(DataKey key) {
        // TODO Auto-generated method stub
    }
}
