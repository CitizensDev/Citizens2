package net.citizensnpcs;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.event.PlayerCreateNPCEvent;
import net.citizensnpcs.util.Messages;
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
        String formattedCost = provider.format(cost);
        if (!hasEnough) {
            event.setCancelled(true);
            event.setCancelReason(Messaging.tr(Messages.MINIMUM_COST_REQUIRED, formattedCost));
            return;
        }
        provider.withdrawPlayer(name, cost);
        String message = Messaging.tr(Messages.MONEY_WITHDRAWN, StringHelper.wrap(formattedCost));
        Messaging.send(event.getCreator(), ChatColor.GREEN + message);
    }
}
