package net.citizensnpcs.trait;

import org.bukkit.entity.Wither;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.util.NMS;

/**
 * Persists {@link Wither} metadata.
 *
 * @see Wither
 */
@TraitName("withertrait")
public class WitherTrait extends Trait {
    @Persist("arrowshield")
    private Boolean arrowShield;
    @Persist("charged")
    private Boolean invulnerable;

    public WitherTrait() {
        super("withertrait");
    }

    public Boolean blocksArrows() {
        return arrowShield;
    }

    public boolean isInvulnerable() {
        return invulnerable == null ? npc.isProtected() : invulnerable;
    }

    @Override
    public void run() {
        if (!(npc.getEntity() instanceof Wither))
            return;
        Wither wither = (Wither) npc.getEntity();
        NMS.setWitherInvulnerable(wither, invulnerable == null ? npc.isProtected() : invulnerable);
        if (arrowShield != null) {
            npc.data().set("wither-arrow-shield", arrowShield);
        } else {
            npc.data().remove("wither-arrow-shield");
        }
    }

    public void setBlocksArrows(boolean arrowShield) {
        this.arrowShield = arrowShield;
    }

    public void setInvulnerable(boolean invulnerable) {
        this.invulnerable = invulnerable;
    }
}
