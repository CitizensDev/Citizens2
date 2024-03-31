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
    @Persist("invulnerableticks")
    private Integer invulnerableTicks;

    public WitherTrait() {
        super("withertrait");
    }

    public Boolean blocksArrows() {
        return arrowShield;
    }

    public Integer getInvulnerableTicks() {
        return invulnerableTicks;
    }

    public boolean isInvulnerable() {
        if (invulnerable != null)
            return invulnerable;
        if (invulnerableTicks != null)
            return invulnerableTicks > 0;
        return npc.isProtected();
    }

    @Override
    public void run() {
        if (!(npc.getEntity() instanceof Wither))
            return;
        Wither wither = (Wither) npc.getEntity();
        if (invulnerable != null) {
            NMS.setWitherInvulnerableTicks(wither, invulnerable ? 20 : 0);
        } else if (invulnerableTicks != null) {
            NMS.setWitherInvulnerableTicks(wither, invulnerableTicks);
        } else {
            NMS.setWitherInvulnerableTicks(wither, npc.isProtected() ? 20 : 0);
        }
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

    public void setInvulnerableTicks(int ticks) {
        this.invulnerableTicks = ticks;
    }
}
