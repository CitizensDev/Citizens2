package net.citizensnpcs.trait.versioned;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform;

import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Flag;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.Messaging;

@TraitName("itemdisplaytrait")
public class ItemDisplayTrait extends Trait implements Cloneable {
    @Persist
    private ItemDisplayTransform transform;

    public ItemDisplayTrait() {
        super("itemdisplaytrait");
    }

    public ItemDisplayTransform getTransform() {
        return transform;
    }

    @Override
    public void onSpawn() {
        ItemDisplay display = (ItemDisplay) npc.getEntity();
        if (transform != null) {
            display.setItemDisplayTransform(transform);
        }
    }

    public void setTransform(ItemDisplayTransform transform) {
        this.transform = transform;
    }

    @Command(
            aliases = { "npc" },
            usage = "itemdisplay --transform [transform]",
            desc = "",
            modifiers = { "itemdisplay" },
            min = 1,
            max = 1,
            permission = "citizens.npc.itemdisplay")
    @Requirements(selected = true, ownership = true, types = { EntityType.ITEM_DISPLAY })
    public static void itemdisplay(CommandContext args, CommandSender sender, NPC npc,
            @Flag("transform") ItemDisplayTransform transform) throws CommandException {
        ItemDisplayTrait trait = npc.getOrAddTrait(ItemDisplayTrait.class);
        String output = "";
        if (transform != null) {
            trait.setTransform(transform);
        }
        if (npc.isSpawned()) {
            trait.onSpawn();
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output.trim());
        }
    }
}
