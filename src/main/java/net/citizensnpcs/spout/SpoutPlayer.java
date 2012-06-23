package net.citizensnpcs.spout;

import net.citizensnpcs.api.abstraction.Equipment;
import net.citizensnpcs.api.abstraction.ItemStack;
import net.citizensnpcs.api.abstraction.MobType;
import net.citizensnpcs.api.abstraction.entity.Player;

import org.spout.vanilla.controller.VanillaControllerTypes;
import org.spout.vanilla.controller.living.player.VanillaPlayer;
import org.spout.vanilla.controller.source.HealthChangeReason;

public class SpoutPlayer extends SpoutEntity implements Player {
    private final VanillaPlayer player;

    public SpoutPlayer(VanillaPlayer player) {
        super(player.getParent());
        this.player = player;
    }

    @Override
    public int getHealth() {
        return player.getHealth();
    }

    @Override
    public MobType getType() {
        return SpoutConverter.toMobType(VanillaControllerTypes.HUMAN);
    }

    @Override
    public void setHealth(int health) {
        player.setHealth(health, HealthChangeReason.UNKNOWN);
    }

    @Override
    public String getName() {
        return player.getPlayer().getName();
    }

    @Override
    public boolean hasPermission(String perm) {
        return player.getPlayer().hasPermission(perm);
    }

    @Override
    public void sendMessage(String message) {
        player.getPlayer().sendMessage(message);
    }

    @Override
    public void useCommand(String cmd) {
        // TODO Auto-generated method stub

    }

    @Override
    public ItemStack getEquipment(Equipment slot) {
        switch (slot) {
        case CARRIED:
            return SpoutConverter.toItemStack(player.getInventory().getCurrentItem());
        case HELMET:
            return SpoutConverter.toItemStack(player.getInventory().getArmor().getHelmet().getItem());
        case BOOTS:
            return SpoutConverter.toItemStack(player.getInventory().getArmor().getBoots().getItem());
        case CHESTPLATE:
            return SpoutConverter.toItemStack(player.getInventory().getArmor().getChestPlate().getItem());
        case LEGGINGS:
            return SpoutConverter.toItemStack(player.getInventory().getArmor().getLeggings().getItem());
        default:
            return null;
        }
    }

    @Override
    public void setEquipment(Equipment slot, ItemStack item) {
        switch (slot) {
        case CARRIED:
            player.getInventory().setCurrentItem(SpoutConverter.fromItemStack(item));
        case HELMET:
            player.getInventory().setItem(0, SpoutConverter.fromItemStack(item));
        case BOOTS:
            player.getInventory().setItem(3, SpoutConverter.fromItemStack(item));
        case CHESTPLATE:
            player.getInventory().setItem(1, SpoutConverter.fromItemStack(item));
        case LEGGINGS:
            player.getInventory().setItem(2, SpoutConverter.fromItemStack(item));
        }
    }

    @Override
    public boolean isOnline() {
        return player.getPlayer().isOnline();
    }

    @Override
    public void setArmor(ItemStack[] armor) {
        for (int i = 0; i < armor.length; i++) {
            player.getInventory().setItem(i, SpoutConverter.fromItemStack(armor[i]));
        }
    }
}
