package net.citizensnpcs.storage.flatfile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.citizensnpcs.api.DataKey;
import net.citizensnpcs.storage.Storage;
import net.citizensnpcs.util.Messaging;

public class YamlStorage implements Storage {
	private final FileConfiguration config;
	private final File file;

	public YamlStorage(String fileName) {
		config = new YamlConfiguration();
		file = new File(fileName);
		if (!file.exists()) {
			create();
			save();
		} else
			load();
	}

	@Override
	public void load() {
		try {
			config.load(file);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void save() {
		try {
			config.save(file);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public DataKey getKey(String root) {
		return new YamlKey(root);
	}

	private void create() {
		try {
			Messaging.log("Creating file: " + file.getName());
			file.getParentFile().mkdirs();
			file.createNewFile();
		} catch (IOException ex) {
			Messaging.log(Level.SEVERE, "Could not create file: " + file.getName());
		}
	}

	public class YamlKey extends DataKey {
		private final String current;

		public YamlKey(String root) {
			current = root;
		}

		@Override
		public void copy(String to) {
			ConfigurationSection root = config.getConfigurationSection(current);
			if (root == null)
				return;
			config.createSection(to, root.getValues(true));
		}

		@Override
		public boolean getBoolean(String key) {
			String path = getKeyExt(key);
			if (keyExists(path)) {
				if (config.getString(path) == null)
					return config.getBoolean(path);
				return Boolean.parseBoolean(config.getString(path));
			}
			return false;
		}

		@Override
		public boolean getBoolean(String keyExt, boolean def) {
			return config.getBoolean(getKeyExt(keyExt), def);
		}

		@Override
		public double getDouble(String key) {
			String path = getKeyExt(key);
			if (keyExists(path)) {
				if (config.getString(path) == null) {
					if (config.get(path) instanceof Integer)
						return config.getInt(path);
					return config.getDouble(path);
				}
				return Double.parseDouble(config.getString(path));
			}
			return 0;
		}

		@Override
		public double getDouble(String keyExt, double def) {
			return config.getDouble(getKeyExt(keyExt), def);
		}

		@Override
		public int getInt(String key) {
			String path = getKeyExt(key);
			if (keyExists(path)) {
				if (config.getString(path) == null)
					return config.getInt(path);
				return Integer.parseInt(config.getString(path));
			}
			return 0;
		}

		@Override
		public int getInt(String path, int value) {
			return config.getInt(getKeyExt(path), value);
		}

		@Override
		public List<DataKey> getIntegerSubKeys() {
			List<DataKey> res = new ArrayList<DataKey>();
			ConfigurationSection section = config.getConfigurationSection(current);
			if (section == null)
				return res;
			List<Integer> keys = new ArrayList<Integer>();
			for (String key : section.getKeys(false)) {
				try {
					keys.add(Integer.parseInt(key));
				} catch (NumberFormatException ex) {
				}
			}
			Collections.sort(keys);
			for (int key : keys)
				res.add(getRelative(Integer.toString(key)));
			return res;
		}

		@Override
		public long getLong(String key) {
			String path = getKeyExt(key);
			if (keyExists(path)) {
				if (config.getString(path) == null) {
					if (config.get(path) instanceof Integer)
						return config.getInt(path);
					return config.getLong(path);
				}
				return Long.parseLong(config.getString(path));
			}
			return 0;
		}

		@Override
		public long getLong(String keyExt, long def) {
			return config.getLong(getKeyExt(keyExt), def);
		}

		@Override
		public DataKey getRelative(String relative) {
			return new YamlKey(getKeyExt(relative));
		}

		@Override
		public String getString(String key) {
			String path = getKeyExt(key);
			if (keyExists(path)) {
				return config.get(path).toString();
			}
			return "";
		}

		@Override
		public Iterable<DataKey> getSubKeys() {
			List<DataKey> res = new ArrayList<DataKey>();
			ConfigurationSection section = config.getConfigurationSection(current);
			if (section == null)
				return res;
			for (String key : section.getKeys(false)) {
				res.add(getRelative(key));
			}
			return res;
		}

		@Override
		public boolean keyExists(String key) {
			return config.get(getKeyExt(key)) != null;
		}

		@Override
		public String name() {
			int last = current.lastIndexOf('.');
			return current.substring(last == 0 ? 0 : last + 1);
		}

		@Override
		public void removeKey(String key) {
			config.set(getKeyExt(key), null);
		}

		@Override
		public void setBoolean(String key, boolean value) {
			config.set(getKeyExt(key), value);
		}

		@Override
		public void setDouble(String key, double value) {
			config.set(getKeyExt(key), String.valueOf(value));
		}

		@Override
		public void setInt(String key, int value) {
			config.set(getKeyExt(key), value);
		}

		@Override
		public void setLong(String key, long value) {
			config.set(getKeyExt(key), value);
		}

		@Override
		public void setString(String key, String value) {
			config.set(getKeyExt(key), value);
		}

		@Override
		public Object getRaw(String key) {
			return config.get(getKeyExt(key));
		}

		@Override
		public void setRaw(String key, Object value) {
			config.set(getKeyExt(key), value);
		}

		private String getKeyExt(String from) {
			if (from.isEmpty())
				return current;
			if (from.charAt(0) == '.')
				return current.isEmpty() ? from.substring(1, from.length()) : current + from;
			return current.isEmpty() ? from : current + "." + from;
		}
	}
}