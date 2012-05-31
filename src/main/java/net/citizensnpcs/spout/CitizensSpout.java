package net.citizensnpcs.spout;

import net.citizensnpcs.util.Messaging;

import org.spout.api.plugin.CommonPlugin;

public class CitizensSpout extends CommonPlugin {

    @Override
    public void onDisable() {
        Messaging.logF("v%s disabled.", getDescription().getVersion());
    }

    @Override
    public void onEnable() {
        Messaging.logF("v%s enabled.", getDescription().getVersion());
    }
}
