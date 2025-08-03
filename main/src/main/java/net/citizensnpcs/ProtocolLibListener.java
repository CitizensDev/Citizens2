package net.citizensnpcs;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.citizensnpcs.api.event.NPCAddTraitEvent;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Equipment.EquipmentSlot;
import net.citizensnpcs.api.trait.trait.MobType;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.trait.HologramTrait.HologramRenderer;
import net.citizensnpcs.trait.MirrorTrait;
import net.citizensnpcs.trait.RotationTrait;
import net.citizensnpcs.trait.RotationTrait.PacketRotationSession;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.SkinProperty;
import net.citizensnpcs.util.Util;

public class ProtocolLibListener implements Listener {
    private final ProtocolManager manager;
    private final Map<UUID, MirrorTrait> mirrorTraits = Maps.newConcurrentMap();
    private final Citizens plugin;
    private final Map<Integer, RotationTrait> rotationTraits = Maps.newConcurrentMap();

    public ProtocolLibListener(Citizens plugin) {
        this.plugin = plugin;
        manager = ProtocolLibrary.getProtocolManager();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        manager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, Server.ENTITY_EQUIPMENT) {
            private EquipmentSlot convert(ItemSlot slot) {
                if (slot.name().equals("BODY"))
                    return EquipmentSlot.BODY;
                switch (slot) {
                    case CHEST:
                        return EquipmentSlot.CHESTPLATE;
                    case FEET:
                        return EquipmentSlot.BOOTS;
                    case HEAD:
                        return EquipmentSlot.HELMET;
                    case LEGS:
                        return EquipmentSlot.LEGGINGS;
                    case MAINHAND:
                        return EquipmentSlot.HAND;
                    case OFFHAND:
                        return EquipmentSlot.OFF_HAND;
                    default:
                        return null;
                }
            }

            @Override
            public void onPacketSending(PacketEvent event) {
                NPC npc = getNPCFromPacket(event);
                if (npc == null)
                    return;
                MirrorTrait mirror = npc.getTraitNullable(MirrorTrait.class);
                if (mirror != null && mirror.getEquipmentFunction() != null && mirror.isMirroring(event.getPlayer())) {
                    StructureModifier<List<Pair<EnumWrappers.ItemSlot, ItemStack>>> modifier = event.getPacket()
                            .getLists(BukkitConverters.getPairConverter(EnumWrappers.getItemSlotConverter(),
                                    BukkitConverters.getItemStackConverter()));
                    List<Pair<EnumWrappers.ItemSlot, ItemStack>> equipment = Lists.newArrayList();
                    equipment.add(new Pair<>(ItemSlot.CHEST,
                            mirror.getEquipmentFunction().apply(event.getPlayer(), EquipmentSlot.CHESTPLATE)));
                    equipment.add(new Pair<>(ItemSlot.FEET,
                            mirror.getEquipmentFunction().apply(event.getPlayer(), EquipmentSlot.BOOTS)));
                    equipment.add(new Pair<>(ItemSlot.HEAD,
                            mirror.getEquipmentFunction().apply(event.getPlayer(), EquipmentSlot.HELMET)));
                    equipment.add(new Pair<>(ItemSlot.LEGS,
                            mirror.getEquipmentFunction().apply(event.getPlayer(), EquipmentSlot.LEGGINGS)));
                    equipment.add(new Pair<>(ItemSlot.MAINHAND,
                            mirror.getEquipmentFunction().apply(event.getPlayer(), EquipmentSlot.HAND)));
                    equipment.add(new Pair<>(ItemSlot.OFFHAND,
                            mirror.getEquipmentFunction().apply(event.getPlayer(), EquipmentSlot.OFF_HAND)));
                    modifier.write(0, equipment);
                    return;
                }
                Equipment trait = npc.getTraitNullable(Equipment.class);
                if (trait == null)
                    return;
                PacketContainer packet = event.getPacket();
                StructureModifier<List<Pair<EnumWrappers.ItemSlot, ItemStack>>> modifier = packet
                        .getLists(BukkitConverters.getPairConverter(EnumWrappers.getItemSlotConverter(),
                                BukkitConverters.getItemStackConverter()));
                for (int i = 0; i < modifier.getValues().size(); i++) {
                    List<Pair<EnumWrappers.ItemSlot, ItemStack>> pairs = modifier.read(i);
                    boolean modified = false;
                    for (Pair<EnumWrappers.ItemSlot, ItemStack> pair : pairs) {
                        EquipmentSlot converted = convert(pair.getFirst());
                        if (converted == null)
                            continue;
                        ItemStack cosmetic = trait.getCosmetic(converted);
                        if (cosmetic != null) {
                            pair.setSecond(cosmetic);
                            modified = true;
                        }
                    }
                    if (modified) {
                        modifier.write(i, pairs);
                    }
                }
            }
        });
        manager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.HIGHEST, Server.ENTITY_METADATA) {

            @Override
            public void onPacketSending(PacketEvent event) {
                NPC npc = getNPCFromPacket(event);
                if (npc == null)
                    return;
                PacketContainer packet = event.getPacket();
                int version = manager.getProtocolVersion(event.getPlayer());
                if (npc.data().has(NPC.Metadata.HOLOGRAM_RENDERER)) {
                    HologramRenderer hr = npc.data().get(NPC.Metadata.HOLOGRAM_RENDERER);
                    Object fakeName = null;
                    String suppliedName = hr.getPerPlayerText(npc, event.getPlayer());
                    fakeName = version <= 340 ? suppliedName
                            : Optional.of(Messaging.minecraftComponentFromRawMessage(suppliedName));
                    boolean sneaking = hr.isSneaking(npc, event.getPlayer());
                    boolean delta = false;

                    if (version < 761) {
                        List<WrappedWatchableObject> wwo = packet.getWatchableCollectionModifier().readSafely(0);
                        if (wwo == null)
                            return;

                        for (WrappedWatchableObject wo : wwo) {
                            if (fakeName != null && wo.getIndex() == 2) {
                                wo.setValue(fakeName);
                                delta = true;
                            } else if (sneaking && wo.getIndex() == 0) {
                                byte b = (byte) (((Number) wo.getValue()).byteValue() | 0x02);
                                wo.setValue(b);
                                delta = true;
                            }
                        }
                        if (delta) {
                            packet.getWatchableCollectionModifier().write(0, wwo);
                        }
                    } else {
                        List<WrappedDataValue> wdvs = packet.getDataValueCollectionModifier().readSafely(0);
                        if (wdvs == null)
                            return;

                        for (WrappedDataValue wdv : wdvs) {
                            switch (wdv.getIndex()) {
                                case 0:
                                    if (sneaking) {
                                        byte flags = (byte) (((Number) wdv.getValue()).byteValue() | 0x02);
                                        wdv.setValue(flags);
                                        delta = true;
                                    }
                                    break;
                                case 2:
                                    if (fakeName != null) {
                                        wdv.setRawValue(fakeName);
                                        delta = true;
                                    }
                                    break;
                                case 22:
                                    if (version <= 762 && fakeName != null
                                            && npc.getEntity().getType() == EntityType.TEXT_DISPLAY) {
                                        wdv.setRawValue(((Optional<?>) fakeName).get());
                                        delta = true;
                                    }
                                    break;
                                case 23:
                                    if (version > 762 && fakeName != null
                                            && npc.getEntity().getType() == EntityType.TEXT_DISPLAY) {
                                        wdv.setRawValue(((Optional<?>) fakeName).get());
                                        delta = true;
                                    }
                                    break;
                            }
                        }
                        if (delta) {
                            packet.getDataValueCollectionModifier().write(0, wdvs);
                        }
                    }
                }
            }
        });
        manager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.HIGHEST, Arrays.asList(Server.PLAYER_INFO),
                ListenerOptions.ASYNC) {
            @Override
            public void onPacketSending(PacketEvent event) {
                int version = manager.getProtocolVersion(event.getPlayer());
                if (version >= 761) {
                    NMS.onPlayerInfoAdd(event.getPlayer(), event.getPacket().getHandle(),
                            uuid -> mirrorTraits.get(uuid));
                    return;
                }
                List<PlayerInfoData> list = event.getPacket().getPlayerInfoDataLists().readSafely(0);
                if (list == null)
                    return;

                boolean changed = false;
                GameProfile playerProfile = null;
                WrappedGameProfile wgp = null;
                WrappedChatComponent playerName = null;
                for (int i = 0; i < list.size(); i++) {
                    PlayerInfoData npcInfo = list.get(i);
                    if (npcInfo == null)
                        continue;

                    MirrorTrait trait = mirrorTraits.get(npcInfo.getProfile().getUUID());
                    if (trait == null || !trait.isMirroring(event.getPlayer()))
                        continue;

                    if (playerProfile == null) {
                        playerProfile = NMS.getProfile(event.getPlayer());
                        wgp = WrappedGameProfile.fromPlayer(event.getPlayer());
                        playerName = WrappedChatComponent.fromText(
                                Util.possiblyStripBedrockPrefix(event.getPlayer().getDisplayName(), wgp.getUUID()));
                    }
                    if (trait.mirrorName()) {
                        list.set(i, new PlayerInfoData(wgp.withId(npcInfo.getProfile().getId()), npcInfo.getLatency(),
                                npcInfo.getGameMode(), playerName));
                        continue;
                    }
                    Collection<Property> textures = playerProfile.getProperties().get("textures");
                    if (textures == null || textures.size() == 0)
                        continue;

                    npcInfo.getProfile().getProperties().clear();
                    for (String key : playerProfile.getProperties().keySet()) {
                        npcInfo.getProfile().getProperties().putAll(key,
                                Iterables.transform(playerProfile.getProperties().get(key), skin -> {
                                    SkinProperty sp = SkinProperty.fromMojang(skin);
                                    return new WrappedSignedProperty(sp.name, sp.value, sp.signature);
                                }));
                    }
                    changed = true;
                }
                if (changed) {
                    event.getPacket().getPlayerInfoDataLists().write(0, list);
                }
            }
        });
        manager.addPacketListener(new PacketAdapter(
                plugin, ListenerPriority.HIGHEST, Arrays.asList(Server.ENTITY_HEAD_ROTATION, Server.ENTITY_LOOK,
                        Server.REL_ENTITY_MOVE_LOOK, Server.ENTITY_MOVE_LOOK, Server.ENTITY_TELEPORT),
                ListenerOptions.ASYNC) {
            @Override
            public void onPacketSending(PacketEvent event) {
                Integer eid = null;
                try {
                    eid = event.getPacket().getIntegers().readSafely(0);
                    if (eid == null)
                        return;
                } catch (FieldAccessException | IllegalArgumentException ex) {
                    if (!LOGGED_ERROR) {
                        Messaging.severe(
                                "Error retrieving entity from ID: ProtocolLib error? Suppressing further exceptions unless debugging.");
                        ex.printStackTrace();
                        LOGGED_ERROR = true;
                    } else if (Messaging.isDebugging()) {
                        ex.printStackTrace();
                    }
                    return;
                }
                RotationTrait trait = rotationTraits.get(eid);
                if (trait == null)
                    return;

                PacketRotationSession session = trait.getPacketSession(event.getPlayer());

                if (session == null || !session.isActive())
                    return;

                PacketContainer packet = event.getPacket();
                PacketType type = event.getPacketType();
                Messaging.debug("Modifying body/head yaw for", eid, "->", event.getPlayer().getName(),
                        session.getBodyYaw(), degToByte(session.getBodyYaw()), session.getHeadYaw(),
                        degToByte(session.getHeadYaw()), session.getPitch(), type);
                if (type == Server.ENTITY_HEAD_ROTATION) {
                    packet.getBytes().write(0, degToByte(session.getHeadYaw()));
                } else if (type == Server.ENTITY_LOOK || type == Server.ENTITY_MOVE_LOOK
                        || type == Server.REL_ENTITY_MOVE_LOOK || type == Server.ENTITY_TELEPORT) {
                    packet.getBytes().write(0, degToByte(session.getBodyYaw()));
                    packet.getBytes().write(1, degToByte(session.getPitch()));
                }
                session.onPacketOverwritten();
            }
        });
    }

    private NPC getNPCFromPacket(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        try {
            Object entityModifier = packet.getEntityModifier(event).read(0);
            return entityModifier instanceof NPCHolder ? ((NPCHolder) entityModifier).getNPC() : null;
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
            mirrorTraits.put(event.getNPC().getEntity().getUniqueId(), event.getNPC().getTraitNullable(MirrorTrait.class));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTraitAdd(NPCAddTraitEvent event) {
        if (!event.getNPC().isSpawned())
            return;
        onSpawn(event);
    }

    private static byte degToByte(float in) {
        return (byte) (in * 256.0F / 360.0F);
    }

    private static boolean LOGGED_ERROR = false;
}
