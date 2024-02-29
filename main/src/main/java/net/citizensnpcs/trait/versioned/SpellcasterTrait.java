package net.citizensnpcs.trait.versioned;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Spellcaster;
import org.bukkit.entity.Spellcaster.Spell;

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

@TraitName("spellcastertrait")
public class SpellcasterTrait extends Trait {
    @Persist
    private Spell spell;

    public SpellcasterTrait() {
        super("spellcastertrait");
    }

    public Spell getSpell() {
        return spell;
    }

    @Override
    public void run() {
        if (!npc.isSpawned() || !(npc.getEntity() instanceof Spellcaster))
            return;
        if (spell != null) {
            ((Spellcaster) npc.getEntity()).setSpell(spell);
        }
    }

    public void setSpell(Spell spell) {
        this.spell = spell;
    }

    @Command(
            aliases = { "npc" },
            usage = "spellcaster (--spell spell)",
            desc = "",
            modifiers = { "spellcaster" },
            min = 1,
            max = 1,
            flags = "d",
            permission = "citizens.npc.spellcaster")
    @Requirements(selected = true, ownership = true, types = { EntityType.EVOKER, EntityType.ILLUSIONER })
    public static void Spellcaster(CommandContext args, CommandSender sender, NPC npc, @Flag("spell") Spell spell)
            throws CommandException {
        SpellcasterTrait trait = npc.getOrAddTrait(SpellcasterTrait.class);
        String output = "";
        if (spell != null) {
            trait.setSpell(spell);
            output += Messaging.tr(Messages.SPELL_SET, spell);
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output.trim());
        } else
            throw new CommandUsageException();
    }
}
