package net.citizensnpcs.npc.skin;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.citizensnpcs.Settings;
import net.citizensnpcs.api.util.Messaging;
import org.bukkit.Bukkit;

import javax.annotation.Nullable;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

class SkinThread implements Runnable {

    private volatile int delay = 0;
    private volatile int retryTimes = 0;
    private final BlockingDeque<SkinEntry> skinEntries = new LinkedBlockingDeque<SkinEntry>();

    public void retrieveSkin(String skinName, NPCSkin fetcher, MinecraftSessionService repo,
                             final @Nullable SkinRetrieved onRetrieve) {

        if (!Bukkit.isPrimaryThread())
            throw new IllegalStateException("SkinThread.retrieveSkin must be invoked from the main thread.");

        SkinEntry entry = getSkinEntry(skinName, repo);
        if (!entry.isValidSkin) {
            if (onRetrieve != null)
                onRetrieve.onRetrieve(Skin.FetchResult.INVALID_SKIN);
            return;
        }

        if (entry.skin.hasSkinData()) {
            try {
                entry.skin.fetchAndRespawn(fetcher.getNPC(), SkinThread.this,
                        onRetrieve == null
                                ? null
                        : new Skin.SkinFetchCallback() {
                            @Override
                            public void onFetch(Skin.FetchResult result) {
                                onRetrieve.onRetrieve(result);
                            }
                        });
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        entry.fetchers.offer(new FetcherEntry(fetcher, onRetrieve));
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

    public interface SkinRetrieved {
        void onRetrieve(Skin.FetchResult result);
    }

    private class FetcherEntry {
        final NPCSkin fetcher;
        final SkinRetrieved onRetrieve;

        FetcherEntry(NPCSkin fetcher, SkinRetrieved onRetrieve) {
            this.fetcher = fetcher;
            this.onRetrieve = onRetrieve;
        }

        void retrieved(Skin.FetchResult result) {

            if (onRetrieve == null)
                return;

            onRetrieve.onRetrieve(result);
        }

        @Nullable
        Skin.SkinFetchCallback fetchCallback() {
            if (onRetrieve == null)
                return null;

            return new Skin.SkinFetchCallback() {
                @Override
                public void onFetch(Skin.FetchResult result) {
                    onRetrieve.onRetrieve(result);
                }
            };
        }
    }

    private class SkinEntry {
        final String skinName;
        final Skin skin;
        BlockingDeque<FetcherEntry> fetchers = new LinkedBlockingDeque<FetcherEntry>();
        boolean isValidSkin = true;

        SkinEntry(Skin skin) {
            this.skinName = skin.getSkinName();
            this.skin = skin;
        }

        void fetchSkin() {

            if (fetchers.isEmpty() || !isValidSkin)
                return;

            final FetcherEntry firstSkin = fetchers.poll();

            skin.fetchAndRespawn(firstSkin.fetcher.getNPC(), SkinThread.this, new Skin.SkinFetchCallback() {
                @Override
                public void onFetch(Skin.FetchResult result) {

                    firstSkin.retrieved(result);

                    if (result == Skin.FetchResult.INVALID_SKIN) {
                        isValidSkin = false;
                        return;
                    }

                    if (result != Skin.FetchResult.SUCCESS) {
                        fetchers.offer(firstSkin);
                        return;
                    }

                    while (!fetchers.isEmpty()) {
                        FetcherEntry entry = fetchers.poll();

                        skin.fetchAndRespawn(entry.fetcher.getNPC(), SkinThread.this, entry.fetchCallback());

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
