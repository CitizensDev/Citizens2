package net.citizensnpcs.trait.shop;

import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.persistence.PersistenceLoader;
import net.citizensnpcs.api.persistence.PersisterRegistry;

public abstract class NPCShopAction implements Cloneable {
    @Override
    public NPCShopAction clone() {
        try {
            return (NPCShopAction) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new Error(e);
        }
    }

    public abstract boolean grant(HumanEntity entity);

    public abstract boolean take(HumanEntity entity);

    public static class ItemAction extends NPCShopAction {
        @Persist
        public List<ItemStack> items = Lists.newArrayList();

        public ItemAction() {
        }

        public ItemAction(ItemStack... items) {
            this(Arrays.asList(items));
        }

        public ItemAction(List<ItemStack> items) {
            this.items = items;
        }

        @Override
        public boolean grant(HumanEntity entity) {
            return false;
        }

        @Override
        public boolean take(HumanEntity entity) {
            return false;
        }
    }

    public static class MoneyAction extends NPCShopAction {
        @Persist
        public int money;

        public MoneyAction() {
        }

        public MoneyAction(int money) {
            this.money = money;
        }

        @Override
        public boolean grant(HumanEntity entity) {
            return false;
        }

        @Override
        public boolean take(HumanEntity entity) {
            return false;
        }
    }

    public static class PermissionAction extends NPCShopAction {
        @Persist
        public List<String> permissions = Lists.newArrayList();

        public PermissionAction() {
        }

        public PermissionAction(List<String> permissions) {
            this.permissions = permissions;
        }

        @Override
        public boolean grant(HumanEntity entity) {
            return false;
        }

        @Override
        public boolean take(HumanEntity entity) {
            return false;
        }
    }

    public static PersisterRegistry<NPCShopAction> REGISTRY = PersistenceLoader.createRegistry(NPCShopAction.class);
    static {
        REGISTRY.register("items", ItemAction.class);
        REGISTRY.register("permissions", PermissionAction.class);
        REGISTRY.register("money", MoneyAction.class);
    }
}