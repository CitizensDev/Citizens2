package net.citizensnpcs;

import java.util.Set;

import org.bukkit.entity.Entity;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.EnumWrappers;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.trait.SmoothRotationTrait;
import net.citizensnpcs.trait.SmoothRotationTrait.LocalRotationSession;

public class ProtocolLibListener {
    private final Class<?> flagsClass;
    private final ProtocolManager manager;
    private final Citizens plugin;

    public ProtocolLibListener(Citizens plugin) {
        this.plugin = plugin;
        this.manager = ProtocolLibrary.getProtocolManager();
        flagsClass = MinecraftReflection.getMinecraftClass("EnumPlayerTeleportFlags",
                "PacketPlayOutPosition$EnumPlayerTeleportFlags",
                "network.protocol.game.PacketPlayOutPosition$EnumPlayerTeleportFlags");
        manager.addPacketListener(
                new PacketAdapter(plugin, ListenerPriority.MONITOR, Server.ENTITY_HEAD_ROTATION, Server.ENTITY_LOOK) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        PacketContainer packet = event.getPacket();
                        Entity entity = manager.getEntityFromID(event.getPlayer().getWorld(),
                                packet.getIntegers().getValues().get(0));
                        if (!(entity instanceof NPCHolder))
                            return;

                        NPC npc = ((NPCHolder) entity).getNPC();
                        SmoothRotationTrait trait = npc.getTraitNullable(SmoothRotationTrait.class);
                        if (trait == null)
                            return;

                        LocalRotationSession session = trait.getLocalSession(event.getPlayer());
                        if (session == null || !session.isActive())
                            return;

                        if (event.getPacketType() == Server.ENTITY_HEAD_ROTATION) {
                            packet.getBytes().write(0, degToByte(session.getHeadYaw()));
                        } else if (event.getPacketType() == Server.ENTITY_LOOK) {
                            packet.getBytes().write(0, degToByte(session.getBodyYaw()));
                            packet.getBytes().write(1, degToByte(session.getPitch()));
                        } else if (event.getPacketType() == Server.ENTITY_MOVE_LOOK
                                || event.getPacketType() == Server.REL_ENTITY_MOVE_LOOK) {
                            packet.getBytes().write(0, degToByte(session.getBodyYaw()));
                            packet.getBytes().write(1, degToByte(session.getPitch()));
                        } else if (event.getPacketType() == Server.POSITION) {
                            Set<PlayerTeleportFlag> rel = getFlagsModifier(packet).read(0);
                            rel.remove(PlayerTeleportFlag.ZYAW);
                            rel.remove(PlayerTeleportFlag.ZPITCH);
                            getFlagsModifier(packet).write(0, rel);
                            packet.getFloat().write(0, session.getBodyYaw());
                            packet.getFloat().write(1, session.getPitch());
                        }
                    }
                });
    }

    private static byte degToByte(float in) {
        return (byte) (in * 256.0F / 360.0F);
    }

    private StructureModifier<Set<PlayerTeleportFlag>> getFlagsModifier(PacketContainer handle) {
        return handle.getSets(EnumWrappers.getGenericConverter(flagsClass, PlayerTeleportFlag.class));
    }

    public enum PlayerTeleportFlag {
        X,
        Y,
        Z,
        ZPITCH,
        ZYAW,
    }
}
