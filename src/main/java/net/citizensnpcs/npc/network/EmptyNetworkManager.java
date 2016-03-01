package net.citizensnpcs.npc.network;

import java.io.IOException;

import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_9_R1.EnumProtocolDirection;
import net.minecraft.server.v1_9_R1.NetworkManager;

public class EmptyNetworkManager extends NetworkManager {
    public EmptyNetworkManager(EnumProtocolDirection flag) throws IOException {
        super(flag);
        NMS.initNetworkManager(this);
    }
}