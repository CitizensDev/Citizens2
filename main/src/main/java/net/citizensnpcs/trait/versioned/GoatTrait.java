package net.citizensnpcs.trait.versioned;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Goat;

import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;

@TraitName("goattrait")
public class GoatTrait extends Trait {
    @Persist
    private boolean leftHorn = true;
    @Persist
    private boolean rightHorn = true;

    public GoatTrait() {
        super("goattrait");
    }

    public boolean isLeftHorn() {
        return leftHorn;
    }

    public boolean isRightHorn() {
        return rightHorn;
    }

    @Override
    public void run() {
        if (npc.isSpawned() && npc.getEntity() instanceof Goat) {
            Goat goat = (Goat) npc.getEntity();
            if (SUPPORTS_HORNS) {
                try {
                    goat.setRightHorn(rightHorn);
                    goat.setLeftHorn(leftHorn);
                } catch (Throwable t) {
                    SUPPORTS_HORNS = false;
                }
            }
        }
    }

    public void setLeftHorn(boolean horn) {
        leftHorn = horn;
    }

    public void setRightHorn(boolean horn) {
        rightHorn = horn;
    }

    @Command(
            aliases = { "npc" },
            usage = "goat -l(eft) -r(ight) -n(either) -b(oth) horn",
            desc = "Sets goat modifiers",
            modifiers = { "goat" },
            flags = "lrnb",
            min = 1,
            max = 1,
            permission = "citizens.npc.goat")
    @Requirements(selected = true, ownership = true, types = EntityType.GOAT)
    public static void goat(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        GoatTrait trait = npc.getOrAddTrait(GoatTrait.class);
        boolean left = trait.isLeftHorn(), right = trait.isRightHorn();
        if (args.hasFlag('l')) {
            left = !left;
        }
        if (args.hasFlag('r')) {
            right = !right;
        }
        if (args.hasFlag('b')) {
            left = right = true;
        }
        if (args.hasFlag('n')) {
            left = right = false;
        }
        trait.setLeftHorn(left);
        trait.setRightHorn(right);
        String output = Messaging.tr(Messages.NPC_GOAT_HORNS_SET, npc.getName(), left, right);
        if (!output.isEmpty()) {
            Messaging.send(sender, output);
        }
    }

    private static boolean SUPPORTS_HORNS = true;
}
