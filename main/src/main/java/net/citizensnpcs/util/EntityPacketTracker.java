package net.citizensnpcs.util;

import java.util.function.Consumer;

import org.bukkit.entity.Player;

public interface EntityPacketTracker extends Runnable {
    public void link(Player player);

    public void unlink(Player player);

    public void unlinkAll(Consumer<Player> callback);
}