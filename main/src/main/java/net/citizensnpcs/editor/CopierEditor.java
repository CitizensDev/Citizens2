package net.citizensnpcs.editor;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.trait.CurrentLocation;
import net.citizensnpcs.util.Messages;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class CopierEditor extends Editor {
    private final String name;
    private final NPC npc;
    private final Player player;

    public CopierEditor(Player player, NPC npc) {
        this.player = player;
        this.npc = npc;
        this.name = npc.getFullName();
    }

    @Override
    public void begin() {
        Messaging.sendTr(player, Messages.COPIER_EDITOR_BEGIN);
    }

    @Override
    public void end() {
        Messaging.sendTr(player, Messages.COPIER_EDITOR_END);
    }

    @EventHandler
    public void onBlockClick(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }
        NPC copy = npc.clone();
        if (!copy.getFullName().equals(name)) {
            copy.setName(name);
        }

        if (copy.isSpawned() && player.isOnline()) {
            Location location = player.getLocation();
            location.getChunk().load();
            copy.teleport(location, TeleportCause.PLUGIN);
            copy.getTrait(CurrentLocation.class).setLocation(location);
        }

        Messaging.sendTr(player, Messages.NPC_COPIED, npc.getName());
    }
}
