package net.citizensnpcs.storage.flatfile;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

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
		// TODO Auto-generated method stub

	}

	@Override
	public void save() {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeKey(String key) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getString(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getString(String key, String value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setString(String key, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getInt(String key) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getInt(String key, int value) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setInt(String key, int value) {
		// TODO Auto-generated method stub

	}

	@Override
	public double getDouble(String key) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getDouble(String key, double value) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setDouble(String key, double value) {
		// TODO Auto-generated method stub

	}

	@Override
	public long getLong(String key) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getLong(String key, long value) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setLong(String key, long value) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean getBoolean(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getBoolean(String key, boolean value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setBoolean(String key, boolean value) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getRaw(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRaw(String path, Object value) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean keyExists(String path) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Collection<String> getKeys(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Integer> getIntegerKeys(String string) {
		// TODO Auto-generated method stub
		return null;
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
}