package net.citizensnpcs.api.ai;

import org.bukkit.entity.LivingEntity;

public interface AttackStrategy {
    /**
     * Tries to attack the supplied target from the supplied attacker. Returns <code>true</code> if the attack was
     * handled, or false if the default attack strategy should be used.
     * 
     * @param attacker
     *            The entity attacker to use
     * @param target
     *            The target to attack
     * @return Whether the attack was handled
     */
    public boolean handle(LivingEntity attacker, LivingEntity target);
}
