package net.citizensnpcs.nms.v1_16_R3.util;

import java.lang.invoke.MethodHandle;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.mojang.datafixers.DataFixer;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_16_R3.Advancement;
import net.minecraft.server.v1_16_R3.AdvancementDataPlayer;
import net.minecraft.server.v1_16_R3.AdvancementDataWorld;
import net.minecraft.server.v1_16_R3.AdvancementProgress;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.MinecraftKey;
import net.minecraft.server.v1_16_R3.PlayerList;

public class EmptyAdvancementDataPlayer extends AdvancementDataPlayer {
    public EmptyAdvancementDataPlayer(DataFixer datafixer, PlayerList playerlist, EntityPlayer entityplayer) {
        super(datafixer, playerlist, new EmptyServerAdvancementManager(), CitizensAPI.getDataFolder(), entityplayer);
        this.b();
    }

    @Override
    public void a() {
        super.a();
    }

    @Override
    public void a(Advancement advancement) {
    }

    @Override
    public void a(EntityPlayer entityplayer) {
    }

    @Override
    public void b() {
        clear(this);
    }

    @Override
    public void b(EntityPlayer entityplayer) {
    }

    @Override
    public AdvancementProgress getProgress(Advancement advancement) {
        return new AdvancementProgress();
    }

    @Override
    public boolean grantCriteria(Advancement advancement, String s) {
        return false;
    }

    @Override
    public boolean revokeCritera(Advancement advancement, String s) {
        return false;
    }

    private static class EmptyServerAdvancementManager extends AdvancementDataWorld {
        public EmptyServerAdvancementManager() {
            super(null);
        }

        @Override
        public Advancement a(MinecraftKey minecraftkey) {
            return null;
        }

        @Override
        public Collection<Advancement> getAdvancements() {
            return Collections.emptyList();
        }
    }

    public static void clear(AdvancementDataPlayer data) {
        data.a();
        data.data.clear();
        try {
            ((Set<?>) H.invoke(data)).clear();
            ((Set<?>) I.invoke(data)).clear();
            ((Set<?>) J.invoke(data)).clear();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static final MethodHandle H = NMS.getGetter(AdvancementDataPlayer.class, "h");
    private static final MethodHandle I = NMS.getGetter(AdvancementDataPlayer.class, "i");
    private static final MethodHandle J = NMS.getGetter(AdvancementDataPlayer.class, "j");
}
