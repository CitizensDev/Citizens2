package net.citizensnpcs.api.astar.pathfinder;

import java.util.ListIterator;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Openable;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.material.MaterialData;

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
        Material in = source.getMaterialAt(point.getVector());
        if (MinecraftBlockExaminer.isDoor(in) || MinecraftBlockExaminer.isGate(in)) {
            point.addCallback(new DoorOpener());
            return PassableState.PASSABLE;
        }
        return PassableState.IGNORE;
    }

    static class DoorOpener implements PathCallback {
        boolean opened = false;

        private void close(Block point) {
            closeOpenable(point);
        }

        private void closeOpenable(Block point) {
            if (SpigotUtil.isUsing1_13API()) {
                Openable open = (Openable) point.getBlockData();
                if (!open.isOpen()) {
                    return;
                }
                open.setOpen(false);
                point.setBlockData(open);
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
                    point.getWorld().playSound(point.getLocation(), sound, 10, 1);
                } catch (Exception ex) {
                    SUPPORTS_SOUNDS = false;
                }
            }
        }

        @SuppressWarnings("deprecation")
        private Block getCorrectDoor(Block point) {
            MaterialData data = point.getState().getData();
            if (data instanceof org.bukkit.material.Door) {
                return point;
            }
            org.bukkit.material.Door door = (org.bukkit.material.Door) data;
            boolean bottom = !door.isTopHalf();
            return bottom ? point : point.getRelative(BlockFace.DOWN);
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
                opened = true;
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
                opened = true;
            }
            if (SUPPORTS_SOUNDS) {
                try {
                    Sound sound = MinecraftBlockExaminer.isDoor(point.getType())
                            ? (point.getType() == Material.IRON_DOOR ? Sound.BLOCK_IRON_DOOR_OPEN
                                    : Sound.BLOCK_WOODEN_DOOR_OPEN)
                            : Sound.BLOCK_FENCE_GATE_OPEN;
                    point.getWorld().playSound(point.getLocation(), sound, 10, 1);
                } catch (Exception ex) {
                    SUPPORTS_SOUNDS = false;
                }
            }
        }

        @Override
        public void run(NPC npc, Block point, ListIterator<Block> path) {
            if (!MinecraftBlockExaminer.isDoor(point.getType()))
                return;
            if (npc.getStoredLocation().distanceSquared(point.getLocation().add(0.5, 0, 0.5)) > 4) {
                if (opened) {
                    close(point);
                    opened = false;
                }
                return;
            }
            open(npc, point);
        }

        private static boolean SUPPORTS_SOUNDS = true;
    }
}