package net.citizensnpcs.trait.versioned;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Panda;

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
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;

@TraitName("pandatrait")
public class PandaTrait extends Trait {
    @Persist
    private Panda.Gene hiddenGene;
    @Persist
    private Panda.Gene mainGene = Panda.Gene.NORMAL;
    @Persist
    private boolean sitting;

    public PandaTrait() {
        super("pandatrait");
    }

    public Panda.Gene getHiddenGene() {
        return hiddenGene;
    }

    public Panda.Gene getMainGene() {
        return mainGene;
    }

    public boolean isSitting() {
        return sitting;
    }

    @Override
    public void run() {
        if (npc.isSpawned() && npc.getEntity() instanceof Panda) {
            Panda panda = (Panda) npc.getEntity();
            panda.setMainGene(mainGene);
            NMS.setPandaSitting(npc.getEntity(), sitting);
            if (hiddenGene != null) {
                panda.setHiddenGene(hiddenGene);
            }
        }
    }

    public void setHiddenGene(Panda.Gene gene) {
        this.hiddenGene = gene;
    }

    public void setMainGene(Panda.Gene gene) {
        this.mainGene = gene;
    }

    public void setSitting(boolean sitting) {
        this.sitting = sitting;
    }

    public boolean toggleSitting() {
        return sitting = !sitting;
    }

    @Command(
            aliases = { "npc" },
            usage = "panda --gene (main gene) --hiddengene (hidden gene) -s(itting)",
            desc = "Sets panda modifiers",
            modifiers = { "panda" },
            flags = "s",
            min = 1,
            max = 1,
            permission = "citizens.npc.panda")
    @Requirements(selected = true, ownership = true, types = EntityType.PANDA)
    public static void panda(CommandContext args, CommandSender sender, NPC npc, @Flag("gene") Panda.Gene gene,
            @Flag("hiddengene") Panda.Gene hiddengene) throws CommandException {
        PandaTrait trait = npc.getOrAddTrait(PandaTrait.class);
        String output = "";
        if (args.hasValueFlag("gene")) {
            if (gene == null) {
                throw new CommandUsageException(Messages.INVALID_PANDA_GENE,
                        Util.listValuesPretty(Panda.Gene.values()));
            }
            trait.setMainGene(gene);
            output += ' ' + Messaging.tr(Messages.PANDA_MAIN_GENE_SET, args.getFlag("gene"));
        }
        if (args.hasValueFlag("hiddengene")) {
            if (hiddengene == null) {
                throw new CommandUsageException(Messages.INVALID_PANDA_GENE,
                        Util.listValuesPretty(Panda.Gene.values()));
            }
            trait.setHiddenGene(hiddengene);
            output += ' ' + Messaging.tr(Messages.PANDA_HIDDEN_GENE_SET, hiddengene);
        }
        if (args.hasFlag('s')) {
            boolean isSitting = trait.toggleSitting();
            output += ' ' + Messaging.tr(isSitting ? Messages.PANDA_SITTING : Messages.PANDA_STOPPED_SITTING);
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output.trim());
        } else {
            throw new CommandUsageException();
        }
    }

}
