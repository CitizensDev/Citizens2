package net.citizensnpcs.nms.v1_14_R1.util;

import java.lang.invoke.MethodHandle;
import java.util.Set;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_14_R1.Advancement;
import net.minecraft.server.v1_14_R1.AdvancementDataPlayer;
import net.minecraft.server.v1_14_R1.AdvancementProgress;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.MinecraftServer;

public class EmptyAdvancementDataPlayer extends AdvancementDataPlayer {
    public EmptyAdvancementDataPlayer(MinecraftServer minecraftserver, EntityPlayer entityplayer) {
        super(minecraftserver, CitizensAPI.getDataFolder(), entityplayer);
        this.b();
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
    public void c() {
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
            ((Set<?>) G.invoke(data)).clear();
            ((Set<?>) H.invoke(data)).clear();
            ((Set<?>) I.invoke(data)).clear();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static final MethodHandle G = NMS.getGetter(AdvancementDataPlayer.class, "g");
    private static final MethodHandle H = NMS.getGetter(AdvancementDataPlayer.class, "h");
    private static final MethodHandle I = NMS.getGetter(AdvancementDataPlayer.class, "i");
}
