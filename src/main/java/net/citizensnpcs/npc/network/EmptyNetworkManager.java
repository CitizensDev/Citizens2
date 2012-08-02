package net.citizensnpcs.npc.network;

import java.lang.reflect.Field;
import java.net.Socket;
import java.security.PrivateKey;

import net.minecraft.server.NetHandler;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.Packet;

public class EmptyNetworkManager extends NetworkManager {

    public EmptyNetworkManager(Socket socket, String string, NetHandler netHandler, PrivateKey key) {
        super(socket, string, netHandler, key);

        try {
            // the field above the 3 synchronized lists
            Field f = NetworkManager.class.getDeclaredField("l");
            f.setAccessible(true);
            f.set(this, false);
        } catch (Exception e) {
        }
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