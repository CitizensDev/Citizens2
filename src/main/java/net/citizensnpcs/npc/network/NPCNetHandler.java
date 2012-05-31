package net.citizensnpcs.npc.network;


public class NPCNetHandler extends NetServerHandler {

    public NPCNetHandler(MinecraftServer minecraftServer, NetworkManager networkManager, EntityPlayer entityPlayer) {
        super(minecraftServer, networkManager, entityPlayer);
    }

    @Override
    public void a() {
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
    public void sendMessage(String string) {
    }

    @Override
    public void sendPacket(Packet packet) {
    }
}