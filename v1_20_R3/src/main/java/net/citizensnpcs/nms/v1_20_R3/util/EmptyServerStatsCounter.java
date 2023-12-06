package net.citizensnpcs.nms.v1_20_R3.util;

import com.mojang.datafixers.DataFixer;

import net.citizensnpcs.api.CitizensAPI;
import net.minecraft.SharedConstants;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.world.entity.player.Player;

public class EmptyServerStatsCounter extends ServerStatsCounter {
    public EmptyServerStatsCounter() {
        super(null, CitizensAPI.getDataFolder());
    }

    @Override
    public void markAllDirty() {
    }

    @Override
    public void parseLocal(DataFixer datafixer, String s) {
    }

    @Override
    public void save() {
    }

    @Override
    public void sendStats(ServerPlayer entityplayer) {
    }

    @Override
    public void setValue(Player entityhuman, Stat<?> statistic, int i) {
    }

    @Override
    protected String toJson() {
        return "{\"stats\":{},\"DataVersion\":"
                + Integer.valueOf(SharedConstants.getCurrentVersion().getDataVersion().getVersion()) + "}";
    }
}
