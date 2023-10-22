package net.citizensnpcs.trait.versioned;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Warden;

import com.google.common.collect.Maps;

import net.citizensnpcs.api.command.Arg;
import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.command.exception.CommandUsageException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.Messaging;

@TraitName("wardentrait")
public class WardenTrait extends Trait {
    private final Map<UUID, Integer> anger = Maps.newHashMap();

    public WardenTrait() {
        super("wardentrait");
    }

    private void addAnger(Entity entity, int anger) {
        this.anger.put(entity.getUniqueId(), anger);
    }

    @Override
    public void run() {
        if (npc.isSpawned() && npc.getEntity() instanceof Warden) {
            Warden warden = (Warden) npc.getEntity();
            for (Map.Entry<UUID, Integer> entry : anger.entrySet()) {
                warden.setAnger(Bukkit.getEntity(entry.getKey()), entry.getValue());
            }
        }
    }

    @Command(
            aliases = { "npc" },
            usage = "warden anger [entity uuid/player name] [anger]",
            desc = "Sets warden modifiers",
            modifiers = { "warden" },
            min = 1,
            max = 4,
            permission = "citizens.npc.warden")
    @Requirements(selected = true, ownership = true, types = EntityType.WARDEN)
    public static void Warden(CommandContext args, CommandSender sender, NPC npc,
            @Arg(value = 1, completions = { "anger" }) String command, @Arg(2) String player, @Arg(3) Integer anger)
            throws CommandException {
        WardenTrait trait = npc.getOrAddTrait(WardenTrait.class);
        String output = "";
        if (command.equalsIgnoreCase("anger")) {
            if (anger == null)
                throw new CommandUsageException();
            Entity entity = null;
            try {
                UUID uuid = UUID.fromString(player);
                entity = Bukkit.getEntity(uuid);
            } catch (IllegalArgumentException iae) {
                entity = Bukkit.getOfflinePlayer(player).getPlayer();
            }
            if (entity != null) {
                trait.addAnger(entity, anger);
            }
        }

        if (!output.isEmpty()) {
            Messaging.send(sender, output.trim());
        } else {
            throw new CommandUsageException();
        }
    }
}
