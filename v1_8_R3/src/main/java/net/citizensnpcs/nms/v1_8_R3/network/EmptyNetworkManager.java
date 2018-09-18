package net.citizensnpcs.nms.v1_8_R3.network;

import java.io.IOException;

import net.citizensnpcs.nms.v1_8_R3.util.NMSImpl;
import net.minecraft.server.v1_8_R3.EnumProtocolDirection;
import net.minecraft.server.v1_8_R3.NetworkManager;
import net.minecraft.server.v1_8_R3.Packet;

public class EmptyNetworkManager extends NetworkManager {
    public EmptyNetworkManager(EnumProtocolDirection flag) throws IOException {
        super(flag);
        NMSImpl.initNetworkManager(this);
    }

    @Override
    public boolean g() {
        return true;
    }

    @Override
    public void sendPacket(Packet packet) {
    }
}