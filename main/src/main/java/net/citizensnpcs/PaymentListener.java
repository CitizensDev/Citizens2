package net.citizensnpcs;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.event.PlayerCreateNPCEvent;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

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
        boolean hasAccount = provider.hasAccount(event.getCreator());
        if (!hasAccount || event.getCreator().hasPermission("citizens.npc.ignore-cost"))
            return;
        double cost = Setting.NPC_COST.asDouble();
        EconomyResponse response = provider.withdrawPlayer(event.getCreator(), cost);
        if (!response.transactionSuccess()) {
            event.setCancelled(true);
            event.setCancelReason(response.errorMessage);
            return;
        }
        String formattedCost = provider.format(cost);
        Messaging.sendTr(event.getCreator(), Messages.MONEY_WITHDRAWN, formattedCost);
    }
}
