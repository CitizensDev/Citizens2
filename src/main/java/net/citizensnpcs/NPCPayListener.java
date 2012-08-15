package net.citizensnpcs;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.event.PlayerCreateNPCEvent;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.google.common.base.Preconditions;

public class NPCPayListener implements Listener {
    private final Economy provider;

    public NPCPayListener(Economy provider) {
        Preconditions.checkNotNull(provider, "provider cannot be null");
        this.provider = provider;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCreateNPC(PlayerCreateNPCEvent event) {
        String name = event.getCreator().getName();
        boolean hasAccount = provider.hasAccount(name);
        if (!hasAccount)
            return;
        double cost = Setting.NPC_COST.asDouble();
        boolean hasEnough = provider.has(name, cost);
        if (!hasEnough) {
            event.setCancelled(true);
            event.setCancelReason(String.format("Need at least %s.", provider.format(cost)));
            return;
        }
        provider.bankWithdraw(name, cost);
    }
}
