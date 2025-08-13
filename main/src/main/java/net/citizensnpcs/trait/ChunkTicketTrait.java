package net.citizensnpcs.trait;

import org.bukkit.Chunk;
import org.bukkit.plugin.Plugin;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.SpigotUtil;
import net.citizensnpcs.util.ChunkCoord;

@TraitName("chunktickettrait")
public class ChunkTicketTrait extends Trait {
    private ChunkCoord active;
    private int ticks;
    private long timeout;

    public ChunkTicketTrait() {
        super("chunktickettrait");
    }

    @Override
    public void onDespawn() {
        if (!SUPPORT_CHUNK_TICKETS)
            return;
        if (active != null) {
            active.getChunk().removePluginChunkTicket(CitizensAPI.getPlugin());
            active = null;
            ticks = 0;
            timeout = System.currentTimeMillis() + 1000;
        }
    }

    @Override
    public void onSpawn() {
        if (!SUPPORT_CHUNK_TICKETS)
            return;
        if (npc.data().get(NPC.Metadata.KEEP_CHUNK_LOADED, Setting.KEEP_CHUNKS_LOADED.asBoolean())) {
            ticks = -1;
        }
        Chunk chunk = npc.getEntity().getLocation().getChunk();
        active = new ChunkCoord(chunk);
        active.getChunk().addPluginChunkTicket(CitizensAPI.getPlugin());
        // https://github.com/PaperMC/Paper/issues/9581
        // XXX: can be removed if support for <=1.21.8 is dropped
        if (ticks >= 0 && SpigotUtil.getVersion()[1] <= 21
                && (SpigotUtil.getVersion().length < 3 || SpigotUtil.getVersion()[2] <= 8)
                && timeout < System.currentTimeMillis()) {
            ticks = 2;
        }
    }

    @Override
    public void run() {
        if (!SUPPORT_CHUNK_TICKETS)
            return;
        if (ticks > 0) {
            ticks--;
            if (ticks == 0) {
                onDespawn();
            }
            if (active != null) {
                Chunk chunk = npc.getEntity().getLocation().getChunk();
                ChunkCoord next = new ChunkCoord(chunk);
                if (!next.equals(active)) {
                    active.getChunk().removePluginChunkTicket(CitizensAPI.getPlugin());
                    chunk.addPluginChunkTicket(CitizensAPI.getPlugin());
                    active = next;
                } else {
                    chunk.addPluginChunkTicket(CitizensAPI.getPlugin()); // no way to tell if chunk already has a ticket
                }
            }
        }
    }

    private static boolean SUPPORT_CHUNK_TICKETS = true;

    static {
        try {
            Chunk.class.getMethod("removePluginChunkTicket", Plugin.class);
        } catch (NoSuchMethodException | SecurityException e) {
            SUPPORT_CHUNK_TICKETS = false;
        }
    }
}
