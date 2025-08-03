package net.citizensnpcs.trait.shop;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import net.citizensnpcs.api.gui.InventoryMenuPage;
import net.citizensnpcs.api.persistence.PersistenceLoader;
import net.citizensnpcs.api.persistence.PersisterRegistry;
import net.citizensnpcs.trait.ShopTrait.NPCShopStorage;
import net.citizensnpcs.util.InventoryMultiplexer;

public abstract class NPCShopAction implements Cloneable {
    @Override
    public NPCShopAction clone() {
        try {
            return (NPCShopAction) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new Error(e);
        }
    }

    public abstract String describe();

    public abstract int getMaxRepeats(Entity entity, InventoryMultiplexer inventory);

    public abstract Transaction grant(NPCShopStorage storage, Entity entity, InventoryMultiplexer inventory,
            int repeats);

    public Transaction grant(Player player, int repeats) {
        return grant(new NPCShopStorage(), player, new InventoryMultiplexer(player.getInventory()), repeats);
    }

    public abstract Transaction take(NPCShopStorage storage, Entity entity, InventoryMultiplexer inventory,
            int repeats);

    public Transaction take(Player player, int repeats) {
        return take(new NPCShopStorage(), player, new InventoryMultiplexer(player.getInventory()), repeats);
    }

    public static interface GUI {
        public boolean canUse(HumanEntity entity);

        public InventoryMenuPage createEditor(NPCShopAction previous, Consumer<NPCShopAction> callback);

        public ItemStack createMenuItem(NPCShopAction previous);

        public boolean manages(NPCShopAction action);
    }

    public static class Transaction {
        private final Runnable execute;
        private final Supplier<Boolean> possible;
        private final Runnable rollback;

        public Transaction(Supplier<Boolean> isPossible, Runnable execute, Runnable rollback) {
            this.possible = isPossible;
            this.execute = execute;
            this.rollback = rollback;
        }

        public boolean isPossible() {
            return possible.get();
        }

        public void rollback() {
            rollback.run();
        }

        public void run() {
            execute.run();
        }

        public static Transaction compose(Collection<Transaction> txn) {
            if (txn.isEmpty())
                return success();
            return create(() -> txn.stream().allMatch(t -> t == null || t.isPossible()),
                    () -> txn.forEach(Transaction::run), () -> txn.forEach(Transaction::rollback));
        }

        public static Transaction compose(Transaction... txn) {
            return compose(Arrays.asList(txn));
        }

        public static Transaction create(Supplier<Boolean> isPossible, Runnable execute, Runnable rollback) {
            return new Transaction(isPossible, execute, rollback);
        }

        public static Transaction fail() {
            return create(() -> false, () -> {
            }, () -> {
            });
        }

        public static Transaction success() {
            return create(() -> true, () -> {
            }, () -> {
            });
        }
    }

    public static Iterable<GUI> getGUIs() {
        return GUI;
    }

    public static void register(Class<? extends NPCShopAction> clazz, String type, GUI gui) {
        REGISTRY.register(type, clazz);
        GUI.add(gui);
    }

    private static final List<GUI> GUI = Lists.newArrayList();
    private static final PersisterRegistry<NPCShopAction> REGISTRY = PersistenceLoader
            .createRegistry(NPCShopAction.class);
}