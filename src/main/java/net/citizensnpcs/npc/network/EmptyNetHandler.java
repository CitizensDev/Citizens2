package net.citizensnpcs.npc.network;

import net.minecraft.server.v1_5_R1.EntityPlayer;
import net.minecraft.server.v1_5_R1.MinecraftServer;
import net.minecraft.server.v1_5_R1.NetworkManager;
import net.minecraft.server.v1_5_R1.Packet;
import net.minecraft.server.v1_5_R1.Packet102WindowClick;
import net.minecraft.server.v1_5_R1.Packet106Transaction;
import net.minecraft.server.v1_5_R1.Packet10Flying;
import net.minecraft.server.v1_5_R1.Packet130UpdateSign;
import net.minecraft.server.v1_5_R1.Packet14BlockDig;
import net.minecraft.server.v1_5_R1.Packet15Place;
import net.minecraft.server.v1_5_R1.Packet16BlockItemSwitch;
import net.minecraft.server.v1_5_R1.Packet255KickDisconnect;
import net.minecraft.server.v1_5_R1.Packet28EntityVelocity;
import net.minecraft.server.v1_5_R1.Packet3Chat;
import net.minecraft.server.v1_5_R1.Packet51MapChunk;
import net.minecraft.server.v1_5_R1.PlayerConnection;

public class EmptyNetHandler extends PlayerConnection {
    public EmptyNetHandler(MinecraftServer minecraftServer, NetworkManager networkManager, EntityPlayer entityPlayer) {
        super(minecraftServer, networkManager, entityPlayer);
    }

    @Override
    public void a(Packet102WindowClick packet) {
    }

    @Override
    public void a(Packet106Transaction packet) {
    }

    @Override
    public void a(Packet10Flying packet) {
    }

    @Override
    public void a(Packet130UpdateSign packet) {
    }

    @Override
    public void a(Packet14BlockDig packet) {
    }

    @Override
    public void a(Packet15Place packet) {
    }

    @Override
    public void a(Packet16BlockItemSwitch packet) {
    }

    @Override
    public void a(Packet255KickDisconnect packet) {
    }

    @Override
    public void a(Packet28EntityVelocity packet) {
    }

    @Override
    public void a(Packet3Chat packet) {
    }

    @Override
    public void a(Packet51MapChunk packet) {
    }

    @Override
    public void a(String string, Object[] objects) {
    }

    @Override
    public void sendPacket(Packet packet) {
    }
}