package net.citizensnpcs.nms.v1_19_R3.util;

import java.lang.invoke.MethodHandle;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.datafixers.DataFixer;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.util.NMS;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

public class EmptyAdvancementDataPlayer extends PlayerAdvancements {
    public EmptyAdvancementDataPlayer(DataFixer datafixer, PlayerList playerlist, ServerPlayer entityplayer) {
        super(datafixer, playerlist, new EmptyServerAdvancementManager(), CitizensAPI.getDataFolder().toPath(),
                entityplayer);
        this.save();
    }

    @Override
    public boolean award(Advancement advancement, String s) {
        return false;
    }

    @Override
    public void flushDirty(ServerPlayer entityplayer) {
    }

    @Override
    public AdvancementProgress getOrStartProgress(Advancement advancement) {
        return new AdvancementProgress();
    }

    @Override
    public boolean revoke(Advancement advancement, String s) {
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
    public void setSelectedTab(Advancement advancement) {
    }

    @Override
    public void stopListening() {
        super.stopListening();
    }

    private static class EmptyServerAdvancementManager extends ServerAdvancementManager {
        public EmptyServerAdvancementManager() {
            super(null);
        }

        @Override
        public Advancement getAdvancement(ResourceLocation minecraftkey) {
            return null;
        }

        @Override
        public Collection<Advancement> getAllAdvancements() {
            return Collections.emptyList();
        }
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
