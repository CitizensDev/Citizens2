package net.citizensnpcs.trait.shop;

import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.expr.CompiledExpression;
import net.citizensnpcs.api.expr.ExpressionEngine.ExpressionCompileException;
import net.citizensnpcs.api.expr.ExpressionScope;
import net.citizensnpcs.api.gui.InputMenus;
import net.citizensnpcs.api.gui.InventoryMenuPage;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.trait.ShopTrait.NPCShopStorage;
import net.citizensnpcs.util.InventoryMultiplexer;
import net.citizensnpcs.util.Util;

public class ConditionAction extends NPCShopAction {
    @Persist
    private String condition;
    private CompiledExpression expression;

    public ConditionAction() {
    }

    public ConditionAction(String expression) {
        this.condition = expression;
    }

    private void compile() {
        try {
            this.expression = CitizensAPI.getExpressionRegistry().compile(condition);
        } catch (ExpressionCompileException e) {
            e.printStackTrace();
        }
    }

    private ExpressionScope createTransactionScope(Player player) {
        ExpressionScope scope = new ExpressionScope();
        scope.setPlayer(player);
        return scope;
    }

    @Override
    public String describe() {
        return condition;
    }

    @Override
    public int getMaxRepeats(Entity entity, InventoryMultiplexer inventory) {
        return -1;
    }

    @Override
    public Transaction grant(NPCShopStorage storage, Entity entity, InventoryMultiplexer inventory, int repeats) {
        if (condition == null)
            return Transaction.success();
        if (expression == null) {
            compile();
        }
        ExpressionScope scope = createTransactionScope(entity instanceof Player ? (Player) entity : null);
        return Transaction.create(() -> expression.evaluateAsBoolean(scope), () -> {
        }, () -> {
        });
    }

    public void setExpression(String expression) {
        this.condition = CitizensAPI.getExpressionRegistry().applyDefaultExpressionMarkup(expression);
        this.expression = null;
    }

    @Override
    public Transaction take(NPCShopStorage storage, Entity entity, InventoryMultiplexer inventory, int repeats) {
        if (condition == null)
            return Transaction.success();
        if (expression == null) {
            compile();
        }
        ExpressionScope scope = createTransactionScope(entity instanceof Player ? (Player) entity : null);
        return Transaction.create(() -> expression.evaluateAsBoolean(scope), () -> {
        }, () -> {
        });
    }

    public static class ConditionActionGUI implements GUI {
        @Override
        public boolean canUse(HumanEntity entity) {
            return entity.hasPermission("citizens.npc.shop.editor.actions.edit-condition");
        }

        @Override
        public InventoryMenuPage createEditor(NPCShopAction previous, Consumer<NPCShopAction> callback) {
            ConditionAction action = previous == null ? new ConditionAction() : (ConditionAction) previous;
            return InputMenus.stringSetter("Condition", () -> action.condition, s -> {
                action.setExpression(s);
                callback.accept(action);
            });
        }

        @Override
        public ItemStack createMenuItem(NPCShopAction previous) {
            String description = null;
            if (previous != null) {
                ConditionAction old = (ConditionAction) previous;
                description = old.describe();
            }
            return Util.createItem(Material.ENCHANTED_BOOK, "Condition", description);
        }

        @Override
        public boolean manages(NPCShopAction action) {
            return action instanceof ConditionAction;
        }
    }
}