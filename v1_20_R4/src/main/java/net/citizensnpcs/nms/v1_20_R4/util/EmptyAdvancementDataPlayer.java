package net.citizensnpcs.nms.v1_20_R4.util;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.datafixers.DataFixer;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.util.NMS;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

public class EmptyAdvancementDataPlayer extends PlayerAdvancements {
    public EmptyAdvancementDataPlayer(DataFixer datafixer, PlayerList playerlist,
            ServerAdvancementManager advancementdataworld, File file, ServerPlayer entityplayer) {
        super(datafixer, playerlist, advancementdataworld, CitizensAPI.getDataFolder().toPath(), entityplayer);
        this.save();
    }

    @Override
    public boolean award(AdvancementHolder advancement, String s) {
        return false;
    }

    @Override
    public void flushDirty(ServerPlayer entityplayer) {
    }

    @Override
    public AdvancementProgress getOrStartProgress(AdvancementHolder advancement) {
        return new AdvancementProgress();
    }

    @Override
    public void reload(ServerAdvancementManager sam) {
    }

    @Override
    public boolean revoke(AdvancementHolder advancement, String s) {
        return false;
    }

    @Override
    public void save() {
        clear(this);
    }

    @Override
    public void setPlayer(ServerPlayer entityplayer) {
    }

    @Override
    public void setSelectedTab(AdvancementHolder advancement) {
    }

    @Override
    public void stopListening() {
    }

    public static void clear(PlayerAdvancements data) {
        data.stopListening();
        try {
            ((Map<?, ?>) PROGRESS.invoke(data)).clear();
            for (MethodHandle handle : SETS) {
                ((Set<?>) handle.invoke(data)).clear();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static final MethodHandle PROGRESS = NMS.getFirstGetter(PlayerAdvancements.class, Map.class);
    private static final List<MethodHandle> SETS = NMS.getFieldsOfType(PlayerAdvancements.class, Set.class);
}
