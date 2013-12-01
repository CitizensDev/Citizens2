package net.citizensnpcs.npc.network;

import java.io.IOException;

import net.minecraft.server.v1_7_R1.NetworkManager;
import net.minecraft.util.io.netty.channel.ChannelHandlerContext;

public class EmptyNetworkManager extends NetworkManager {
    public EmptyNetworkManager(boolean flag) throws IOException {
        super(flag);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        ctx.channel().close();
    }
}