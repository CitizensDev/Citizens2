package net.citizensnpcs.api.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

public class SpigotUtil {
    /**
     * Spigot has changed InventoryViews to be an abstract class instead of an interface necessitating an abstraction
     * over the existing API to avoid java runtime errors. This class is subject to change as required.
     */
    public static class InventoryViewAPI {
        private final InventoryView view;

        public InventoryViewAPI(InventoryView view) {
            this.view = view;
        }

        public void close() {
            try {
                CLOSE.invoke(view);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj || obj == null)
                return true;

            if (getClass() != obj.getClass())
                return false;

            InventoryViewAPI other = (InventoryViewAPI) obj;
            if (view == null) {
                if (other.view != null) {
                    return false;
                }
            } else {
                try {
                    if (!(boolean) EQUALS.invoke(view, other.view))
                        return false;

                } catch (Throwable e) {
                    e.printStackTrace();
                    return false;
                }
            }
            return true;
        }

        public Player getPlayer() {
            try {
                return (Player) GET_PLAYER.invoke(view);
            } catch (Throwable e) {
                e.printStackTrace();
                return null;
            }
        }

        public Inventory getTopInventory() {
            try {
                return (Inventory) TOP_INVENTORY.invoke(view);
            } catch (Throwable e) {
                e.printStackTrace();
                return null;
            }
        }

        public InventoryView getView() {
            return view;
        }

        @Override
        public int hashCode() {
            try {
                return 31 + ((view == null) ? 0 : (int) HASHCODE.invoke(view));
            } catch (Throwable e) {
                e.printStackTrace();
                return 31;
            }
        }

        private static MethodHandle getMethod(Class<?> clazz, String method, Class<?>... params) {
            try {
                Method f = null;
                try {
                    f = clazz.getMethod(method, params);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return MethodHandles.publicLookup().unreflect(f);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        private static final MethodHandle CLOSE = getMethod(InventoryView.class, "close");
        private static final MethodHandle EQUALS = getMethod(Object.class, "equals", Object.class);
        private static final MethodHandle GET_PLAYER = getMethod(InventoryView.class, "getPlayer");
        private static final MethodHandle HASHCODE = getMethod(Object.class, "hashCode");
        private static final MethodHandle TOP_INVENTORY = getMethod(InventoryView.class, "getTopInventory");
    }

    private static interface ThrowingConsumer<T> {
        void accept(T t) throws ClassNotFoundException;
    }

    public static boolean checkYSafe(double y, World world) {
        if (!SUPPORT_WORLD_HEIGHT || world == null)
            return y >= 0 && y <= 255;

        try {
            return y >= world.getMinHeight() && y <= world.getMaxHeight();
        } catch (Throwable t) {
            SUPPORT_WORLD_HEIGHT = false;
            return y >= 0 && y <= 255;
        }
    }

    public static NamespacedKey getKey(String raw) {
        return getKey(raw, "minecraft");
    }

    public static NamespacedKey getKey(String raw, String defaultNamespace) {
        int index = raw.indexOf(':');
        if (index == -1) {
            raw = defaultNamespace + ":" + raw.toLowerCase(Locale.ROOT);
        } else {
            raw = raw.substring(0, index) + raw.substring(index).toLowerCase(Locale.ROOT);
        }
        return NamespacedKey.fromString(raw);
    }

    public static int getMaxNameLength(EntityType type) {
        return isUsing1_13API() ? 256 : 64;
    }

    public static String getMinecraftPackage() {
        if (MINECRAFT_PACKAGE == null) {
            int[] version = getVersion();
            if (version == null)
                throw new IllegalStateException();
            String versionString = "v" + version[0] + "_" + version[1] + "_R";
            ThrowingConsumer<String> versionChecker = s -> Class
                    .forName("org.bukkit.craftbukkit." + s + ".CraftServer");
            if (Bukkit.getServer().getClass().getName().equals("org.bukkit.craftbukkit.CraftServer")) {
                Messaging.log("Using mojmapped server, avoiding server package checks");
                versionChecker = s -> Class.forName("net.citizensnpcs.nms." + s + ".util.NMSImpl");
            }
            String revision = null;
            for (int i = 1; i <= 6; i++) {
                try {
                    versionChecker.accept(versionString + i);
                    revision = versionString + i;
                    break;
                } catch (ClassNotFoundException e) {
                }
            }
            if (revision == null)
                throw new IllegalStateException();
            MINECRAFT_PACKAGE = revision;
        }
        return MINECRAFT_PACKAGE;

    }

    public static int[] getVersion() {
        if (BUKKIT_VERSION == null) {
            String version = Bukkit.getBukkitVersion();

            if (version == null || version.isEmpty())
                return BUKKIT_VERSION = new int[] { 1, 8, 8 };

            String[] parts = version.split("\\.");
            if (parts[1].contains("-")) {
                parts[1] = parts[1].split("-")[0];
            }
            if (parts[2].contains("-")) {
                parts[2] = parts[2].split("-")[0];
            }
            if (parts.length == 3) {
                return BUKKIT_VERSION = new int[] { Integer.parseInt(parts[0]), Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]) };
            }
            return BUKKIT_VERSION = new int[] { Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) };
        }
        return BUKKIT_VERSION;
    }

