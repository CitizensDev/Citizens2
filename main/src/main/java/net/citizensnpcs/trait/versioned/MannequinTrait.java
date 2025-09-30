package net.citizensnpcs.trait.versioned;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mannequin;
import org.bukkit.inventory.MainHand;

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

@TraitName("mannequintrait")
public class MannequinTrait extends Trait {
    @Persist
    String description;
    @Persist
    boolean hideDescription = true;
    @Persist
    boolean immovable;
    @Persist
    private MainHand mainHand;

    public MannequinTrait() {
        super("mannequintrait");
    }

    @Override
    public void run() {
        if (npc.getEntity() instanceof Mannequin) {
            Mannequin mannequin = (Mannequin) npc.getEntity();
            if (mainHand != null) {
                mannequin.setMainHand(mainHand);
            }
            mannequin.setDescription(description);
            mannequin.setImmovable(immovable);
            mannequin.setHideDescription(hideDescription);
        }
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setHideDescription(boolean hide) {
        this.hideDescription = hide;
    }

    public void setImmovable(boolean immovable) {
        this.immovable = immovable;
    }

    public void setMainHand(MainHand hand) {
        this.mainHand = hand;
    }

    @Command(
            aliases = { "npc" },
            usage = "mannequin --hide_description [true|false] --immovable [true|false] --description [description] --main_hand [LEFT|RIGHT]",
            desc = "",
            modifiers = { "mannequin" },
            min = 1,
            max = 1,
            permission = "citizens.npc.mannequin")
    @Requirements(selected = true, ownership = true, types = EntityType.MANNEQUIN)
    public static void cow(CommandContext args, CommandSender sender, NPC npc, @Flag("description") String description,
            @Flag("immovable") Boolean immovable, @Flag("hide_description") Boolean hideDescription,
            @Flag("main_hand") MainHand mainHand) throws CommandException {
        MannequinTrait trait = npc.getOrAddTrait(MannequinTrait.class);
        String output = "";
        if (description != null) {
            trait.setDescription(description);
        }
        if (hideDescription != null) {
            trait.setHideDescription(hideDescription);
        }
        if (immovable != null) {
            trait.setImmovable(immovable);
        }
        if (mainHand != null) {
            trait.setMainHand(mainHand);
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output);
        }
    }
}