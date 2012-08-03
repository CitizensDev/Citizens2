package net.citizensnpcs.npc.network;

import java.net.Socket;
import java.security.PrivateKey;

import net.citizensnpcs.util.NMSReflection;
import net.minecraft.server.NetHandler;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.Packet;

public class EmptyNetworkManager extends NetworkManager {

    public EmptyNetworkManager(Socket socket, String string, NetHandler netHandler, PrivateKey key) {
        super(socket, string, netHandler, key);

        NMSReflection.stopNetworkThreads(this);
    }

    @Override
    public void a() {
    }

    @Override
    public void a(NetHandler netHandler) {
    }

    @Override
    public void a(String s, Object... objects) {
    }

    @Override
    public void b() {
    }

    @Override
    public void d() {
    }

    @Override
    public int e() {
        return 0;
    }

    @Override
    public void queue(Packet packet) {
    }
}