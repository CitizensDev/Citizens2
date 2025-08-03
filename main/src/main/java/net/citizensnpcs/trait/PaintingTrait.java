package net.citizensnpcs.trait;

import org.bukkit.Art;
import org.bukkit.entity.Painting;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

/**
 * Persists {@link Painting} metadata.
 */
@TraitName("paintingtrait")
public class PaintingTrait extends Trait {
    @Persist("art")
    private Art art;

    public PaintingTrait() {
        super("paintingtrait");
    }

    @Override
    public void run() {
        if (!(npc.getEntity() instanceof Painting))
            return;
        Painting painting = (Painting) npc.getEntity();
        if (art != null) {
            painting.setArt(art);
        }
    }

    /**
     * @see Painting#setArt(Art)
     */
    public void setArt(Art art) {
        this.art = art;
    }
}
