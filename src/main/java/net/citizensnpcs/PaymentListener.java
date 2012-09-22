package net.citizensnpcs;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.event.PlayerCreateNPCEvent;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.google.common.base.Preconditions;

public class PaymentListener implements Listener {
    private final Economy provider;

    public PaymentListener(Economy provider) {
        Preconditions.checkNotNull(provider, "provider cannot be null");
        this.provider = provider;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCreateNPC(PlayerCreateNPCEvent event) {
        String name = event.getCreator().getName();
        boolean hasAccount = provider.hasAccount(name);
        if (!hasAccount || event.getCreator().hasPermission("citizens.npc.ignore-cost"))
            return;
        double cost = Setting.NPC_COST.asDouble();
        boolean hasEnough = provider.has(name, cost);
        if (!hasEnough) {
            event.setCancelled(true);
            event.setCancelReason(String.format("Need at least %s.", provider.format(cost)));
            return;
        }
        provider.withdrawPlayer(name, cost);
        Messaging.sendF(event.getCreator(), ChatColor.GREEN + "Withdrew %s for your NPC.",
                StringHelper.wrap(provider.format(cost)));
    }
}
