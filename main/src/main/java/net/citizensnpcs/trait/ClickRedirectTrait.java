package net.citizensnpcs.trait;

import java.util.UUID;

import org.bukkit.event.EventHandler;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCAddTraitEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.trait.trait.PlayerFilter;
import net.citizensnpcs.api.util.DataKey;

/**
 * Redirects left and right clicks to another {@link NPC}.
 */
@TraitName("clickredirecttrait")
public class ClickRedirectTrait extends Trait {
    private NPC redirectTo;

    public ClickRedirectTrait() {
        super("clickredirecttrait");
    }

    public ClickRedirectTrait(NPC redirectTo) {
        this();
        this.redirectTo = redirectTo;
    }

    public NPC getRedirectToNPC() {
        return redirectTo;
    }

    @Override
    public void linkToNPC(NPC npc) {
        super.linkToNPC(npc);
        if (redirectTo != null && redirectTo.hasTrait(PlayerFilter.class)) {
            redirectTo.getOrAddTrait(PlayerFilter.class).addChildNPC(npc);
        }
    }

    @Override
    public void load(DataKey key) {
        redirectTo = CitizensAPI.getNPCRegistry().getByUniqueIdGlobal(UUID.fromString(key.getString("uuid")));
    }

    @EventHandler
    public void onTraitAdd(NPCAddTraitEvent event) {
        if (event.getNPC() == redirectTo && event.getTrait() instanceof PlayerFilter) {
            ((PlayerFilter) event.getTrait()).addChildNPC(npc);
        }
    }

    @Override
    public void save(DataKey key) {
        key.removeKey("uuid");
        if (redirectTo == null)
            return;
        key.setString("uuid", redirectTo.getUniqueId().toString());
    }
}
