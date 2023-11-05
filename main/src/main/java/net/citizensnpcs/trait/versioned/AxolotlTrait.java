package net.citizensnpcs.trait.versioned;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.EntityType;

import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Flag;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.command.exception.CommandUsageException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Util;

@TraitName("axolotltrait")
public class AxolotlTrait extends Trait {
    @Persist
    private boolean playingDead = false;
    @Persist
    private Axolotl.Variant variant = null;

    public AxolotlTrait() {
        super("axolotltrait");
    }

    public Axolotl.Variant getVariant() {
        return variant;
    }

    public boolean isPlayingDead() {
        return playingDead;
    }

    @Override
    public void run() {
        if (npc.isSpawned() && npc.getEntity() instanceof Axolotl) {
            Axolotl axolotl = (Axolotl) npc.getEntity();
            if (variant != null) {
                axolotl.setVariant(variant);
            }
            axolotl.setPlayingDead(playingDead);
        }
    }

    public void setPlayingDead(boolean playingDead) {
        this.playingDead = playingDead;
    }

    public void setVariant(Axolotl.Variant variant) {
        this.variant = variant;
    }

    @Command(
            aliases = { "npc" },
            usage = "axolotl (-d) (--variant variant)",
            desc = "Sets axolotl modifiers",
            modifiers = { "axolotl" },
            min = 1,
            max = 1,
            flags = "d",
            permission = "citizens.npc.axolotl")
    @Requirements(selected = true, ownership = true, types = EntityType.AXOLOTL)
    public static void axolotl(CommandContext args, CommandSender sender, NPC npc,
            @Flag("variant") Axolotl.Variant variant) throws CommandException {
        AxolotlTrait trait = npc.getOrAddTrait(AxolotlTrait.class);
        String output = "";
        if (args.hasValueFlag("variant")) {
            if (variant == null)
                throw new CommandException(Messages.INVALID_AXOLOTL_VARIANT,
                        Util.listValuesPretty(Axolotl.Variant.values()));
            trait.setVariant(variant);
            output += ' ' + Messaging.tr(Messages.AXOLOTL_VARIANT_SET, args.getFlag("variant"));
        }
        if (args.hasFlag('d')) {
            trait.setPlayingDead(!trait.isPlayingDead());
            output += ' ' + (trait.isPlayingDead() ? Messaging.tr(Messages.AXOLOTL_PLAYING_DEAD, npc.getName())
                    : Messaging.tr(Messages.AXOLOTL_NOT_PLAYING_DEAD, npc.getName()));
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output.trim());
        } else
            throw new CommandUsageException();
    }
}
