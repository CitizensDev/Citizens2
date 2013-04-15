package net.citizensnpcs.api.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;

import net.citizensnpcs.api.jnbt.ByteTag;
import net.citizensnpcs.api.jnbt.CompoundTag;
import net.citizensnpcs.api.jnbt.DoubleTag;
import net.citizensnpcs.api.jnbt.IntTag;
import net.citizensnpcs.api.jnbt.LongTag;
import net.citizensnpcs.api.jnbt.NBTInputStream;
import net.citizensnpcs.api.jnbt.NBTOutputStream;
import net.citizensnpcs.api.jnbt.StringTag;
import net.citizensnpcs.api.jnbt.Tag;

import org.bukkit.Bukkit;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import com.google.common.io.Files;

public class NBTStorage implements FileStorage {
    private final File file;
    private final String name;
    private final Map<String, Tag> root = Maps.newHashMap();

    public NBTStorage(String file) {
        this(file, "root");
    }

    public NBTStorage(String file, String name) {
        this.file = new File(file);
        if (!this.file.exists())
            create();
        this.name = name;
    }

    private void create() {
        try {
            Bukkit.getLogger().log(Level.INFO, "Creating file: " + file.getName());
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not create file: " + file.getName());
        }
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public DataKey getKey(String root) {
        return new NBTKey(root);
    }

    @Override
    public boolean load() {
        NBTInputStream stream = null;
        try {
            stream = new NBTInputStream(new GZIPInputStream(new FileInputStream(file)));
            Tag tag = stream.readTag();
            if (tag == null || !(tag instanceof CompoundTag)) {
                return false;
            } else {
                root.clear();
                root.putAll(((CompoundTag) tag).getValue());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        } finally {
            Closeables.closeQuietly(stream);
        }
        return true;
    }

    @Override
    public void save() {
        NBTOutputStream stream = null;
        try {
            Files.createParentDirs(file);
            File temporaryFile = File.createTempFile(file.getName(), null, file.getParentFile());
            temporaryFile.deleteOnExit();

            stream = new NBTOutputStream(new FileOutputStream(temporaryFile));
            stream.writeTag(new CompoundTag(name, root));
            stream.close();

            file.delete();
            temporaryFile.renameTo(file);
            temporaryFile.delete();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            Closeables.closeQuietly(stream);
        }
    }

    @Override
    public String toString() {
        return "NBTStorage {file=" + file + "}";
    }

    public class NBTKey extends DataKey {
        public NBTKey(String root) {
            super(root);
        }

        private String createRelativeKey(String parent, String sub) {
            if (sub.isEmpty())
                return parent;
            if (sub.charAt(0) == '.')
                return parent.isEmpty() ? sub.substring(1, sub.length()) : parent + sub;
            return parent.isEmpty() ? sub : parent + "." + sub;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            NBTKey other = (NBTKey) obj;
            if (!getOuterType().equals(other.getOuterType())) {
                return false;
            }
            if (path == null) {
                if (other.path != null) {
                    return false;
                }
            } else if (!path.equals(other.path)) {
                return false;
            }
            return true;
        }

        private Map<String, Tag> findLastParent(String key) {
            String[] parts = Iterables.toArray(Splitter.on('.').split(key), String.class);
            Map<String, Tag> map = root;
            for (int i = 0; i < parts.length - 1; ++i) {
                if (!map.containsKey(parts[i]) || !(map.get(parts[i]) instanceof CompoundTag))
                    return null;
                map = ((CompoundTag) map.get(parts[i])).getValue();
            }
            return map;
        }

        private Tag findLastTag(String key) {
            return findLastTag(key, true);
        }

        private Tag findLastTag(String key, boolean relative) {
            String[] parts = Iterables.toArray(Splitter.on('.').split(relative ? createRelativeKey(key) : key),
                    String.class);
            Map<String, Tag> map = findLastParent(key);
            if (!map.containsKey(parts[parts.length - 1]))
                return null;
            return map.get(parts[parts.length - 1]);
        }

        @Override
        public boolean getBoolean(String key) {
            Tag tag = findLastTag(key);
            if (tag == null || !(tag instanceof ByteTag))
                return false;
            return ((ByteTag) tag).getValue() != 0;
        }

        @Override
        public double getDouble(String key) {
            Tag tag = findLastTag(key);
            if (tag == null || !(tag instanceof DoubleTag))
                return 0D;
            return ((DoubleTag) tag).getValue();
        }

        @Override
        public int getInt(String key) {
            Tag tag = findLastTag(key);
            if (tag == null || !(tag instanceof IntTag))
                return 0;
            return ((IntTag) tag).getValue();
        }

        @Override
        public long getLong(String key) {
            Tag tag = findLastTag(key);
            if (tag == null || !(tag instanceof LongTag))
                return 0;
            return ((LongTag) tag).getValue();
        }

        private String getNameFor(String key) {
            String[] parts = Iterables.toArray(Splitter.on('.').split(createRelativeKey(key)), String.class);
            return parts[parts.length - 1];
        }

        private NBTStorage getOuterType() {
            return NBTStorage.this;
        }

        @Override
        public Object getRaw(String key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DataKey getRelative(String relative) {
            return new NBTKey(createRelativeKey(relative));
        }

        @Override
        public String getString(String key) {
            Tag tag = findLastTag(key);
            if (tag == null || !(tag instanceof StringTag))
                return "";
            return ((StringTag) tag).getValue();
        }

        @Override
        public Iterable<DataKey> getSubKeys() {
            Tag tag = findLastTag(path, false);
            if (!(tag instanceof CompoundTag))
                return Collections.emptyList();
            List<DataKey> subKeys = Lists.newArrayList();
            for (String name : ((CompoundTag) tag).getValue().keySet()) {
                subKeys.add(new NBTKey(createRelativeKey(name)));
            }
            return subKeys;
        }

        @Override
        public Map<String, Object> getValuesDeep() {
            Tag tag = findLastTag(path, false);
            if (!(tag instanceof CompoundTag))
                return Collections.emptyMap();
            Queue<Node> node = new ArrayDeque<Node>(ImmutableList.of(new Node(tag)));
            Map<String, Object> values = Maps.newHashMap();
            while (!node.isEmpty()) {
                Node root = node.poll();
                for (Entry<String, Tag> entry : root.values.entrySet()) {
                    String key = createRelativeKey(root.parent, entry.getKey());
                    if (entry.getValue() instanceof CompoundTag) {
                        node.add(new Node(key, entry.getValue()));
                        continue;
                    }
                    values.put(key, entry.getValue().getValue());
                }
            }
            return values;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((path == null) ? 0 : path.hashCode());
            return result;
        }

        @Override
        public boolean keyExists(String key) {
            return findLastTag(createRelativeKey(key)) != null;
        }

        @Override
        public String name() {
            int last = path.lastIndexOf('.');
            return path.substring(last == 0 ? 0 : last + 1);
        }

        private void putTag(String key, Tag tag) {
            String[] parts = Iterables.toArray(Splitter.on('.').split(createRelativeKey(key)), String.class);
            Map<String, Tag> parent = root;
            for (int i = 0; i < parts.length - 1; ++i) {
                if (!parent.containsKey(parts[i]) || !(parent.get(parts[i]) instanceof CompoundTag)) {
                    parent.put(parts[i], new CompoundTag(parts[i]));
                }
                parent = ((CompoundTag) parent.get(parts[i])).getValue();
            }
            parent.put(tag.getName(), tag);
        }

        @Override
        public void removeKey(String key) {
            String[] parts = Iterables.toArray(Splitter.on('.').split(key), String.class);
            Map<String, Tag> parent = findLastParent(createRelativeKey(key));
            parent.remove(parts[parts.length - 1]);
        }

        @Override
        public void setBoolean(String key, boolean value) {
            putTag(key, new ByteTag(getNameFor(key), (byte) (value ? 1 : 0)));
        }

        @Override
        public void setDouble(String key, double value) {
            putTag(key, new DoubleTag(getNameFor(key), value));
        }

        @Override
        public void setInt(String key, int value) {
            putTag(key, new IntTag(getNameFor(key), value));
        }

        @Override
        public void setLong(String key, long value) {
            putTag(key, new LongTag(getNameFor(key), value));
        }

        @Override
        public void setRaw(String key, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setString(String key, String value) {
            putTag(key, new StringTag(getNameFor(key), value));
        }
    }

    private static class Node {
        final String parent;

        final Map<String, Tag> values;

        public Node(String parent, Tag tag) {
            this.parent = parent;
            values = ((CompoundTag) tag).getValue();
        }

        public Node(Tag tag) {
            this("", tag);
        }
    }
}
