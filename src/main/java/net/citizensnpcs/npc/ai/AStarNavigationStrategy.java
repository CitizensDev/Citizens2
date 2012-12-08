package net.citizensnpcs.npc.ai;

import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.astar.AStarMachine;
import net.citizensnpcs.api.astar.pathfinder.ChunkBlockSource;
import net.citizensnpcs.api.astar.pathfinder.Path;
import net.citizensnpcs.api.astar.pathfinder.VectorGoal;
import net.citizensnpcs.api.astar.pathfinder.VectorNode;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.NMS;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class AStarNavigationStrategy extends AbstractPathStrategy {
    private final Location destination;
    private final NPC npc;
    private final NavigatorParameters params;
    private Path plan;
    private Vector vector;

    AStarNavigationStrategy(NPC npc, Location dest, NavigatorParameters params) {
        super(TargetType.LOCATION);
        this.params = params;
        this.destination = dest;
        this.npc = npc;
        Location location = npc.getBukkitEntity().getEyeLocation();
        plan = (Path) ASTAR.runFully(new VectorGoal(dest), new VectorNode(location, new ChunkBlockSource(
                location, params.range()), params.examiners()), (int) (params.range() * 10));
        if (plan == null || plan.isComplete()) {
            setCancelReason(CancelReason.STUCK);
        } else {
            vector = plan.getCurrentVector();
        }
    }

    @Override
    public Location getTargetAsLocation() {
        return destination;
    }

    @Override
    public void stop() {
        plan = null;
    }

    @Override
    public boolean update() {
        if (getCancelReason() != null || plan == null || plan.isComplete())
            return true;
        if (npc.getBukkitEntity().getLocation(NPC_LOCATION).toVector().distanceSquared(vector) <= params
                .distanceMargin()) {
            plan.update(npc);
            if (plan.isComplete())
                return true;
            vector = plan.getCurrentVector();
            npc.getBukkitEntity()
                    .getWorld()
                    .playEffect(vector.toLocation(npc.getBukkitEntity().getWorld()), Effect.STEP_SOUND,
                            org.bukkit.Material.STONE.getId());
        }
        NMS.setDestination(npc.getBukkitEntity(), vector.getX(), vector.getY(), vector.getZ(), params.speed());
        return false;
    }

    private static final AStarMachine ASTAR = AStarMachine.createWithDefaultStorage();

    private static final Location NPC_LOCATION = new Location(null, 0, 0, 0);
}