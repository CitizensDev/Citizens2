package net.citizensnpcs.trait;

import org.bukkit.entity.Pig;
import org.bukkit.entity.Steerable;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.util.Util;

/**
 * Persists saddle metadata.
 *
 * @see Pig#hasSaddle()
 */
@TraitName("saddle")
public class Saddle extends Trait {
    @Persist("")
    private boolean saddle;
    private boolean steerable;

    public Saddle() {
        super("saddle");
    }

    @Override
    public void onSpawn() {
        if (Util.optionalEntitySet("PIG", "STRIDER").contains(npc.getEntity().getType())) {
            steerable = true;
            updateSaddleState();
        } else {
            steerable = false;
        }
    }

    public boolean toggle() {
        saddle = !saddle;
        if (steerable) {
            updateSaddleState();
        }
        return saddle;
    }

    @Override
    public String toString() {
        return "Saddle{" + saddle + "}";
    }

    private void updateSaddleState() {
        if (SUPPORT_STEERABLE) {
            try {
                ((Steerable) npc.getEntity()).setSaddle(saddle);
            } catch (Throwable t) {
                SUPPORT_STEERABLE = false;
                ((Pig) npc.getEntity()).setSaddle(saddle);
            }
        } else {
            ((Pig) npc.getEntity()).setSaddle(saddle);
        }
    }

    public boolean useSaddle() {
        return saddle;
    }

    private static boolean SUPPORT_STEERABLE = true;
}