    public static boolean isRegistryKeyed(Class<?> clazz) {
        if (NON_REGISTRY_CLASSES.containsKey(clazz))
            return false;
        try {
            return SUPPORTS_KEYED && Keyed.class.isAssignableFrom(clazz)
                    && Bukkit.getRegistry((Class<? extends Keyed>) clazz) != null;
        } catch (Exception ex) {
            NON_REGISTRY_CLASSES.put(clazz, true);
            return false;
        }
    }

    public static boolean isUsing1_13API() {
        if (using1_13API == null) {
            try {
                Enchantment.getByKey(Enchantment.getByName("ARROW_DAMAGE").getKey());
                using1_13API = true;
            } catch (Exception ex) {
                using1_13API = false;
            } catch (NoSuchMethodError ex) {
                using1_13API = false;
            }
        }
        return using1_13API;
    }

    public static Duration parseDuration(String raw, TimeUnit defaultUnits) {
        if (defaultUnits == null) {
            Integer ticks = Ints.tryParse(raw);
            if (ticks != null)
                return Duration.ofMillis(ticks * 50);
        } else if (NUMBER_MATCHER.matcher(raw).matches())
            return Duration.of(Longs.tryParse(raw), toChronoUnit(defaultUnits));
        if (raw.endsWith("t"))
            return Duration.ofMillis(Integer.parseInt(raw.substring(0, raw.length() - 1)) * 50);
        raw = DAY_MATCHER.matcher(raw).replaceFirst("P$1").replace("min", "m").replace("hr", "h");
        if (raw.charAt(0) != 'P') {
            raw = "PT" + raw;
        }
        return Duration.parse(raw);
    }

    public static ItemStack parseItemStack(ItemStack base, String item) {
        if (base == null || base.getType() == Material.AIR) {
            base = new ItemStack(Material.STONE, 1);
        }
        if (item.contains("["))
            return Bukkit.getItemFactory().createItemStack(item);

        String[] parts = Iterables.toArray(Splitter.on(',').split(item), String.class);
        if (parts.length == 0)
            return base;
        base.setType(Material.matchMaterial(parts[0]));
        if (parts.length > 1) {
            base.setAmount(Ints.tryParse(parts[1]));
        }
        if (parts.length > 2) {
            Integer durability = Ints.tryParse(parts[2]);
            base.setDurability(durability.shortValue());
        }
        return base;
    }

    private static ChronoUnit toChronoUnit(TimeUnit tu) {
        switch (tu) {
            case NANOSECONDS:
                return ChronoUnit.NANOS;
            case MICROSECONDS:
                return ChronoUnit.MICROS;
            case MILLISECONDS:
                return ChronoUnit.MILLIS;
            case SECONDS:
                return ChronoUnit.SECONDS;
            case MINUTES:
                return ChronoUnit.MINUTES;
            case HOURS:
                return ChronoUnit.HOURS;
            case DAYS:
                return ChronoUnit.DAYS;
            default:
                throw new AssertionError();
        }
    }

    private static int[] BUKKIT_VERSION = null;
    private static final Pattern DAY_MATCHER = Pattern.compile("(\\d+d)");
    private static String MINECRAFT_PACKAGE;
    private static final Map<Class<?>, Boolean> NON_REGISTRY_CLASSES = new WeakHashMap<Class<?>, Boolean>();
    private static final Pattern NUMBER_MATCHER = Pattern.compile("(\\d+)");
    private static boolean SUPPORT_WORLD_HEIGHT = true;
    private static boolean SUPPORTS_KEYED;
    private static Boolean using1_13API;

    static {
        try {
            Class.forName("org.bukkit.Keyed");
            SUPPORTS_KEYED = true;
        } catch (ClassNotFoundException e) {
        }
    }
}
