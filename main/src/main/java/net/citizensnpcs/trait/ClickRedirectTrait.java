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
    private NPC redirectNPC;

    public ClickRedirectTrait() {
        super("clickredirecttrait");
    }

    public ClickRedirectTrait(NPC npc) {
        this();
        redirectNPC = npc;
        if (redirectNPC != null && redirectNPC.hasTrait(PlayerFilter.class)) {
            redirectNPC.getOrAddTrait(PlayerFilter.class).addChildNPC(npc);
        }
    }

    public NPC getRedirectNPC() {
        return redirectNPC;
    }

    @Override
    public void load(DataKey key) {
        redirectNPC = CitizensAPI.getNPCRegistry().getByUniqueIdGlobal(UUID.fromString(key.getString("uuid")));
    }

    @EventHandler
    public void onTraitAdd(NPCAddTraitEvent event) {
        if (event.getNPC() == redirectNPC && event.getTrait() instanceof PlayerFilter) {
            ((PlayerFilter) event.getTrait()).addChildNPC(npc);
        }
    }

    @Override
    public void save(DataKey key) {
        key.removeKey("uuid");
        if (redirectNPC == null)
            return;
        key.setString("uuid", redirectNPC.getUniqueId().toString());
    }
}
