package net.citizensnpcs.trait.versioned;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Display;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.Display.Brightness;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;

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

@TraitName("displaytrait")
public class DisplayTrait extends Trait {
    @Persist
    private Billboard billboard;
    @Persist
    private Integer blockLight;
    @Persist
    private Float height;
    @Persist
    private Integer interpolationDelay;
    @Persist
    private Integer interpolationDuration;
    @Persist
    private Quaternionf leftRotation;
    @Persist
    private Quaternionf rightRotation;
    @Persist
    private Vector scale;
    @Persist
    private Integer skyLight;
    @Persist
    private Float viewRange;
    @Persist
    private Float width;

    public DisplayTrait() {
        super("displaytrait");
    }

    @Override
    public void onSpawn() {
        Display display = (Display) npc.getEntity();
        if (billboard != null) {
            display.setBillboard(billboard);
        }
        if (blockLight != null && skyLight != null) {
            display.setBrightness(new Brightness(blockLight, skyLight));
        }
        if (interpolationDelay != null) {
            display.setInterpolationDelay(interpolationDelay);
        }
        if (interpolationDuration != null) {
            display.setInterpolationDuration(interpolationDuration);
        }
        if (height != null) {
            display.setDisplayHeight(height);
        }
        if (width != null) {
            display.setDisplayWidth(width);
        }
        Transformation tf = display.getTransformation();
        if (scale != null) {
            tf.getScale().set(scale.getX(), scale.getY(), scale.getZ());
        }
        if (leftRotation != null) {
            tf.getLeftRotation().set(leftRotation);
        }
        if (rightRotation != null) {
            tf.getRightRotation().set(rightRotation);
        }
        display.setTransformation(tf);
        if (viewRange != null) {
            display.setViewRange(viewRange);
        }
    }

    public void setBillboard(Billboard billboard) {
        this.billboard = billboard;
    }

    public void setBrightness(Brightness brightness) {
        this.blockLight = brightness.getBlockLight();
        this.skyLight = brightness.getSkyLight();
    }

    public void setHeight(Float height) {
        this.height = height;
    }

    public void setInterpolationDelay(Integer interpolationDelay) {
        this.interpolationDelay = interpolationDelay;
    }

    public void setInterpolationDuration(Integer interpolationDuration) {
        this.interpolationDuration = interpolationDuration;
    }

    public void setScale(Vector scale) {
        this.scale = scale;
    }

    public void setViewRange(Float viewRange) {
        this.viewRange = viewRange;
    }

    public void setWidth(Float width) {
        this.width = width;
    }

    @Command(
            aliases = { "npc" },
            usage = "display --billboard [billboard] --brightness [blockLight,skyLight] --interpolationdelay [delay] --interpolationduration [duration] --height [height] --width [width] --scale [x,y,z] --viewrange [range] --leftrotation [x,y,z,w] --rightrotation [x,y,z,w]",
            desc = "",
            modifiers = { "display" },
            min = 1,
            max = 1,
            permission = "citizens.npc.display")
    @Requirements(
            selected = true,
            ownership = true,
            types = { EntityType.ITEM_DISPLAY, EntityType.TEXT_DISPLAY, EntityType.BLOCK_DISPLAY })
    public static void display(CommandContext args, CommandSender sender, NPC npc,
            @Flag("billboard") Billboard billboard, @Flag("leftrotation") Quaternionf leftrotation,
            @Flag("rightrotation") Quaternionf rightrotation, @Flag("scale") Vector scale,
            @Flag("viewrange") Float viewRange, @Flag("brightness") String brightness,
            @Flag("interpolationdelay") Integer interpolationDelay,
            @Flag("interpolationduration") Integer interpolationDuration, @Flag("height") Float height,
            @Flag("width") Float width) throws CommandException {
        DisplayTrait trait = npc.getOrAddTrait(DisplayTrait.class);
        String output = "";
        if (billboard != null) {
            trait.setBillboard(billboard);
        }
        if (brightness != null) {
            trait.setBrightness(new Brightness(Integer.parseInt(brightness.split(",")[0]),
                    Integer.parseInt(brightness.split(",")[1])));
        }
        if (interpolationDelay != null) {
            trait.setInterpolationDelay(interpolationDelay);
        }
        if (interpolationDuration != null) {
            trait.setInterpolationDuration(interpolationDuration);
        }
        if (width != null) {
            trait.setWidth(width);
        }
        if (height != null) {
            trait.setHeight(height);
        }
        if (viewRange != null) {
            trait.setViewRange(viewRange);
        }
        if (scale != null) {
            trait.setScale(scale);
        }
        trait.onSpawn();
        if (!output.isEmpty()) {
            Messaging.send(sender, output.trim());
        } else
            throw new CommandUsageException();
    }
}
