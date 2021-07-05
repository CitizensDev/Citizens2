package net.citizensnpcs.trait.versioned;

import org.bukkit.entity.Axolotl;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

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
}
