package net.citizensnpcs.npc.entity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Colorizer;
import net.citizensnpcs.npc.AbstractEntityController;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_7_R3.PlayerInteractManager;
import net.minecraft.server.v1_7_R3.WorldServer;
import net.minecraft.util.com.google.common.collect.Iterables;
import net.minecraft.util.com.mojang.authlib.Agent;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import net.minecraft.util.com.mojang.authlib.GameProfileRepository;
import net.minecraft.util.com.mojang.authlib.ProfileLookupCallback;
import net.minecraft.util.com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.util.com.mojang.authlib.properties.Property;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_7_R3.CraftServer;
import org.bukkit.craftbukkit.v1_7_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.google.common.collect.Maps;

public class HumanController extends AbstractEntityController {
    public HumanController() {
        super();
    }

    @Override
    protected Entity createEntity(final Location at, final NPC npc) {
        final WorldServer nmsWorld = ((CraftWorld) at.getWorld()).getHandle();
        String coloredName = Colorizer.parseColors(npc.getFullName());
        if (coloredName.length() > 16) {
            coloredName = coloredName.substring(0, 16);
        }

        UUID uuid = UUID.randomUUID();
        if (uuid.version() == 4) { // clear version
            uuid = new UUID(uuid.getMostSignificantBits() | 0x0000000000005000L, uuid.getLeastSignificantBits());
        }

        GameProfile profile = new GameProfile(uuid, coloredName);
        updateSkin(npc, nmsWorld, profile);

        final EntityHumanNPC handle = new EntityHumanNPC(nmsWorld.getServer().getServer(), nmsWorld, profile,
                new PlayerInteractManager(nmsWorld), npc);
        handle.setPositionRotation(at.getX(), at.getY(), at.getZ(), at.getYaw(), at.getPitch());
        Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
            @Override
            public void run() {
                boolean removeFromPlayerList = Setting.REMOVE_PLAYERS_FROM_PLAYER_LIST.asBoolean();
                NMS.addOrRemoveFromPlayerList(getBukkitEntity(),
                        npc.data().get("removefromplayerlist", removeFromPlayerList));
            }
        }, 1);
        handle.getBukkitEntity().setSleepingIgnored(true);
        return handle.getBukkitEntity();
    }

    @Override
    public Player getBukkitEntity() {
        return (Player) super.getBukkitEntity();
    }

    private void updateSkin(final NPC npc, final WorldServer nmsWorld, GameProfile profile) {
        String skinUUID = npc.data().get(NPC.PLAYER_SKIN_UUID_METADATA);
        if (skinUUID == null) {
            skinUUID = npc.getName();
        }
        if (npc.data().has(CACHED_SKIN_UUID_METADATA) && npc.data().has(CACHED_SKIN_UUID_NAME_METADATA)
                && ChatColor.stripColor(skinUUID).equalsIgnoreCase(ChatColor.stripColor(npc.data().<String> get(CACHED_SKIN_UUID_NAME_METADATA)))) {
            skinUUID = npc.data().get(CACHED_SKIN_UUID_METADATA);
        }
        if (UUID_CACHE.containsKey(skinUUID)) {
            skinUUID = UUID_CACHE.get(skinUUID);
        }
        Property cached = TEXTURE_CACHE.get(skinUUID);
        if (cached != null) {
            profile.getProperties().put("textures", cached);
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(CitizensAPI.getPlugin(),
                    new SkinFetcher(new UUIDFetcher(skinUUID, npc), nmsWorld.getMinecraftServer().av(), npc));
        }
    }

    private static class SkinFetcher implements Runnable {
        private final NPC npc;
        private final MinecraftSessionService repo;
        private final Callable<String> uuid;

        public SkinFetcher(Callable<String> uuid, MinecraftSessionService repo, NPC npc) {
            this.uuid = uuid;
            this.repo = repo;
            this.npc = npc;
        }

        @Override
        public void run() {
            String realUUID;
            try {
                realUUID = uuid.call();
            } catch (Exception e) {
                return;
            }
            GameProfile skinProfile = null;
            try {
                skinProfile = repo.fillProfileProperties(new GameProfile(UUID.fromString(realUUID), ""), true);
            } catch (Exception e) {
                return;
            }
            if (skinProfile == null || !skinProfile.getProperties().containsKey("textures"))
                return;
            Property textures = Iterables.getFirst(skinProfile.getProperties().get("textures"), null);
            if (textures.getValue() != null && textures.getSignature() != null) {
                TEXTURE_CACHE.put(realUUID, new Property("textures", textures.getValue(), textures.getSignature()));
                Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
                    @Override
                    public void run() {
                        if (npc.isSpawned()) {
                            npc.despawn(DespawnReason.PENDING_RESPAWN);
                            npc.spawn(npc.getStoredLocation());
                        }
                    }
                });
            }
        }
    }

    public static class UUIDFetcher implements Callable<String> {
        private final NPC npc;
        private final String reportedUUID;

        public UUIDFetcher(String reportedUUID, NPC npc) {
            this.reportedUUID = reportedUUID;
            this.npc = npc;
        }

        @Override
        public String call() throws Exception {
            if (reportedUUID.contains("-")) {
                return reportedUUID;
            }
            final GameProfileRepository repo = ((CraftServer) Bukkit.getServer()).getServer()
                    .getGameProfileRepository();
            repo.findProfilesByNames(new String[] { ChatColor.stripColor(reportedUUID) }, Agent.MINECRAFT, new ProfileLookupCallback() {
                @Override
                public void onProfileLookupFailed(GameProfile arg0, Exception arg1) {
                    throw new RuntimeException(arg1);
                }

                @Override
                public void onProfileLookupSucceeded(final GameProfile profile) {
                    UUID_CACHE.put(reportedUUID, profile.getId().toString());
                    npc.data().setPersistent(CACHED_SKIN_UUID_METADATA, profile.getId().toString());
                    npc.data().setPersistent(CACHED_SKIN_UUID_NAME_METADATA, profile.getName());
                }
            });
            return npc.data().get(CACHED_SKIN_UUID_METADATA);
        }
    }

    private static final String CACHED_SKIN_UUID_METADATA = "cached-skin-uuid";
    private static final String CACHED_SKIN_UUID_NAME_METADATA = "cached-skin-uuid-name";
    private static final Map<String, Property> TEXTURE_CACHE = Maps.newConcurrentMap();
    private static final Map<String, String> UUID_CACHE = Maps.newConcurrentMap();
}
