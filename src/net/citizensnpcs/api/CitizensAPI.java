package net.citizensnpcs.api;

public class CitizensAPI {
	private static CitizensPlugin citizens;

	public static CitizensPlugin getInstance() {
		return citizens;
	}

	public static void setInstance(CitizensPlugin plugin) {
		if (citizens != null)
			throw new IllegalArgumentException("already set");
		citizens = plugin;
	}
}