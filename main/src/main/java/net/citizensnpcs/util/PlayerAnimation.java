package net.citizensnpcs.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.trait.ArmorStandTrait;
import net.citizensnpcs.trait.SitTrait;
import net.citizensnpcs.trait.SleepTrait;

public enum PlayerAnimation {
    ARM_SWING,
    ARM_SWING_OFFHAND,
    CRIT,
    EAT_FOOD,
    HURT,
    LEAVE_BED,
    MAGIC_CRIT,
    SIT,
    SLEEP,
    SNEAK,
    START_ELYTRA,
    START_USE_MAINHAND_ITEM,
    START_USE_OFFHAND_ITEM,
    STOP_ELYTRA,
    STOP_SITTING,
    STOP_SLEEPING,
    STOP_SNEAKING,
    STOP_USE_ITEM;

    public void play(Player player) {
        play(player, 64);
    }

    public void play(Player player, int radius) {
        if (this == SIT) {
            if (player instanceof NPCHolder) {
                ((NPCHolder) player).getNPC().getOrAddTrait(SitTrait.class).setSitting(player.getLocation());
                return;
            }
            player.setMetadata("citizens.sitting", new FixedMetadataValue(CitizensAPI.getPlugin(), true));
            NPCRegistry registry = CitizensAPI.getNamedNPCRegistry("PlayerAnimationImpl");
            if (registry == null) {
                registry = CitizensAPI.createNamedNPCRegistry("PlayerAnimationImpl", new MemoryNPCDataStore());
            }
            final NPC holder = registry.createNPC(EntityType.ARMOR_STAND, "");
            holder.getOrAddTrait(ArmorStandTrait.class).setAsPointEntity();
            holder.spawn(player.getLocation());
            new BukkitRunnable() {
                @Override
                public void cancel() {
                    super.cancel();
                    holder.destroy();
                }

                @Override
                public void run() {
                    if (!player.isValid() || !player.hasMetadata("citizens.sitting")
                            || !player.getMetadata("citizens.sitting").get(0).asBoolean()) {
                        cancel();
                        return;
                    }
                    if (!NMS.getPassengers(holder.getEntity()).contains(player)) {
                        NMS.mount(holder.getEntity(), player);
                    }
                }
            }.runTaskTimer(CitizensAPI.getPlugin(), 0, 1);
            return;
        } else if (this == STOP_SITTING) {
            if (player instanceof NPCHolder) {
                ((NPCHolder) player).getNPC().getOrAddTrait(SitTrait.class).setSitting(null);
                return;
            }
            player.setMetadata("citizens.sitting", new FixedMetadataValue(CitizensAPI.getPlugin(), false));
            NMS.mount(player, null);
            return;
        } else if (this == SLEEP) {
            if (player instanceof NPCHolder) {
                ((NPCHolder) player).getNPC().getOrAddTrait(SleepTrait.class).setSleeping(player.getLocation());
            } else {
                NMS.sleep(player, true);
            }
            return;
        } else if (this == STOP_SLEEPING) {
            if (player instanceof NPCHolder) {
                ((NPCHolder) player).getNPC().getOrAddTrait(SleepTrait.class).setSleeping(null);
            } else {
                NMS.sleep(player, false);
            }
            return;
        } else if (this == STOP_USE_ITEM || this == START_USE_MAINHAND_ITEM || this == START_USE_OFFHAND_ITEM) {
            NMS.playAnimation(this, player, radius);
            if (player.hasMetadata("citizens-using-item-id")) {
                Bukkit.getScheduler().cancelTask(player.getMetadata("citizens-using-item-id").get(0).asInt());
                player.removeMetadata("citizens-using-item-id", CitizensAPI.getPlugin());
            }
            if (this == STOP_USE_ITEM)
                return;
            if (player.hasMetadata("citizens-using-item-remaining-ticks")) {
                int remainingTicks = player.getMetadata("citizens-using-item-remaining-ticks").get(0).asInt();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!NMS.isValid(player)) {
                            cancel();
                            return;
                        }
                        NMS.playAnimation(PlayerAnimation.STOP_USE_ITEM, player, radius);
                        NMS.playAnimation(PlayerAnimation.this, player, radius);
                        if (!player.hasMetadata("citizens-using-item-id")) {
                            player.setMetadata("citizens-using-item-id",
                                    new FixedMetadataValue(CitizensAPI.getPlugin(), getTaskId()));
                        }
                    }
                }.runTaskTimer(CitizensAPI.getPlugin(), Math.max(0, remainingTicks + 1),
                        Math.max(1, remainingTicks + 1));
            }
            return;
        }
        NMS.playAnimation(this, player, radius);
    }
}
