package net.citizensnpcs.npc.skin;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.citizensnpcs.Settings;
import net.citizensnpcs.api.util.Messaging;
import org.bukkit.Bukkit;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

class SkinThread implements Runnable {

    private volatile int delay = 0;
    private volatile int retryTimes = 0;
    private final BlockingDeque<SkinEntry> skinEntries = new LinkedBlockingDeque<SkinEntry>();

    public void retrieveSkin(String skinName, NPCSkin fetcher, MinecraftSessionService repo) {

        if (!Bukkit.isPrimaryThread())
            throw new IllegalStateException("SkinThread.retrieveSkin must be invoked from the main thread.");

        SkinEntry entry = getSkinEntry(skinName, repo);
        if (!entry.isValidSkin)
            return;

        if (entry.skin.hasSkinData()) {
            try {
                entry.skin.fetchAndRespawn(fetcher.getNPC(), SkinThread.this, null);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        entry.fetchers.offer(fetcher);
    }

    public void delay() {
        delay = Settings.Setting.NPC_SKIN_RETRY_DELAY.asInt();
    }

    @Override
    public void run() {

        if (delay > 0) {
            delay--;
            return;
        }

        while (!skinEntries.isEmpty() && delay == 0) {

            SkinEntry entry = skinEntries.pollFirst();

            int maxRetries = Settings.Setting.MAX_NPC_SKIN_RETRIES.asInt();
            if (maxRetries >= 0 && retryTimes > maxRetries) {
                skinEntries.clear();
                retryTimes = 0;
                return;
            }

            try {
                entry.fetchSkin();
            }
            catch (Exception e) {

                if (Messaging.isDebugging()) {
                    Throwable cause = e.getCause();

                    if (cause != null)
                        Messaging.debug(cause.getClass().getName() + ": " + cause.getMessage());
                    else
                        Messaging.debug(e.getClass().getName() + ": " + e.getMessage());
                }
            }
        }
    }

    private SkinEntry getSkinEntry(String skinName, MinecraftSessionService repo) {

        skinName = skinName.toLowerCase();

        for (SkinEntry entry : skinEntries) {
            if (entry.skinName.equals(skinName))
                return entry;
        }

        Skin skin = Skin.getFromCache(skinName);
        if (skin == null) {
            skin = new Skin(skinName, repo);
        }

        SkinEntry entry = new SkinEntry(skin);
        skinEntries.offer(entry);
        return entry;
    }

    private class SkinEntry {
        final String skinName;
        final Skin skin;
        BlockingDeque<NPCSkin> fetchers = new LinkedBlockingDeque<NPCSkin>();
        boolean isValidSkin = true;

        SkinEntry(Skin skin) {
            this.skinName = skin.getSkinName();
            this.skin = skin;
        }

        void fetchSkin() {

            if (fetchers.isEmpty() || !isValidSkin)
                return;

            final NPCSkin firstSkin = fetchers.poll();

            skin.fetchAndRespawn(firstSkin.getNPC(), SkinThread.this, new Skin.SkinFetchCallback() {
                @Override
                public void onFetch(Skin.FetchResult result) {

                    if (result == Skin.FetchResult.INVALID_SKIN) {
                        isValidSkin = false;
                        return;
                    }

                    if (result != Skin.FetchResult.SUCCESS) {
                        fetchers.offer(firstSkin);
                        return;
                    }

                    while (!fetchers.isEmpty()) {
                        NPCSkin humanSkin = fetchers.poll();

                        skin.fetchAndRespawn(humanSkin.getNPC(), SkinThread.this, null);

                        try {
                            Thread.sleep(50 * 10);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            });
        }
    }
}
