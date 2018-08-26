package net.citizensnpcs.nms.v1_13_R2.network;

import java.io.IOException;

import net.citizensnpcs.nms.v1_13_R2.util.NMSImpl;
import net.minecraft.server.v1_13_R2.EnumProtocolDirection;
import net.minecraft.server.v1_13_R2.NetworkManager;

public class EmptyNetworkManager extends NetworkManager {
    public EmptyNetworkManager(EnumProtocolDirection flag) throws IOException {
        super(flag);
        NMSImpl.initNetworkManager(this);
    }

    @Override
    public boolean isConnected() {
        return true;
    }
}