package net.citizensnpcs.npc.entity;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingDeque;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Colorizer;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.npc.AbstractEntityController;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_8_R1.PlayerInteractManager;
import net.minecraft.server.v1_8_R1.WorldServer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R1.CraftServer;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.authlib.yggdrasil.response.MinecraftProfilePropertiesResponse;
import com.mojang.util.UUIDTypeAdapter;

public class HumanController extends AbstractEntityController {
    public HumanController() {
        super();
        if (SKIN_THREAD == null) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(CitizensAPI.getPlugin(), SKIN_THREAD = new SkinThread(),
                    10, 10);
        }
    }

    @Override
    protected Entity createEntity(final Location at, final NPC npc) {
        final WorldServer nmsWorld = ((CraftWorld) at.getWorld()).getHandle();
        String coloredName = Colorizer.parseColors(npc.getFullName());
        if (coloredName.length() > 16) {
            coloredName = coloredName.substring(0, 16);
        }

        UUID uuid = npc.getUniqueId();
        if (uuid.version() == 4) { // clear version
            long msb = uuid.getMostSignificantBits();
            msb &= ~0x0000000000004000L;
            msb |= 0x0000000000002000L;
            uuid = new UUID(msb, uuid.getLeastSignificantBits());
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

    @Override
    public void remove() {
        NMS.sendPlayerlistPacket(false, getBukkitEntity());
        super.remove();
    }

    private void updateSkin(final NPC npc, final WorldServer nmsWorld, GameProfile profile) {
    	
        String skinUUID = npc.data().get(NPC.PLAYER_SKIN_UUID_METADATA);
        if (skinUUID == null) {
            skinUUID = npc.getName();
        }
        if (npc.data().has(CACHED_SKIN_UUID_METADATA)
                && npc.data().has(CACHED_SKIN_UUID_NAME_METADATA)
                && ChatColor.stripColor(skinUUID).equalsIgnoreCase(
                        ChatColor.stripColor(npc.data().<String> get(CACHED_SKIN_UUID_NAME_METADATA)))) {
            skinUUID = npc.data().get(CACHED_SKIN_UUID_METADATA);
        }
        if (npc.data().has(PLAYER_SKIN_TEXTURE_PROPERTIES)&&npc.data().<String>get(PLAYER_SKIN_TEXTURE_PROPERTIES).equals("cache")) {
    		SKIN_THREAD.addRunnable(new SkinFetcher(new UUIDFetcher(skinUUID, npc), nmsWorld.getMinecraftServer().aB(),
                    npc));
    		return;
    	}
        Property cached = TEXTURE_CACHE.get(skinUUID);
        if (npc.data().has(PLAYER_SKIN_TEXTURE_PROPERTIES)&&npc.data().has(PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN)) {
        	cached = new Property("textures",npc.data().<String>get(PLAYER_SKIN_TEXTURE_PROPERTIES),npc.data().<String>get(PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN));
        }
        if (cached != null) {
            profile.getProperties().put("textures", cached);
        } else {
            SKIN_THREAD.addRunnable(new SkinFetcher(new UUIDFetcher(skinUUID, npc), nmsWorld.getMinecraftServer().aB(),
                    npc));
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

        /*
         * Yggdrasil's default implementation of this method silently fails instead of throwing an Exception like it should.
         */
        private GameProfile fillProfileProperties(YggdrasilAuthenticationService auth, GameProfile profile,
                boolean requireSecure) throws Exception {
            URL url = HttpAuthenticationService.constantURL(new StringBuilder()
                    .append("https://sessionserver.mojang.com/session/minecraft/profile/")
                    .append(UUIDTypeAdapter.fromUUID(profile.getId())).toString());
            url = HttpAuthenticationService.concatenateURL(url,
                    new StringBuilder().append("unsigned=").append(!requireSecure).toString());
            MinecraftProfilePropertiesResponse response = (MinecraftProfilePropertiesResponse) MAKE_REQUEST.invoke(
                    auth, url, null, MinecraftProfilePropertiesResponse.class);
            if (response == null) {
                return profile;
            }
            GameProfile result = new GameProfile(response.getId(), response.getName());
            result.getProperties().putAll(response.getProperties());
            profile.getProperties().putAll(response.getProperties());
            return result;
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
            Property cached = TEXTURE_CACHE.get(realUUID);
            if (cached != null && !(npc.data().has(PLAYER_SKIN_TEXTURE_PROPERTIES)&&npc.data().<String>get(PLAYER_SKIN_TEXTURE_PROPERTIES).equals("cache"))) {
                if (Messaging.isDebugging()) {
                    Messaging
                            .debug("Using cached skin texture for NPC " + npc.getName() + " UUID " + npc.getUniqueId());
                }
                skinProfile = new GameProfile(UUID.fromString(realUUID), "");
                skinProfile.getProperties().put("textures", cached);
            } else {
                try {
                    skinProfile = fillProfileProperties(
                            ((YggdrasilMinecraftSessionService) repo).getAuthenticationService(),
                            new GameProfile(UUID.fromString(realUUID), ""), true);
                } catch (Exception e) {
                    if ((e.getMessage() != null && e.getMessage().contains("too many requests"))
                            || (e.getCause() != null && e.getCause().getMessage() != null && e.getCause().getMessage()
                                    .contains("too many requests"))) {
                        SKIN_THREAD.delay();
                        SKIN_THREAD.addRunnable(this);
                    }
                    return;
                }

                if (skinProfile == null || !skinProfile.getProperties().containsKey("textures"))
                    return;
                Property textures = Iterables.getFirst(skinProfile.getProperties().get("textures"), null);
                if (textures.getValue() == null || textures.getSignature() == null)
                    return;
                if (npc.data().has(PLAYER_SKIN_TEXTURE_PROPERTIES)&&npc.data().<String>get(PLAYER_SKIN_TEXTURE_PROPERTIES).equals("cache")) {
                	npc.data().setPersistent(PLAYER_SKIN_TEXTURE_PROPERTIES, textures.getValue());
                	npc.data().setPersistent(PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN, textures.getSignature());
                }
                if (Messaging.isDebugging()) {
                    Messaging.debug("Fetched skin texture for UUID " + realUUID + " for NPC " + npc.getName()
                            + " UUID " + npc.getUniqueId());
                }
                TEXTURE_CACHE.put(realUUID, new Property("textures", textures.getValue(), textures.getSignature()));
            }
            if (CitizensAPI.getPlugin().isEnabled()) {
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

    public static class SkinThread implements Runnable {
        private volatile int delay = 0;
        private volatile int retryTimes = 0;
        private final BlockingDeque<Runnable> tasks = new LinkedBlockingDeque<Runnable>();

        public void addRunnable(Runnable r) {
            Iterator<Runnable> itr = tasks.iterator();
            while (itr.hasNext()) {
                if (((SkinFetcher) itr.next()).npc.getUniqueId().equals(((SkinFetcher) r).npc.getUniqueId())) {
                    itr.remove();
                }
            }
            tasks.offer(r);
        }

        public void delay() {
            delay = Setting.NPC_SKIN_RETRY_DELAY.asInt();
            // need to wait before Mojang accepts API calls again
            retryTimes++;
            if (Setting.MAX_NPC_SKIN_RETRIES.asInt() >= 0 && retryTimes > Setting.MAX_NPC_SKIN_RETRIES.asInt()) {
                tasks.clear();
                retryTimes = 0;
            }
        }

        @Override
        public void run() {
            if (delay > 0) {
                delay--;
                return;
            }
            Runnable r = tasks.pollFirst();
            if (r == null) {
                return;
            }
            r.run();
        }

    }

    public static class UUIDFetcher implements Callable<String> {
        private final NPC npc;
        private String reportedUUID;

        public UUIDFetcher(String reportedUUID, NPC npc) {
            this.reportedUUID = reportedUUID;
            this.npc = npc;
        }

        @Override
        public String call() throws Exception {
            String skinUUID = UUID_CACHE.get(reportedUUID);
            if (skinUUID != null) {
                npc.data().setPersistent(CACHED_SKIN_UUID_METADATA, skinUUID);
                npc.data().setPersistent(CACHED_SKIN_UUID_NAME_METADATA, reportedUUID);
                reportedUUID = skinUUID;
            }
            if (reportedUUID.contains("-")) {
                return reportedUUID;
            }
            final GameProfileRepository repo = ((CraftServer) Bukkit.getServer()).getServer()
                    .getGameProfileRepository();
            repo.findProfilesByNames(new String[] { ChatColor.stripColor(reportedUUID) }, Agent.MINECRAFT,
                    new ProfileLookupCallback() {
                        @Override
                        public void onProfileLookupFailed(GameProfile arg0, Exception arg1) {
                        }

                        @Override
                        public void onProfileLookupSucceeded(final GameProfile profile) {
                            UUID_CACHE.put(reportedUUID, profile.getId().toString());
                            if (Messaging.isDebugging()) {
                                Messaging.debug("Fetched UUID " + profile.getId() + " for NPC " + npc.getName()
                                        + " UUID " + npc.getUniqueId());
                            }
                            npc.data().setPersistent(CACHED_SKIN_UUID_METADATA, profile.getId().toString());
                            npc.data().setPersistent(CACHED_SKIN_UUID_NAME_METADATA, profile.getName());
                        }
                    });
            return npc.data().get(CACHED_SKIN_UUID_METADATA, reportedUUID);
        }
    }

    private static final String CACHED_SKIN_UUID_METADATA = "cached-skin-uuid";
    private static final String CACHED_SKIN_UUID_NAME_METADATA = "cached-skin-uuid-name";
    private static final String PLAYER_SKIN_TEXTURE_PROPERTIES = "player-skin-textures";
    private static final String PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN = "player-skin-signature";
    private static Method MAKE_REQUEST;
    private static SkinThread SKIN_THREAD;
    private static final Map<String, Property> TEXTURE_CACHE = Maps.newConcurrentMap();
    private static final Map<String, String> UUID_CACHE = Maps.newConcurrentMap();
    static {
        try {
            MAKE_REQUEST = YggdrasilAuthenticationService.class.getDeclaredMethod("makeRequest", URL.class,
                    Object.class, Class.class);
            MAKE_REQUEST.setAccessible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
