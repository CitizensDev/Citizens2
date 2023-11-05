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
    private boolean eating;
    @Persist
    private Panda.Gene hiddenGene;
    @Persist
    private Panda.Gene mainGene = Panda.Gene.NORMAL;
    @Persist
    private boolean rolling;
    @Persist
    private boolean sitting;
    @Persist
    private boolean sneezing;

    public PandaTrait() {
        super("pandatrait");
    }

    public Panda.Gene getHiddenGene() {
        return hiddenGene;
    }

    public Panda.Gene getMainGene() {
        return mainGene;
    }

    public boolean isEating() {
        return eating;
    }

    public boolean isRolling() {
        return rolling;
    }

    public boolean isSitting() {
        return sitting;
    }

    public boolean isSneezing() {
        return sneezing;
    }

    @Override
    public void run() {
        if (npc.isSpawned() && npc.getEntity() instanceof Panda) {
            Panda panda = (Panda) npc.getEntity();
            panda.setMainGene(mainGene);
            NMS.setPandaSitting(npc.getEntity(), sitting);
            if (SUPPORT_ROLLING_SNEEZING) {
                try {
                    panda.setRolling(rolling);
                    panda.setSneezing(sneezing);
                    panda.setEating(eating);
                } catch (Throwable t) {
                    SUPPORT_ROLLING_SNEEZING = false;
                }
            }
            if (hiddenGene != null) {
                panda.setHiddenGene(hiddenGene);
            }
        }
    }

    public void setEating(boolean eating) {
        this.eating = eating;
    }

    public void setHiddenGene(Panda.Gene gene) {
        hiddenGene = gene;
    }

    public void setMainGene(Panda.Gene gene) {
        mainGene = gene;
    }

    public void setRolling(boolean rolling) {
        this.rolling = rolling;
    }

    public void setSitting(boolean sitting) {
        this.sitting = sitting;
    }

    public void setSneezing(boolean sneezing) {
        this.sneezing = sneezing;
    }

    public boolean toggleEating() {
        return eating = !eating;
    }

    public boolean toggleRolling() {
        return rolling = !rolling;
    }

    public boolean toggleSitting() {
        return sitting = !sitting;
    }

    public boolean toggleSneezing() {
        return sneezing = !sneezing;
    }

    @Command(
            aliases = { "npc" },
            usage = "panda --gene (main gene) --hiddengene (hidden gene) -e(ating) -s(itting) -n (sneezing) -r(olling)",
            desc = "Sets panda modifiers",
            modifiers = { "panda" },
            flags = "srne",
            min = 1,
            max = 1,
            permission = "citizens.npc.panda")
    @Requirements(selected = true, ownership = true, types = EntityType.PANDA)
    public static void panda(CommandContext args, CommandSender sender, NPC npc, @Flag("gene") Panda.Gene gene,
            @Flag("hiddengene") Panda.Gene hiddengene) throws CommandException {
        PandaTrait trait = npc.getOrAddTrait(PandaTrait.class);
        String output = "";
        if (args.hasValueFlag("gene")) {
            if (gene == null)
                throw new CommandUsageException(Messages.INVALID_PANDA_GENE,
                        Util.listValuesPretty(Panda.Gene.values()));
            trait.setMainGene(gene);
            output += ' ' + Messaging.tr(Messages.PANDA_MAIN_GENE_SET, args.getFlag("gene"));
        }
        if (args.hasValueFlag("hiddengene")) {
            if (hiddengene == null)
                throw new CommandUsageException(Messages.INVALID_PANDA_GENE,
                        Util.listValuesPretty(Panda.Gene.values()));
            trait.setHiddenGene(hiddengene);
            output += ' ' + Messaging.tr(Messages.PANDA_HIDDEN_GENE_SET, hiddengene);
        }
        if (args.hasFlag('e')) {
            boolean isEating = trait.toggleEating();
            output += ' '
                    + Messaging.tr(isEating ? Messages.PANDA_EATING : Messages.PANDA_STOPPED_EATING, npc.getName());
        }
        if (args.hasFlag('s')) {
            boolean isSitting = trait.toggleSitting();
            output += ' '
                    + Messaging.tr(isSitting ? Messages.PANDA_SITTING : Messages.PANDA_STOPPED_SITTING, npc.getName());
        }
        if (args.hasFlag('r')) {
            boolean isRolling = trait.toggleRolling();
            output += ' '
                    + Messaging.tr(isRolling ? Messages.PANDA_ROLLING : Messages.PANDA_STOPPED_ROLLING, npc.getName());
        }
        if (args.hasFlag('n')) {
            boolean isSneezing = trait.toggleSneezing();
            output += ' ' + Messaging.tr(isSneezing ? Messages.PANDA_SNEEZING : Messages.PANDA_STOPPED_SNEEZING,
                    npc.getName());
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output.trim());
        } else
            throw new CommandUsageException();
    }

    private static boolean SUPPORT_ROLLING_SNEEZING = true;

}
