package net.citizensnpcs.api.astar.pathfinder;

import net.citizensnpcs.api.astar.pathfinder.PathPoint.PathCallback;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.material.Door;
import org.bukkit.material.MaterialData;

public class OpenDoorCallback implements PathCallback {
    @Override
    public void run(NPC npc, Block point) {
        BlockState state = point.getState();
        MaterialData data = state.getData();
        if (!(data instanceof Door))
            return;
        Door door = (Door) data;
        door.setOpen(true);
        state.update(true);
    }
}
