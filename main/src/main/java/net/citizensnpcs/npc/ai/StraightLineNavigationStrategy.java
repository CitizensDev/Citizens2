package net.citizensnpcs.npc.ai;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.ai.AbstractPathStrategy;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.api.astar.pathfinder.MinecraftBlockExaminer;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;

public class StraightLineNavigationStrategy extends AbstractPathStrategy {
    private Location destination;
    private final NPC npc;
    private final NavigatorParameters params;
    private Entity target;

    public StraightLineNavigationStrategy(NPC npc, Entity target, NavigatorParameters params) {
        super(TargetType.LOCATION);
        this.params = params;
        this.target = target;
        this.npc = npc;
        destination = params.entityTargetLocationMapper().apply(target);
    }

    public StraightLineNavigationStrategy(NPC npc, Location dest, NavigatorParameters params) {
        super(TargetType.LOCATION);
        this.params = params;
        destination = dest;
        this.npc = npc;
    }

    @Override
    public Location getCurrentDestination() {
        return destination;
    }

    @Override
    public Iterable<Vector> getPath() {
        return null;
    }

    @Override
    public Location getTargetAsLocation() {
        return destination;
    }

    @Override
    public void stop() {
    }

    @Override
    public boolean update() {
        if (getCancelReason() != null || npc.getStoredLocation().getWorld() != destination.getWorld())
            return true;

        Location currLoc = npc.getEntity().getLocation();
        if (currLoc.distance(destination) <= params.distanceMargin()) {
            if (npc.isFlyable()) {
                npc.getEntity().setVelocity(new Vector(0, 0, 0));
            }
            return true;
        }
        if (target != null) {
            destination = params.entityTargetLocationMapper().apply(target);
        }
        Vector destVector = currLoc.toVector().add(destination.toVector().subtract(currLoc.toVector()).normalize());
        Location destLoc = destVector.toLocation(destination.getWorld());
        if (!npc.isFlyable() && destVector.getBlockY() > currLoc.getBlockY()) {
            Block block = destLoc.getBlock();
            while (block.getY() > currLoc.getBlockY()
                    && !MinecraftBlockExaminer.canStandOn(block.getRelative(BlockFace.DOWN))) {
                block = block.getRelative(BlockFace.DOWN);
                if (block.getY() <= 0) {
                    block = destLoc.getBlock();
                    break;
                }
            }
            destLoc = block.getLocation();
            destVector = destLoc.toVector();
        }
        double dX = destVector.getX() - currLoc.getX();
        double dZ = destVector.getZ() - currLoc.getZ();
        double dY = destVector.getY() - currLoc.getY();
        double xzDistance = dX * dX + dZ * dZ;
        double distance = xzDistance + dY * dY;
        if (npc.isFlyable()) {
            Vector velocity = npc.getEntity().getVelocity();
            double motX = velocity.getX(), motY = velocity.getY(), motZ = velocity.getZ();

            motX += (Math.signum(dX) * 0.5D - motX) * 0.1;
            motY += (Math.signum(dY) - motY) * 0.1;
            motZ += (Math.signum(dZ) * 0.5D - motZ) * 0.1;
            velocity.setX(motX).setY(motY).setZ(motZ).multiply(params.speed());
            npc.getEntity().setVelocity(velocity);

            float targetYaw = (float) (Math.atan2(motZ, motX) * 180.0D / Math.PI) - 90.0F;
            float normalisedTargetYaw = targetYaw - currLoc.getYaw();
            while (normalisedTargetYaw >= 180.0F) {
                normalisedTargetYaw -= 360.0F;
            }
            while (normalisedTargetYaw < -180.0F) {
                normalisedTargetYaw += 360.0F;
            }
            if (npc.getEntity().getType() != EntityType.ENDER_DRAGON) {
                NMS.setVerticalMovement(npc.getEntity(), 0.5);
                NMS.setHeadAndBodyYaw(npc.getEntity(), currLoc.getYaw() + normalisedTargetYaw);
            }
        } else if (npc.getEntity() instanceof LivingEntity) {
            NMS.setDestination(npc.getEntity(), destVector.getX(), destVector.getY(), destVector.getZ(),
                    params.speed());
        } else {
            Vector dir = destVector.subtract(currLoc.toVector()).normalize().multiply(0.2);
            Block in = currLoc.getBlock();
            if (distance > 0 && dY >= 1 && xzDistance <= 2.75
                    || dY >= 0.2 && MinecraftBlockExaminer.isLiquidOrInLiquid(in)) {
                dir.add(new Vector(0, 0.75, 0));
            }
            Util.faceLocation(npc.getEntity(), destLoc);
            npc.getEntity().setVelocity(dir);
        }
        return false;
    }
}
