package net.citizensnpcs.npc.network;

import net.minecraft.server.v1_4_5.EntityPlayer;
import net.minecraft.server.v1_4_5.MinecraftServer;
import net.minecraft.server.v1_4_5.NetServerHandler;
import net.minecraft.server.v1_4_5.NetworkManager;
import net.minecraft.server.v1_4_5.Packet;
import net.minecraft.server.v1_4_5.Packet102WindowClick;
import net.minecraft.server.v1_4_5.Packet106Transaction;
import net.minecraft.server.v1_4_5.Packet10Flying;
import net.minecraft.server.v1_4_5.Packet130UpdateSign;
import net.minecraft.server.v1_4_5.Packet14BlockDig;
import net.minecraft.server.v1_4_5.Packet15Place;
import net.minecraft.server.v1_4_5.Packet16BlockItemSwitch;
import net.minecraft.server.v1_4_5.Packet255KickDisconnect;
import net.minecraft.server.v1_4_5.Packet28EntityVelocity;
import net.minecraft.server.v1_4_5.Packet3Chat;
import net.minecraft.server.v1_4_5.Packet51MapChunk;

public class EmptyNetHandler extends NetServerHandler {
    public EmptyNetHandler(MinecraftServer minecraftServer, NetworkManager networkManager,
            EntityPlayer entityPlayer) {
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