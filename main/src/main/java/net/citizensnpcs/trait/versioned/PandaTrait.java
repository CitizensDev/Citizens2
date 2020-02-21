package net.citizensnpcs.trait.versioned;

import org.bukkit.entity.Panda;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.util.NMS;

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

    public boolean toggleSitting() {
        return sitting = !sitting;
    }

}
