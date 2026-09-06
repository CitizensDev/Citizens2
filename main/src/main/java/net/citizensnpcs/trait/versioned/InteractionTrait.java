package net.citizensnpcs.trait.versioned;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;

import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Flag;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.Messaging;

@TraitName("interactiontrait")
public class InteractionTrait extends Trait {
    @Persist
    private Float height;
    @Persist
    private Boolean responsive;
    @Persist
    private Float width;

    public InteractionTrait() {
        super("interactiontrait");
    }

    @Override
    public void onSpawn() {
        if (!(npc.getCosmeticEntity() instanceof Interaction))
            return;
        Interaction display = (Interaction) npc.getCosmeticEntity();
        display.setCustomName(null);
        if (SUPPORTS_RESPONSIVE && responsive != null) {
            display.setResponsive(responsive);
        }
        if (width != null) {
            display.setInteractionWidth(width);
        }
        if (height != null) {
            display.setInteractionHeight(height);
        }
    }

    public void setInteractionHeight(Float height) {
        this.height = height;
        onSpawn();
    }

    public void setInteractionWidth(Float width) {
        this.width = width;
        onSpawn();
    }

    public void setResponsive(Boolean responsive) {
        this.responsive = responsive;
        onSpawn();
    }

    @Command(
            aliases = { "npc" },
            usage = "interaction --height [height] --responsive [true|false] --width [width]",
            desc = "",
            modifiers = { "interaction" },
            min = 1,
            max = 1,
            permission = "citizens.npc.interaction")
    @Requirements(selected = true, ownership = true, cosmeticTypes = { EntityType.INTERACTION })
    public static void display(CommandContext args, CommandSender sender, NPC npc,
            @Flag("responsive") Boolean responsive, @Flag("width") Float width, @Flag("height") Float height)
            throws CommandException {
        InteractionTrait trait = npc.getOrAddTrait(InteractionTrait.class);
        String output = "";
        if (height != null) {
            trait.setInteractionHeight(height);
        }
        if (width != null) {
            trait.setInteractionWidth(width);
        }
        if (responsive != null) {
            trait.setResponsive(responsive);
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output.trim());
        }
    }

    private static boolean SUPPORTS_RESPONSIVE;

    static {
        try {
            Interaction.class.getMethod("setResponsive", boolean.class);
            SUPPORTS_RESPONSIVE = true;
        } catch (Throwable t) {
        }
    }
}
