package net.citizensnpcs.nms.v1_20_R2.network;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class EmptyPacketListener extends ServerGamePacketListenerImpl {
    public EmptyPacketListener(MinecraftServer minecraftServer, Connection networkManager, ServerPlayer entityPlayer,
            CommonListenerCookie clc) {
        super(minecraftServer, networkManager, entityPlayer, clc);
    }

    @Override
    public void send(Packet<?> packet) {
    }
}