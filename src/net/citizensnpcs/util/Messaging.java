package net.citizensnpcs.util;

import java.util.logging.Level;

import org.bukkit.Bukkit;

public class Messaging {

	public static void log(Level level, Object msg) {
		Bukkit.getLogger().log(level, "[Citizens] " + msg);
	}

	public static void log(Object msg) {
		log(Level.INFO, msg);
	}

	public static void debug(Object msg) {
		// TODO add debug setting
		log(msg);
	}
}