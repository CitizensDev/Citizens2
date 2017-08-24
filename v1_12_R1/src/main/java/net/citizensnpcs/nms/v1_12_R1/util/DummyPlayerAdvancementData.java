package net.citizensnpcs.nms.v1_12_R1.util;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;

import net.citizensnpcs.api.CitizensAPI;
import net.minecraft.server.v1_12_R1.Advancement;
import net.minecraft.server.v1_12_R1.AdvancementDataPlayer;
import net.minecraft.server.v1_12_R1.AdvancementProgress;
import net.minecraft.server.v1_12_R1.EntityPlayer;

public class DummyPlayerAdvancementData extends AdvancementDataPlayer {
    private DummyPlayerAdvancementData() {
        super(((CraftServer) Bukkit.getServer()).getServer(), CitizensAPI.getDataFolder(), null);
    }

    @Override
    public void a() {
    }

    @Override
    public void a(Advancement adv) {
    }

    @Override
    public void a(EntityPlayer p) {
    }

    @Override
    public void b() {
    }

    @Override
    public void b(EntityPlayer p) {
    }

    @Override
    public void c() {
    }

    @Override
    public AdvancementProgress getProgress(Advancement adv) {
        return new AdvancementProgress();
    }

    @Override
    public boolean grantCriteria(Advancement adv, String str) {
        return false;
    }

    @Override
    public boolean revokeCritera(Advancement adv, String str) {
        return true;
    }

    public static final DummyPlayerAdvancementData INSTANCE = new DummyPlayerAdvancementData();
}
