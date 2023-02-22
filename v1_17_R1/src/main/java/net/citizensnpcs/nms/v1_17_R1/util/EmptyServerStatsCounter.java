package net.citizensnpcs.nms.v1_17_R1.util;

import java.util.Collections;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.gson.JsonObject;
import com.mojang.datafixers.DataFixer;

import net.citizensnpcs.api.CitizensAPI;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.world.entity.player.Player;

public class EmptyServerStatsCounter extends ServerStatsCounter {
    public EmptyServerStatsCounter() {
        super(null, CitizensAPI.getDataFolder());
    }

    private Set<Stat<?>> getDirty() {
        return Collections.EMPTY_SET;
    }

    private <T> Optional<Stat<T>> getStat(StatType<T> statisticwrapper, String s) {
        return Optional.absent();
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
        return "{\"stats\":{},\"DataVersion\":" + Integer.valueOf(SharedConstants.getCurrentVersion().getWorldVersion())
                + "}";
    }

    private static CompoundTag fromJson(JsonObject jsonobject) {
        return new CompoundTag();
    }

    private static <T> ResourceLocation getKey(Stat<T> statistic) {
        return statistic.getType().getRegistry().getKey(statistic.getValue());
    }
}
