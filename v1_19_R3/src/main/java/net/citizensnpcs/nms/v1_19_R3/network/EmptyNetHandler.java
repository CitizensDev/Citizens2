package net.citizensnpcs.nms.v1_19_R3.network;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class EmptyNetHandler extends ServerGamePacketListenerImpl {
    public EmptyNetHandler(MinecraftServer minecraftServer, Connection networkManager, ServerPlayer entityPlayer) {
        super(minecraftServer, networkManager, entityPlayer);
    }

    @Override
    public void send(Packet<?> packet) {
    }
}