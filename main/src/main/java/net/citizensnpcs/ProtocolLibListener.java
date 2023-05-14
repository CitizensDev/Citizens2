package net.citizensnpcs;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.trait.MirrorTrait;
import net.citizensnpcs.trait.RotationTrait;
import net.citizensnpcs.trait.RotationTrait.PacketRotationSession;
import net.citizensnpcs.util.NMS;

public class ProtocolLibListener {
    private final Class<?> flagsClass;
    private final ProtocolManager manager;
    private final Citizens plugin;

    public ProtocolLibListener(Citizens plugin) {
        this.plugin = plugin;
        this.manager = ProtocolLibrary.getProtocolManager();
        flagsClass = MinecraftReflection.getMinecraftClass("RelativeMovement", "world.entity.RelativeMovement",
                "EnumPlayerTeleportFlags", "PacketPlayOutPosition$EnumPlayerTeleportFlags",
                "network.protocol.game.PacketPlayOutPosition$EnumPlayerTeleportFlags");
        manager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.HIGHEST, Server.ENTITY_METADATA) {
            @Override
            public void onPacketSending(PacketEvent event) {
                NPC npc = getNPCFromPacket(event);
                if (npc == null || !npc.data().has(NPC.Metadata.HOLOGRAM_LINE_SUPPLIER))
                    return;
                Function<Player, String> hvs = npc.data().get(NPC.Metadata.HOLOGRAM_LINE_SUPPLIER);
                int version = manager.getProtocolVersion(event.getPlayer());
                PacketContainer packet = event.getPacket();
                if (version < 761) {
                    List<WrappedWatchableObject> wwo = packet.getWatchableCollectionModifier().readSafely(0);
                    if (wwo == null)
                        return;
                    boolean delta = false;
                    String text = hvs.apply(event.getPlayer());
                    for (WrappedWatchableObject wo : wwo) {
                        if (wo.getIndex() != 2)
                            continue;
                        if (version <= 340) {
                            wo.setValue(text);
                        } else {
                            wo.setValue(Optional.of(Messaging.minecraftComponentFromRawMessage(text)));
                        }
                        delta = true;
                        break;
                    }
                    if (delta) {
                        packet.getWatchableCollectionModifier().write(0, wwo);
                    }
                } else {
                    List<WrappedDataValue> wdvs = packet.getDataValueCollectionModifier().readSafely(0);
                    if (wdvs == null)
                        return;
                    boolean delta = false;
                    String text = hvs.apply(event.getPlayer());
                    for (WrappedDataValue wdv : wdvs) {
                        if (wdv.getIndex() != 2)
                            continue;
                        wdv.setValue(Optional.of(Messaging.minecraftComponentFromRawMessage(text)));
                        break;
                    }
                    if (delta) {
                        packet.getDataValueCollectionModifier().write(0, wdvs);
                    }
                }
            }
        });
        manager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.HIGHEST, Server.PLAYER_INFO) {
            @Override
            public void onPacketSending(PacketEvent event) {
                int version = manager.getProtocolVersion(event.getPlayer());
                if (version >= 761) {
                    NMS.onPlayerInfoAdd(event.getPlayer(), event.getPacket().getHandle());
                    return;
                }
                List<PlayerInfoData> list = event.getPacket().getPlayerInfoDataLists().readSafely(0);
                if (list == null)
                    return;
                boolean changed = false;
                for (int i = 0; i < list.size(); i++) {
                    PlayerInfoData npcInfo = list.get(i);
                    if (npcInfo == null)
                        continue;
                    NPC npc = CitizensAPI.getNPCRegistry().getByUniqueIdGlobal(npcInfo.getProfile().getUUID());
                    if (npc == null || !npc.isSpawned())
                        continue;
                    MirrorTrait trait = npc.getTraitNullable(MirrorTrait.class);
                    if (trait == null || !trait.isMirroring(event.getPlayer())) {
                        continue;
                    }
                    GameProfile playerProfile = NMS.getProfile(event.getPlayer());
                    if (trait.mirrorName()) {
                        list.set(i,
                                new PlayerInfoData(
                                        WrappedGameProfile.fromPlayer(event.getPlayer())
                                                .withId(npcInfo.getProfile().getId()),
                                        npcInfo.getLatency(), npcInfo.getGameMode(),
                                        WrappedChatComponent.fromText(event.getPlayer().getDisplayName())));
                        continue;
                    }
                    Collection<Property> textures = playerProfile.getProperties().get("textures");
                    if (textures == null || textures.size() == 0)
                        continue;
                    npcInfo.getProfile().getProperties().clear();
                    for (String key : playerProfile.getProperties().keySet()) {
                        npcInfo.getProfile().getProperties().putAll(key,
                                Iterables.transform(playerProfile.getProperties().get(key),
                                        skin -> new WrappedSignedProperty(skin.getName(), skin.getValue(),
                                                skin.getSignature())));
                    }
                    changed = true;
                }
                if (changed) {
                    event.getPacket().getPlayerInfoDataLists().write(0, list);
                }
            }
        });
        manager.addPacketListener(
                new PacketAdapter(plugin, ListenerPriority.MONITOR, Server.ENTITY_HEAD_ROTATION, Server.ENTITY_LOOK) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        NPC npc = getNPCFromPacket(event);
                        if (npc == null)
                            return;

                        RotationTrait trait = npc.getTraitNullable(RotationTrait.class);
                        if (trait == null)
                            return;

                        PacketRotationSession session = trait.getPacketSession(event.getPlayer());
                        if (session == null || !session.isActive())
                            return;

                        PacketContainer packet = event.getPacket();
                        PacketType type = event.getPacketType();
                        if (type == Server.ENTITY_HEAD_ROTATION) {
                            packet.getBytes().write(0, degToByte(session.getHeadYaw()));
                        } else if (type == Server.ENTITY_LOOK) {
                            packet.getBytes().write(0, degToByte(session.getBodyYaw()));
                            packet.getBytes().write(1, degToByte(session.getPitch()));
                        } else if (type == Server.ENTITY_MOVE_LOOK || type == Server.REL_ENTITY_MOVE_LOOK) {
                            packet.getBytes().write(0, degToByte(session.getBodyYaw()));
                            packet.getBytes().write(1, degToByte(session.getPitch()));
                        } else if (type == Server.POSITION) {
                            StructureModifier<Set<PlayerTeleportFlag>> flagsModifier = packet
                                    .getSets(EnumWrappers.getGenericConverter(flagsClass, PlayerTeleportFlag.class));
                            Set<PlayerTeleportFlag> rel = flagsModifier.read(0);
                            rel.remove(PlayerTeleportFlag.ZYAW);
                            rel.remove(PlayerTeleportFlag.ZPITCH);
                            flagsModifier.write(0, rel);
                            packet.getFloat().write(0, session.getBodyYaw());
                            packet.getFloat().write(1, session.getPitch());
                        }

                        session.onPacketOverwritten();
                    }
                });

    }

    private NPC getNPCFromPacket(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        Entity entity = null;
        try {
            Integer id = packet.getIntegers().readSafely(0);
            if (id == null)
                return null;
            entity = manager.getEntityFromID(event.getPlayer().getWorld(), id);
        } catch (FieldAccessException | IllegalArgumentException ex) {
            if (!LOGGED_ERROR) {
                Messaging.severe(
                        "Error retrieving entity from ID: ProtocolLib error? Suppressing further exceptions unless debugging.");
                ex.printStackTrace();
                LOGGED_ERROR = true;
            } else if (Messaging.isDebugging()) {
                ex.printStackTrace();
            }
            return null;
        }

        return entity instanceof NPCHolder ? ((NPCHolder) entity).getNPC() : null;
    }

    public enum PlayerTeleportFlag {
        X,
        Y,
        Z,
        ZPITCH,
        ZYAW,
    }

    private static byte degToByte(float in) {
        return (byte) (in * 256.0F / 360.0F);
    }

    private static boolean LOGGED_ERROR = false;
}
