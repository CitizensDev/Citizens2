package net.citizensnpcs.nms.v1_12_R1.util;

import java.io.File;

import net.minecraft.server.v1_12_R1.Advancement;
import net.minecraft.server.v1_12_R1.AdvancementDataPlayer;
import net.minecraft.server.v1_12_R1.AdvancementProgress;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.MinecraftServer;

public class EmptyAdvancementDataPlayer extends AdvancementDataPlayer {
    public EmptyAdvancementDataPlayer(MinecraftServer minecraftserver, File file, EntityPlayer entityplayer) {
        super(minecraftserver, file, entityplayer);
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
}
