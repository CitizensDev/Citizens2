package net.citizensnpcs.trait.versioned;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionType;

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

@TraitName("areaeffectcloudtrait")
public class AreaEffectCloudTrait extends Trait {
    @Persist
    private Color color;
    @Persist
    private Integer duration;
    @Persist
    private Particle particle;
    @Persist
    private Float radius;
    @Persist
    private PotionType type;

    public AreaEffectCloudTrait() {
        super("areaeffectcloudtrait");
    }

    @Override
    public AreaEffectCloudTrait clone() {
        AreaEffectCloudTrait copy = new AreaEffectCloudTrait();
        copy.color = color;
        copy.duration = duration;
        copy.particle = particle;
        copy.radius = radius;
        copy.type = type;
        return copy;
    }

    @Override
    public void onSpawn() {
        if (!(npc.getCosmeticEntity() instanceof AreaEffectCloud))
            return;
        AreaEffectCloud cloud = (AreaEffectCloud) npc.getCosmeticEntity();
        if (color != null) {
            cloud.setColor(color);
        }
        if (radius != null) {
            cloud.setRadius(radius);
        }
        if (duration != null) {
            cloud.setDuration(duration);
        }
        if (particle != null) {
            cloud.setParticle(particle);
        }
        if (type != null) {
            cloud.setBasePotionType(type);
        }
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public void setParticle(Particle particle) {
        this.particle = particle;
    }

    public void setPotionType(PotionType type) {
        this.type = type;
    }

    public void setRadius(Float radius) {
        this.radius = radius;
    }

    @Command(
            aliases = { "npc" },
            usage = "areaeffectcloud --color [color] --duration [duration] --radius [radius] --particle [particle]",
            desc = "",
            modifiers = { "areaeffectcloud" },
            min = 1,
            max = 1,
            permission = "citizens.npc.areaeffectcloud")
    @Requirements(selected = true, ownership = true, cosmeticTypes = { EntityType.AREA_EFFECT_CLOUD })
    public static void areaeffectcloud(CommandContext args, CommandSender sender, NPC npc,
            @Flag("duration") Integer duration, @Flag("radius") Float radius, @Flag("color") Color color,
            @Flag("potiontype") PotionType type, @Flag("particle") Particle particle) throws CommandException {
        AreaEffectCloudTrait trait = npc.getOrAddTrait(AreaEffectCloudTrait.class);
        String output = "";
        if (radius != null) {
            trait.setRadius(radius);
        }
        if (duration != null) {
            trait.setDuration(duration);
        }
        if (color != null) {
            trait.setColor(color);
        }
        if (type != null) {
            trait.setPotionType(type);
        }
        if (particle != null) {
            trait.setParticle(particle);
        }
        trait.onSpawn();
        if (!output.isEmpty()) {
            Messaging.send(sender, output.trim());
        }
    }
}
