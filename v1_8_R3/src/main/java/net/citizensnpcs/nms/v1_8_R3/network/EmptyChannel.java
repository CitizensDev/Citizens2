package net.citizensnpcs.nms.v1_8_R3.network;

import io.netty.channel.AbstractChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.DefaultChannelConfig;
import io.netty.channel.EventLoop;
import io.netty.util.Version;

import java.net.SocketAddress;

public class EmptyChannel extends AbstractChannel {

    private static boolean updatedNetty = false;

    static {
        Version nettyVersion = Version.identify().get("netty-common");
        if (nettyVersion != null) {
            String[] split = nettyVersion.artifactVersion().split("\\.");
            try {
                int major = Integer.parseInt(split[0]);
                int minor = Integer.parseInt(split[1]);
                int revision = Integer.parseInt(split[2]);

                if (major > 4 || minor > 1 || revision > 24) {
                    updatedNetty = true;
                }
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
            }
        }
    }

    private final ChannelConfig config = new DefaultChannelConfig(this);

    public EmptyChannel(Channel parent) {
        super(parent);
    }

    @Override
    public ChannelConfig config() {
        config.setAutoRead(true);
        return config;
    }

    @Override
    protected void doBeginRead() throws Exception {
    }

    @Override
    protected void doBind(SocketAddress arg0) throws Exception {
    }

    @Override
    protected void doClose() throws Exception {
    }

    @Override
    protected void doDisconnect() throws Exception {
    }

    @Override
    protected void doWrite(ChannelOutboundBuffer arg0) throws Exception {
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    protected boolean isCompatible(EventLoop arg0) {
        return true;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    protected SocketAddress localAddress0() {
        return null;
    }

    @Override
    public ChannelMetadata metadata() {
        return updatedNetty ? new ChannelMetadata(true) : null;
    }

    @Override
    protected AbstractUnsafe newUnsafe() {
        return null;
    }

    @Override
    protected SocketAddress remoteAddress0() {
        return null;
    }
}
