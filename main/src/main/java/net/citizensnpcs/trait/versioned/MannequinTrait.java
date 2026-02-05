package net.citizensnpcs.trait.versioned;

import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mannequin;
import org.bukkit.inventory.MainHand;

import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Flag;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPC.NPCUpdate;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.npc.skin.SkinnableEntity.PlayerSkinModelType;
import net.citizensnpcs.trait.SkinTrait;
import net.citizensnpcs.util.NMS;

@TraitName("mannequintrait")
public class MannequinTrait extends Trait {
    @Persist
    private String description;
    @Persist
    private boolean hideDescription = true;
    @Persist
    private boolean immovable;
    @Persist
    private MainHand mainHand;

    public MannequinTrait() {
        super("mannequintrait");
    }

    @Override
    public void run() {
        if (npc.getCosmeticEntity() instanceof Mannequin) {
            Mannequin mannequin = (Mannequin) npc.getCosmeticEntity();
            if (mainHand != null) {
                mannequin.setMainHand(mainHand);
            }
            if (!hideDescription && npc.isUpdating(NPCUpdate.PACKET)) {
                NMS.setMannequinDescription(mannequin,
                        description == null ? null : Messaging.minecraftComponentFromRawMessage(description));
            }
            mannequin.setImmovable(immovable);
            mannequin.setHideDescription(hideDescription);
        }
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setHideDescription(boolean hide) {
        this.hideDescription = hide;
    }

    public void setImmovable(boolean immovable) {
        this.immovable = immovable;
    }

    public void setMainHand(MainHand hand) {
        this.mainHand = hand;
    }

    @Command(
            aliases = { "npc" },
            usage = "mannequin --hide_description [true|false] --immovable [true|false] --description [description] --main_hand [LEFT|RIGHT] --skin_patch_type [type] --skin_patch_body [location] --skin_patch_cape [location] --skin_patch_elytra [location]",
            desc = "",
            modifiers = { "mannequin" },
            min = 1,
            max = 1,
            permission = "citizens.npc.mannequin")
    @Requirements(selected = true, ownership = true, cosmeticTypes = EntityType.MANNEQUIN)
    public static void mannequin(CommandContext args, CommandSender sender, NPC npc,
            @Flag("description") String description, @Flag("immovable") Boolean immovable,
            @Flag("hide_description") Boolean hideDescription, @Flag("main_hand") MainHand mainHand,
            @Flag("show_part") Object show, @Flag("hide_part") Object hide,
            @Flag("skin_patch_type") PlayerSkinModelType type, @Flag("skin_patch_body") NamespacedKey body,
            @Flag("skin_patch_cape") NamespacedKey cape, @Flag("skin_patch_elytra") NamespacedKey elytra)
            throws CommandException {
        MannequinTrait trait = npc.getOrAddTrait(MannequinTrait.class);
        String output = "";
        if (description != null) {
            trait.setDescription(description);
        }
        if (hideDescription != null) {
            trait.setHideDescription(hideDescription);
        }
        if (immovable != null) {
            trait.setImmovable(immovable);
        }
        if (mainHand != null) {
            trait.setMainHand(mainHand);
        }
        if (body != null || cape != null || type != null || elytra != null) {
            npc.getOrAddTrait(SkinTrait.class).setSkinPatch(type, body, cape, elytra);
        }
        if (!output.isEmpty()) {
            Messaging.send(sender, output);
        }
    }
}