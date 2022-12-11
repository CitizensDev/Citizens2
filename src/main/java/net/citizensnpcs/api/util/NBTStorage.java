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
import java.util.zip.GZIPInputStream;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import net.citizensnpcs.api.jnbt.ByteTag;
import net.citizensnpcs.api.jnbt.CompoundTag;
import net.citizensnpcs.api.jnbt.DoubleTag;
import net.citizensnpcs.api.jnbt.FloatTag;
import net.citizensnpcs.api.jnbt.IntTag;
import net.citizensnpcs.api.jnbt.LongTag;
import net.citizensnpcs.api.jnbt.NBTInputStream;
import net.citizensnpcs.api.jnbt.NBTOutputStream;
import net.citizensnpcs.api.jnbt.NBTUtils;
import net.citizensnpcs.api.jnbt.ShortTag;
import net.citizensnpcs.api.jnbt.StringTag;
import net.citizensnpcs.api.jnbt.Tag;

public class NBTStorage implements FileStorage {
    private final File file;
    private final String name;
    private final Map<String, Tag> root = Maps.newHashMap();

    public NBTStorage(File file) {
        this(file, "root");
    }

    public NBTStorage(File file, String name) {
        this.file = file;
        if (!this.file.exists()) {
            create();
        }
        this.name = name;
    }

    public NBTStorage(String file) {
        this(new File(file), "root");
    }

    private void create() {
        try {
            Messaging.log("Creating file: " + file.getName());
            Files.createParentDirs(file);
            file.createNewFile();
        } catch (IOException ex) {
            Messaging.severe("Could not create file: " + file.getName());
            ex.printStackTrace();
        }
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public NBTKey getKey(String root) {
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
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {
                e.getCause(); // Do nothing
            }
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
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {
                e.getCause(); // Do nothing
            }
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
            if (obj == null || getClass() != obj.getClass()) {
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

        private Number extractNumber(Tag tag) {
            if (tag == null)
                return null;
            if (tag instanceof DoubleTag) {
                return ((DoubleTag) tag).getValue();
            } else if (tag instanceof IntTag) {
                return ((IntTag) tag).getValue();
            } else if (tag instanceof ShortTag) {
                return ((ShortTag) tag).getValue();
            } else if (tag instanceof ByteTag) {
                return ((ByteTag) tag).getValue();
            } else if (tag instanceof FloatTag) {
                return ((FloatTag) tag).getValue();
            } else if (tag instanceof LongTag) {
                return ((LongTag) tag).getValue();
            }
            return null;
        }

        private Map<String, Tag> findLastParent(String[] parts) {
            Map<String, Tag> map = root;
            for (int i = 0; i < parts.length - 1; ++i) {
                if (!map.containsKey(parts[i]) || !(map.get(parts[i]) instanceof CompoundTag))
                    return Collections.emptyMap();
                map = ((CompoundTag) map.get(parts[i])).getValue();
            }
            return map;
        }

        private Tag findLastTag(String key) {
            return findLastTag(key, true);
        }

        private Tag findLastTag(String key, boolean relative) {
            String[] parts = Iterables.toArray(
                    Splitter.on('.').omitEmptyStrings().split(relative ? createRelativeKey(key) : key), String.class);
            if (parts.length == 0)
                return new CompoundTag(name, root);
            Map<String, Tag> map = findLastParent(parts);
            if (!map.containsKey(parts[parts.length - 1]))
                return null;
            return map.get(parts[parts.length - 1]);
        }

        @Override
        public boolean getBoolean(String key) {
            Number number = extractNumber(findLastTag(key));
            if (number == null)
                return false;
            return number.byteValue() >= 1 ? true : false;
        }

        @Override
        public double getDouble(String key) {
            Number number = extractNumber(findLastTag(key));
            if (number == null)
                return 0;
            return number.doubleValue();
        }

        @Override
        public DataKey getFromRoot(String path) {
            return new NBTKey(path);
        }

        @Override
        public int getInt(String key) {
            Number number = extractNumber(findLastTag(key));
            if (number == null)
                return 0;
            return number.intValue();
        }

        @Override
        public long getLong(String key) {
            Number number = extractNumber(findLastTag(key));
            if (number == null)
                return 0;
            return number.longValue();
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
            Tag tag = findLastTag(key);
            if (tag == null)
                return null;
            return tag.getValue();
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
            return 31 * (31 + getOuterType().hashCode()) + ((path == null) ? 0 : path.hashCode());
        }

        @Override
        public boolean keyExists(String key) {
            return findLastTag(createRelativeKey(key), false) != null;
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
            String[] parts = Iterables.toArray(Splitter.on('.').split(createRelativeKey(key)), String.class);
            Map<String, Tag> parent = findLastParent(parts);
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
            Tag tag = NBTUtils.createTag(getNameFor(key), value);
            if (tag == null)
                throw new IllegalArgumentException("could not convert value to tag");
            putTag(key, tag);
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
