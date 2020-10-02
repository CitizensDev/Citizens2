package net.citizensnpcs.nms.v1_16_R2.util;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.util.Set;

import com.mojang.datafixers.DataFixer;

import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_16_R2.Advancement;
import net.minecraft.server.v1_16_R2.AdvancementDataPlayer;
import net.minecraft.server.v1_16_R2.AdvancementDataWorld;
import net.minecraft.server.v1_16_R2.AdvancementProgress;
import net.minecraft.server.v1_16_R2.EntityPlayer;
import net.minecraft.server.v1_16_R2.PlayerList;

public class EmptyAdvancementDataPlayer extends AdvancementDataPlayer {
    public EmptyAdvancementDataPlayer(DataFixer datafixer, PlayerList playerlist,
            AdvancementDataWorld advancementdataworld, File file, EntityPlayer entityplayer) {
        super(datafixer, playerlist, advancementdataworld, file, entityplayer);
        this.b();
    }

    @Override
    public void a() {
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
