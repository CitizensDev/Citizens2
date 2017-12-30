package net.citizensnpcs.util;

import org.bukkit.entity.Player;

public enum PlayerAnimation {
    ARM_SWING,
    ARM_SWING_OFFHAND,
    CRIT,
    EAT_FOOD,
    HURT,
    MAGIC_CRIT,
    SIT,
    SLEEP,
    SNEAK,
    START_USE_MAINHAND_ITEM,
    START_USE_OFFHAND_ITEM,
    STOP_SITTING,
    STOP_SLEEPING,
    STOP_SNEAKING,
    STOP_USE_ITEM;

    public void play(Player player) {
        play(player, 64);
    }

    public void play(Player player, int radius) {
        NMS.playAnimation(this, player, radius);
    }
}
