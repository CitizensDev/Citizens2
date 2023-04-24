package net.citizensnpcs.api.astar.pathfinder;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.astar.pathfinder.PathPoint.PathCallback;
import net.citizensnpcs.api.event.NPCOpenDoorEvent;
import net.citizensnpcs.api.event.NPCOpenGateEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.SpigotUtil;

public class DoorExaminer implements BlockExaminer {
    @Override
    public float getCost(BlockSource source, PathPoint point) {
        return 0F;
    }

    @Override
    public PassableState isPassable(BlockSource source, PathPoint point) {
        if (!MinecraftBlockExaminer.canStandOn(source.getMaterialAt(point.getVector().getBlockX(),
                point.getVector().getBlockY() - 1, point.getVector().getBlockZ())))
            return PassableState.IGNORE;
        Block in = source.getBlockAt(point.getVector());

        if ((MinecraftBlockExaminer.isDoor(in.getType()) && isBottomDoor(in))
                || MinecraftBlockExaminer.isGate(in.getType())) {
            point.addCallback(new DoorOpener());
            return PassableState.PASSABLE;
        }

        return PassableState.IGNORE;
    }

    private static class DoorOpener implements PathCallback {
        private boolean opened;

        private void close(NPC npc, Block point) {
            if (SpigotUtil.isUsing1_13API()) {
                Openable open = (Openable) point.getBlockData();
                if (!open.isOpen()) {
                    return;
                }
                open.setOpen(false);
                point.setBlockData(open);
                point.getState().update();
            } else {
                point = getCorrectDoor(point);
                BlockState state = point.getState();
                org.bukkit.material.Openable open = (org.bukkit.material.Openable) state.getData();
                if (!open.isOpen()) {
                    return;
                }
                open.setOpen(false);
                state.setData((MaterialData) open);
                state.update();
            }
            if (SUPPORTS_SOUNDS) {
                try {
                    Sound sound = MinecraftBlockExaminer.isDoor(point.getType())
                            ? (point.getType() == Material.IRON_DOOR ? Sound.BLOCK_IRON_DOOR_CLOSE
                                    : Sound.BLOCK_WOODEN_DOOR_CLOSE)
                            : Sound.BLOCK_FENCE_GATE_CLOSE;
                    point.getWorld().playSound(point.getLocation(), sound, 2, 1);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    SUPPORTS_SOUNDS = false;
                }
            }
            tryArmSwing(npc);
        }

        @Override
        public void onReached(NPC npc, Block point) {
            Location doorCentre = point.getLocation().add(0.5, 0, 0.5);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!npc.getNavigator().isNavigating()) {
                        if (opened && npc.getStoredLocation().distance(doorCentre) <= 1.8) {
                            close(npc, point);
                        }
                        cancel();
                        return;
                    }

                    if (npc.getStoredLocation().distance(doorCentre) > 1.8) {
                        close(npc, point);
                        cancel();
                    }
                }
            }.runTaskTimer(CitizensAPI.getPlugin(), 3, 1);
        }

        private void open(NPC npc, Block point) {
            if (SpigotUtil.isUsing1_13API()) {
                Openable open = (Openable) point.getBlockData();
                if (open.isOpen()) {
                    return;
                }
                Cancellable event = MinecraftBlockExaminer.isDoor(point.getType()) ? new NPCOpenDoorEvent(npc, point)
                        : new NPCOpenGateEvent(npc, point);
                Bukkit.getPluginManager().callEvent((Event) event);
                if (event.isCancelled()) {
                    return;
                }
                open.setOpen(true);
                point.setBlockData(open);
                point.getState().update();
            } else {
                point = getCorrectDoor(point);
                BlockState state = point.getState();
                org.bukkit.material.Openable open = (org.bukkit.material.Openable) state.getData();
                if (open.isOpen()) {
                    return;
                }
                Cancellable event = MinecraftBlockExaminer.isDoor(point.getType()) ? new NPCOpenDoorEvent(npc, point)
                        : new NPCOpenGateEvent(npc, point);
                Bukkit.getPluginManager().callEvent((Event) event);
                if (event.isCancelled()) {
                    return;
                }
                open.setOpen(true);
                state.setData((MaterialData) open);
                state.update();
            }
            if (SUPPORTS_SOUNDS) {
                try {
                    Sound sound = MinecraftBlockExaminer.isDoor(point.getType())
                            ? (point.getType() == Material.IRON_DOOR ? Sound.BLOCK_IRON_DOOR_OPEN
                                    : Sound.BLOCK_WOODEN_DOOR_OPEN)
                            : Sound.BLOCK_FENCE_GATE_OPEN;
                    point.getWorld().playSound(point.getLocation(), sound, 2, 1);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    SUPPORTS_SOUNDS = false;
                }
            }
            tryArmSwing(npc);
        }

        @Override
        public void run(NPC npc, Block point, List<Block> path, int index) {
            if (opened)
                return;
            if (!MinecraftBlockExaminer.isDoor(point.getType()) && !MinecraftBlockExaminer.isGate(point.getType()))
                return;
            if (npc.getStoredLocation().distance(point.getLocation().add(0.5, 0, 0.5)) > 2.5)
                return;
            open(npc, point);
            opened = true;
        }

        private void tryArmSwing(NPC npc) {
            if (SUPPORTS_SWING_ANIMATION && npc.getEntity() instanceof LivingEntity) {
                try {
                    ((LivingEntity) npc.getEntity()).swingMainHand();
                } catch (Exception ex) {
                    SUPPORTS_SWING_ANIMATION = false;
                }
            }
        }

        private static boolean SUPPORTS_SOUNDS = true;
        private static boolean SUPPORTS_SWING_ANIMATION = true;
    }

    @SuppressWarnings("deprecation")
    private static Block getCorrectDoor(Block point) {
        if (SpigotUtil.isUsing1_13API()) {
            BlockData bd = point.getBlockData();
            if (!(bd instanceof Bisected))
                return point;
            return ((Bisected) bd).getHalf() == Half.BOTTOM ? point : point.getRelative(BlockFace.DOWN);
        }
        MaterialData data = point.getState().getData();
        if (!(data instanceof org.bukkit.material.Door))
            return point;

        org.bukkit.material.Door door = (org.bukkit.material.Door) data;
        boolean bottom = !door.isTopHalf();
        return bottom ? point : point.getRelative(BlockFace.DOWN);
    }

    private static boolean isBottomDoor(Block point) {
        if (SpigotUtil.isUsing1_13API()) {
            BlockData bd = point.getBlockData();
            if (!(bd instanceof Bisected))
                return false;
            return ((Bisected) bd).getHalf() == Half.BOTTOM;
        }
        MaterialData data = point.getState().getData();
        if (!(data instanceof org.bukkit.material.Door))
            return false;

        org.bukkit.material.Door door = (org.bukkit.material.Door) data;
        return !door.isTopHalf();
    }
}