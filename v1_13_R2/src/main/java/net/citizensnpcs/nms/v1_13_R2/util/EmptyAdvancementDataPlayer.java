package net.citizensnpcs.nms.v1_13_R2.util;

import java.lang.reflect.Field;
import java.util.Set;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_13_R2.Advancement;
import net.minecraft.server.v1_13_R2.AdvancementDataPlayer;
import net.minecraft.server.v1_13_R2.AdvancementProgress;
import net.minecraft.server.v1_13_R2.EntityPlayer;
import net.minecraft.server.v1_13_R2.MinecraftServer;

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
            ((Set<?>) G.get(data)).clear();
            ((Set<?>) H.get(data)).clear();
            ((Set<?>) I.get(data)).clear();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static final Field G = NMS.getField(AdvancementDataPlayer.class, "g");
    private static final Field H = NMS.getField(AdvancementDataPlayer.class, "h");
    private static final Field I = NMS.getField(AdvancementDataPlayer.class, "i");
}
