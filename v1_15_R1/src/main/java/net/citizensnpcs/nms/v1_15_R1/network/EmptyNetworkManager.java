package net.citizensnpcs.nms.v1_15_R1.network;

import java.io.IOException;

import io.netty.util.concurrent.GenericFutureListener;
import net.citizensnpcs.nms.v1_15_R1.util.NMSImpl;
import net.minecraft.server.v1_15_R1.EnumProtocolDirection;
import net.minecraft.server.v1_15_R1.NetworkManager;
import net.minecraft.server.v1_15_R1.Packet;

public class EmptyNetworkManager extends NetworkManager {
    public EmptyNetworkManager(EnumProtocolDirection flag) throws IOException {
        super(flag);
        NMSImpl.initNetworkManager(this);
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void sendPacket(Packet packet, GenericFutureListener genericfuturelistener) {
    }
}