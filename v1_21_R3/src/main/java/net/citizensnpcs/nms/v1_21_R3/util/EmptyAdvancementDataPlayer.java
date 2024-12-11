package net.citizensnpcs.nms.v1_21_R3.util;

import java.lang.invoke.MethodHandle;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.DynamicOps;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.util.NMS;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

public class EmptyAdvancementDataPlayer extends PlayerAdvancements {
    public EmptyAdvancementDataPlayer(DataFixer datafixer, PlayerList playerlist, ServerPlayer entityplayer) {
        super(datafixer, playerlist, new EmptyServerAdvancementManager(), CitizensAPI.getDataFolder().toPath(),
                entityplayer);
        save();
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
        super.stopListening();
    }

    private static class EmptyProvider implements HolderLookup.Provider {
        @Override
        public <V> RegistryOps<V> createSerializationContext(DynamicOps<V> var0) {
            return null;
        }

        @Override
        public Stream<ResourceKey<? extends Registry<?>>> listRegistryKeys() {
            return Stream.empty();
        }

        @Override
        public <T> Optional<? extends RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> arg0) {
            return Optional.empty();
        }
    }

    private static class EmptyServerAdvancementManager extends ServerAdvancementManager {
        public EmptyServerAdvancementManager() {
            super(new EmptyProvider());
        }

        @Override
        public AdvancementHolder get(ResourceLocation minecraftkey) {
            return null;
        }

        @Override
        public Collection<AdvancementHolder> getAllAdvancements() {
            return Collections.emptyList();
        }
    }

    private static void clear(PlayerAdvancements data) {
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
