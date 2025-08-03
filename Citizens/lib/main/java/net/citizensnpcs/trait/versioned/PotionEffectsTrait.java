package net.citizensnpcs.trait.versioned;

import java.util.List;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.citizensnpcs.api.command.Arg;
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

@TraitName("potioneffects")
public class PotionEffectsTrait extends Trait {
    @Persist(valueType = PotionEffect.class)
    private final Map<String, PotionEffect> persistent = Maps.newHashMap();
    private final List<PotionEffect> temporary = Lists.newArrayList();

    public PotionEffectsTrait() {
        super("potioneffects");
    }

    public void addEffect(PotionEffect effect) {
        temporary.add(effect);
    }

    public void addPersistentEffect(String name, PotionEffect effect) {
        persistent.put(name, effect);
        if (npc.isSpawned() && npc.getEntity().getType().isAlive()) {
            ((LivingEntity) npc.getEntity()).addPotionEffect(effect);
        }
    }

    public Map<String, PotionEffect> getPersistentEffects() {
        return persistent;
    }

    @Override
    public void onSpawn() {
        if (!npc.getEntity().getType().isAlive())
            return;
        LivingEntity entity = (LivingEntity) npc.getEntity();
        for (PotionEffect effect : persistent.values()) {
            entity.addPotionEffect(effect);
        }
    }

    public void removePersistentEffect(String name) {
        persistent.remove(name);
    }

    @Override
    public void run() {
        if (!npc.isSpawned() || !npc.getEntity().getType().isAlive())
            return;
        LivingEntity entity = (LivingEntity) npc.getEntity();
        for (PotionEffect effect : temporary) {
            entity.addPotionEffect(effect);
        }
        temporary.clear();
    }

    @Command(
            aliases = { "npc" },
            usage = "potioneffect [add|remove|list] (--name [name] or -t for temporary) (--type [type]) (--duration [duration]) (--amplifier [amplifier]) (--icon [icon])",
            desc = "",
            modifiers = { "potioneffect" },
            flags = "it",
            min = 3,
            max = 4,
            permission = "citizens.npc.potioneffect")
    @Requirements(selected = true, ownership = true, livingEntity = true)
    public static void potioneffect(CommandContext args, CommandSender sender, NPC npc,
            @Arg(value = 1, completions = { "add", "list", "remove" }) String operation,
            @Flag(value = "duration", defValue = "-1") Integer duration, @Flag("name") String name,
            @Flag(value = "amplifier", defValue = "1") Integer amplifier, @Flag("type") PotionEffectType type,
            @Flag(value = "icon", defValue = "false") Boolean icon,
            @Flag(value = "ambient", defValue = "false") Boolean ambient,
            @Flag(value = "particles", defValue = "false") Boolean particles) throws CommandException {
        PotionEffectsTrait trait = npc.getOrAddTrait(PotionEffectsTrait.class);
        if (operation.equals("add")) {
            if (type == null)
                throw new CommandUsageException();
            if (name == null && !args.hasFlag('t'))
                throw new CommandUsageException();
            if (args.hasFlag('i')) {
                duration = -1;
            }
            PotionEffect effect = new PotionEffect(type, duration, amplifier, ambient, particles, icon);
            if (args.hasFlag('t')) {
                trait.addEffect(effect);
            } else {
                trait.addPersistentEffect(name, effect);
            }
            Messaging.sendTr(sender, Messages.POTION_EFFECT_ADDED, effect);
        } else if (operation.equals("list")) {
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<String, PotionEffect> entry : trait.getPersistentEffects().entrySet()) {
                builder.append("[[-]] " + entry.getKey() + ": " + entry.getValue());
            }
            Messaging.send(sender, builder.toString());
        } else {
            if (name == null)
                throw new CommandUsageException();
            trait.removePersistentEffect(name);
            Messaging.sendTr(sender, Messages.POTION_EFFECT_REMOVED, name);
        }
    }
}