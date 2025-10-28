package net.citizensnpcs;

import java.lang.invoke.MethodHandle;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityHeadLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMoveAndRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo.PlayerData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate.PlayerInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import net.citizensnpcs.api.event.NPCAddTraitEvent;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Equipment.EquipmentSlot;
import net.citizensnpcs.api.trait.trait.MobType;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.trait.HologramTrait.HologramRenderer;
import net.citizensnpcs.trait.MirrorTrait;
import net.citizensnpcs.trait.RotationTrait;
import net.citizensnpcs.trait.RotationTrait.PacketRotationSession;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;

public class PacketEventsListener implements Listener {
    private final Map<UUID, MirrorTrait> mirrorTraits = Maps.newConcurrentMap();
    private final Map<Integer, RotationTrait> rotationTraits = Maps.newConcurrentMap();

    public PacketEventsListener(Citizens plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        PacketEvents.getAPI().init();
        PacketEvents.getAPI().getEventManager().registerListener(new PacketListener() {
            private EquipmentSlot convert(com.github.retrooper.packetevents.protocol.player.EquipmentSlot slot) {
                if (slot.name().equals("BODY"))
                    return EquipmentSlot.BODY;
                if (slot.name().equals("SADDLE"))
                    return EquipmentSlot.SADDLE;
                switch (slot) {
                    case CHEST_PLATE:
                        return EquipmentSlot.CHESTPLATE;
                    case BOOTS:
                        return EquipmentSlot.BOOTS;
                    case HELMET:
                        return EquipmentSlot.HELMET;
                    case LEGGINGS:
                        return EquipmentSlot.LEGGINGS;
                    case MAIN_HAND:
                        return EquipmentSlot.HAND;
                    case OFF_HAND:
                        return EquipmentSlot.OFF_HAND;
                    default:
                        return null;
                }
            }

            @Override
            public void onPacketSend(PacketSendEvent event) {
                if (event.getPacketType() != PacketType.Play.Server.ENTITY_EQUIPMENT)
                    return;
                WrapperPlayServerEntityEquipment packet = new WrapperPlayServerEntityEquipment(event);
                Entity entity = SpigotConversionUtil.getEntityById(event.<Player> getPlayer().getWorld(),
                        packet.getEntityId());
                NPC npc = plugin.getNPCRegistry().getNPC(entity);
                if (npc == null)
                    return;
                MirrorTrait mirror = npc.getTraitNullable(MirrorTrait.class);
                if (mirror != null && mirror.getEquipmentFunction() != null && mirror.isMirroring(event.getPlayer())) {
                    List<com.github.retrooper.packetevents.protocol.player.Equipment> equipment = Lists.newArrayList();
                    equipment.add(new com.github.retrooper.packetevents.protocol.player.Equipment(
                            com.github.retrooper.packetevents.protocol.player.EquipmentSlot.CHEST_PLATE,
                            SpigotConversionUtil.fromBukkitItemStack(
                                    mirror.getEquipmentFunction().apply(event.getPlayer(), EquipmentSlot.CHESTPLATE))));
                    equipment.add(new com.github.retrooper.packetevents.protocol.player.Equipment(
                            com.github.retrooper.packetevents.protocol.player.EquipmentSlot.BOOTS,
                            SpigotConversionUtil.fromBukkitItemStack(
                                    mirror.getEquipmentFunction().apply(event.getPlayer(), EquipmentSlot.BOOTS))));
                    equipment.add(new com.github.retrooper.packetevents.protocol.player.Equipment(
                            com.github.retrooper.packetevents.protocol.player.EquipmentSlot.HELMET,
                            SpigotConversionUtil.fromBukkitItemStack(
                                    mirror.getEquipmentFunction().apply(event.getPlayer(), EquipmentSlot.HELMET))));
                    equipment.add(new com.github.retrooper.packetevents.protocol.player.Equipment(
                            com.github.retrooper.packetevents.protocol.player.EquipmentSlot.LEGGINGS,
                            SpigotConversionUtil.fromBukkitItemStack(
                                    mirror.getEquipmentFunction().apply(event.getPlayer(), EquipmentSlot.LEGGINGS))));
                    equipment.add(new com.github.retrooper.packetevents.protocol.player.Equipment(
                            com.github.retrooper.packetevents.protocol.player.EquipmentSlot.MAIN_HAND,
                            SpigotConversionUtil.fromBukkitItemStack(
                                    mirror.getEquipmentFunction().apply(event.getPlayer(), EquipmentSlot.HAND))));
                    equipment.add(new com.github.retrooper.packetevents.protocol.player.Equipment(
                            com.github.retrooper.packetevents.protocol.player.EquipmentSlot.MAIN_HAND,
                            SpigotConversionUtil.fromBukkitItemStack(
                                    mirror.getEquipmentFunction().apply(event.getPlayer(), EquipmentSlot.OFF_HAND))));
                    packet.setEquipment(equipment);
                    packet.write();
                    return;
                }
                Equipment trait = npc.getTraitNullable(Equipment.class);
                if (trait == null)
                    return;
                boolean modified = false;
                for (com.github.retrooper.packetevents.protocol.player.Equipment equipment : packet.getEquipment()) {
                    EquipmentSlot converted = convert(equipment.getSlot());
                    if (converted == null)
                        continue;
                    ItemStack cosmetic = trait.getCosmetic(converted);
                    if (cosmetic != null) {
                        equipment.setItem(SpigotConversionUtil.fromBukkitItemStack(cosmetic));
                        modified = true;
                    }
                }
                if (modified) {
                    packet.write();
                }
            }
        }, PacketListenerPriority.NORMAL);
        PacketEvents.getAPI().getEventManager().registerListener(new PacketListener() {
            private MethodHandle DESERIALIZE_METHOD;
            private Object MINIMESSAGE;

            private Object minimessage(String raw) {
                if (MINIMESSAGE == null) {
                    try {
                        MINIMESSAGE = NMS.getMethodHandle(
                                Class.forName("net{}kyori{}adventure.text.minimessage.MiniMessage".replace("{}", ".")),
                                "miniMessage", true).invoke();
                        DESERIALIZE_METHOD = NMS.getMethodHandle(MINIMESSAGE.getClass(), "deserialize", true,
                                String.class);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
                try {
                    return DESERIALIZE_METHOD.invoke(MINIMESSAGE,
                            Messaging.convertLegacyCodes(raw).replace("<csr>", ""));
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            public void onPacketSend(PacketSendEvent event) {
                if (event.getPacketType() != PacketType.Play.Server.ENTITY_METADATA)
                    return;
                WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(event);
                Entity entity = SpigotConversionUtil.getEntityById(event.<Player> getPlayer().getWorld(),
                        packet.getEntityId());
                NPC npc = plugin.getNPCRegistry().getNPC(entity);
                if (npc == null || !npc.data().has(NPC.Metadata.HOLOGRAM_RENDERER))
                    return;
                int version = event.getUser().getClientVersion().getProtocolVersion();

                HologramRenderer hr = npc.data().get(NPC.Metadata.HOLOGRAM_RENDERER);
                Object fakeName = null;
                String suppliedName = hr.getPerPlayerText(npc, event.getPlayer());
                fakeName = version <= 340 ? suppliedName : Optional.of(minimessage(suppliedName));
                boolean sneaking = hr.isSneaking(npc, event.getPlayer());
                boolean delta = false;

                for (EntityData data : packet.getEntityMetadata()) {
                    if (fakeName != null && data.getIndex() == 2) {
                        data.setValue(fakeName);
                        delta = true;
                    } else if (sneaking && data.getIndex() == 0) {
                        byte b = (byte) (((Number) data.getValue()).byteValue() | 0x02);
                        data.setValue(b);
                        delta = true;
                    } else if (((data.getIndex() == 22 && version <= 762) || (data.getIndex() == 23 && version > 762))
                            && fakeName != null && npc.getEntity().getType() == EntityType.TEXT_DISPLAY) {
                        data.setValue(((Optional<?>) fakeName).get());
                        delta = true;
                    }
                }
                if (delta) {
                    packet.write();
                }
            }
        }, PacketListenerPriority.HIGHEST);
        PacketEvents.getAPI().getEventManager().registerListener(new PacketListener() {
            @Override
            public void onPacketSend(PacketSendEvent event) {
                if (event.getPacketType() == PacketType.Play.Server.PLAYER_INFO) {
                    WrapperPlayServerPlayerInfo packet = new WrapperPlayServerPlayerInfo(event);
                    if (packet.getAction() != WrapperPlayServerPlayerInfo.Action.ADD_PLAYER)
                        return;
                    boolean changed = false;
                    UserProfile playerProfile = event.getUser().getProfile();
                    String playerName = Util.possiblyStripBedrockPrefix(event.<Player> getPlayer().getDisplayName(),
                            playerProfile.getUUID());
                    for (PlayerData data : packet.getPlayerDataList()) {
                        MirrorTrait trait = mirrorTraits.get(data.getUserProfile().getUUID());
                        if (trait == null || !trait.isMirroring(event.getPlayer()))
                            continue;

                        if (trait.mirrorName()) {
                            data.getUserProfile().setName(playerName);
                            data.getUserProfile().setUUID(playerProfile.getUUID());
                        }
                        data.getUserProfile().setTextureProperties(playerProfile.getTextureProperties());
                        changed = true;
                    }
                    if (changed) {
                        packet.write();
                    }
                } else if (event.getPacketType() == PacketType.Play.Server.PLAYER_INFO_UPDATE) {
                    WrapperPlayServerPlayerInfoUpdate packet = new WrapperPlayServerPlayerInfoUpdate(event);
                    if (!packet.getActions().contains(WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER))
                        return;
                    boolean changed = false;
                    UserProfile playerProfile = event.getUser().getProfile();
                    String playerName = Util.possiblyStripBedrockPrefix(event.<Player> getPlayer().getDisplayName(),
                            playerProfile.getUUID());
                    for (PlayerInfo data : packet.getEntries()) {
                        MirrorTrait trait = mirrorTraits.get(data.getGameProfile().getUUID());
                        if (trait == null || !trait.isMirroring(event.getPlayer()))
                            continue;

                        if (trait.mirrorName()) {
                            data.getGameProfile().setName(playerName);
                            data.getGameProfile().setUUID(playerProfile.getUUID());
                        }
                        data.setListed(trait.getNPC().shouldRemoveFromTabList());
                        data.getGameProfile().setTextureProperties(playerProfile.getTextureProperties());
                        changed = true;
                    }
                    if (changed) {
                        packet.write();
                    }
                }
            }
        }, PacketListenerPriority.HIGHEST);
        Set<PacketType.Play.Server> rotation = EnumSet.of(PacketType.Play.Server.ENTITY_HEAD_LOOK,
                PacketType.Play.Server.ENTITY_TELEPORT, PacketType.Play.Server.ENTITY_RELATIVE_MOVE_AND_ROTATION,
                PacketType.Play.Server.ENTITY_ROTATION);
        PacketEvents.getAPI().getEventManager().registerListener(new PacketListener() {
            private PacketRotationSession getSession(int eid, Player player) {
                RotationTrait trait = rotationTraits.get(eid);
                if (trait == null)
                    return null;

                PacketRotationSession session = trait.getPacketSession(player);

                if (session == null || !session.isActive())
                    return null;

                return session;
            }

            @Override
            public void onPacketSend(PacketSendEvent event) {
                if (!rotation.contains(event.getPacketType()))
                    return;

                PacketTypeCommon type = event.getPacketType();
                PacketRotationSession session = null;
                if (type == PacketType.Play.Server.ENTITY_HEAD_LOOK) {
                    WrapperPlayServerEntityHeadLook packet = new WrapperPlayServerEntityHeadLook(event);
                    session = getSession(packet.getEntityId(), event.getPlayer());
                    if (session == null)
                        return;
                    packet.setHeadYaw(session.getHeadYaw());
                    packet.write();
                } else if (type == PacketType.Play.Server.ENTITY_RELATIVE_MOVE_AND_ROTATION) {
                    WrapperPlayServerEntityRelativeMoveAndRotation packet = new WrapperPlayServerEntityRelativeMoveAndRotation(
                            event);
                    session = getSession(packet.getEntityId(), event.getPlayer());
                    if (session == null)
                        return;
                    packet.setYaw(session.getBodyYaw());
                    packet.setPitch(session.getPitch());
                    packet.write();
                } else if (type == PacketType.Play.Server.ENTITY_ROTATION) {
                    WrapperPlayServerEntityRotation packet = new WrapperPlayServerEntityRotation(event);
                    session = getSession(packet.getEntityId(), event.getPlayer());
                    if (session == null)
                        return;
                    packet.setYaw(session.getBodyYaw());
                    packet.setPitch(session.getPitch());
                    packet.write();
                } else if (type == PacketType.Play.Server.ENTITY_TELEPORT) {
                    WrapperPlayServerEntityTeleport packet = new WrapperPlayServerEntityTeleport(event);
                    session = getSession(packet.getEntityId(), event.getPlayer());
                    if (session == null)
                        return;
                    packet.setYaw(session.getBodyYaw());
                    packet.setPitch(session.getPitch());
                    packet.write();
                }
                session.onPacketOverwritten();
            }
        }, PacketListenerPriority.HIGHEST);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        rotationTraits.remove(event.getEntity().getEntityId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onNPCDespawn(NPCDespawnEvent event) {
        if (event.getNPC().getEntity() == null)
            return;
        rotationTraits.remove(event.getNPC().getEntity().getEntityId());
        mirrorTraits.remove(event.getNPC().getEntity().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onNPCSpawn(NPCSpawnEvent event) {
        onSpawn(event);
    }

    private void onSpawn(NPCEvent event) {
        if (event.getNPC().hasTrait(RotationTrait.class)) {
            rotationTraits.put(event.getNPC().getEntity().getEntityId(),
                    event.getNPC().getTraitNullable(RotationTrait.class));
        }
        if (event.getNPC().hasTrait(MirrorTrait.class)
                && event.getNPC().getOrAddTrait(MobType.class).getType() == EntityType.PLAYER) {
            mirrorTraits.put(event.getNPC().getEntity().getUniqueId(),
                    event.getNPC().getTraitNullable(MirrorTrait.class));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTraitAdd(NPCAddTraitEvent event) {
        if (event.getNPC().getEntity() == null)
            return;
        onSpawn(event);
    }
}
