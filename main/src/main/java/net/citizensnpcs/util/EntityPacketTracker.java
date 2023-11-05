package net.citizensnpcs.util;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.entity.Player;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public interface EntityPacketTracker extends Runnable {
    public void link(Player player);

    public void unlink(Player player);

    public void unlinkAll(Consumer<Player> callback);

    public static class PacketAggregator {
        private final Set<PlayerConnection> connections = Sets.newHashSet();
        private List<Object> packets;

        public void add(UUID uuid, Consumer<Object> conn) {
            connections.add(new PlayerConnection(uuid, conn));
        }

        public void removeConnection(UUID uuid) {
            connections.remove(new PlayerConnection(uuid, null));
        }

        public void send(Object packet) {
            if (packets != null) {
                packets.add(packet);
                return;
            }
            for (PlayerConnection conn : connections) {
                conn.conn.accept(packet);
            }
        }

        public void startBundling() {
            packets = Lists.newArrayList();
        }

        public void stopBundlingAndSend() {
            Iterable<Object> packets = NMS.createBundlePacket(this.packets);
            this.packets = null;
            for (Object packet : packets) {
                for (PlayerConnection conn : connections) {
                    conn.conn.accept(packet);
                }
            }
        }

        private static class PlayerConnection {
            Consumer<Object> conn;
            UUID uuid;

            public PlayerConnection(UUID uuid, Consumer<Object> conn) {
                this.uuid = uuid;
                this.conn = conn;
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj)
                    return true;

                if (obj == null || getClass() != obj.getClass())
                    return false;

                PlayerConnection other = (PlayerConnection) obj;
                if (!Objects.equals(uuid, other.uuid)) {
                    return false;

                }
                return true;
            }

            @Override
            public int hashCode() {
                return 31 + (uuid == null ? 0 : uuid.hashCode());
            }
        }
    }
}