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
    public void onRemove() {
        onDespawn();
    }

    @Override
    public void onSpawn() {
        if (!SUPPORT_CHUNK_TICKETS)
            return;
        if (npc.data().get(NPC.Metadata.KEEP_CHUNK_LOADED, Setting.KEEP_CHUNKS_LOADED.asBoolean())) {
            ticks = -1;
        }
        // https://github.com/PaperMC/Paper/issues/9581
        // XXX: can be removed if support for <=1.21.8 is dropped
        if (ticks >= 0 && SpigotUtil.getVersion()[1] <= 21
                && (SpigotUtil.getVersion().length < 3 || SpigotUtil.getVersion()[2] <= 8)
                && timeout < System.currentTimeMillis()) {
            ticks = 2;
        }
        if (ticks != 0) {
            Chunk chunk = npc.getEntity().getLocation().getChunk();
            chunk.addPluginChunkTicket(CitizensAPI.getPlugin());
            active = new ChunkCoord(chunk);
        }
    }

    @Override
    public void run() {
        if (!SUPPORT_CHUNK_TICKETS || ticks == 0)
            return;
        if (--ticks == 0) {
            onDespawn();
            return;
        }
        if (active != null) {
            Plugin plugin = CitizensAPI.getPlugin();
            Chunk chunk = npc.getEntity().getLocation().getChunk();
            ChunkCoord next = new ChunkCoord(chunk);
            if (!next.equals(active)) {
                active.getChunk().removePluginChunkTicket(plugin);
                chunk.addPluginChunkTicket(plugin);
                active = next;
            } else if (!chunk.getPluginChunkTickets().contains(plugin)) {
                chunk.addPluginChunkTicket(plugin);
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
