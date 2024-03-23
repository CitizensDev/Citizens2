package net.citizensnpcs.trait.versioned;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
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
import net.citizensnpcs.api.util.Placeholders;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;

@TraitName("bossbar")
public class BossBarTrait extends Trait {
    private BossBar activeBar;
    @Persist
    private BarColor color = BarColor.PURPLE;
    @Persist
    private List<BarFlag> flags = Lists.newArrayList();
    private Supplier<Double> progressProvider;
    @Persist
    private int range = -1;
    @Persist
    private BarStyle style = BarStyle.SOLID;
    @Persist
    private String title = "";
    @Persist
    private String track;
    @Persist
    private String viewPermission;
    @Persist
    private boolean visible = true;

    public BossBarTrait() {
        super("bossbar");
    }

    private BossBar getBar() {
        if (npc.isSpawned() && isBoss(npc.getEntity()) && NMS.getBossBar(npc.getEntity()) != null)
            return (BossBar) NMS.getBossBar(npc.getEntity());

        if (activeBar == null) {
            activeBar = Bukkit.getServer().createBossBar(npc.getFullName(), color, style,
                    flags.toArray(new BarFlag[flags.size()]));
        }
        return activeBar;
    }

    public BarColor getColor() {
        return color;
    }

    public List<BarFlag> getFlags() {
        return flags;
    }

    public int getRange() {
        return range;
    }

    public BarStyle getStyle() {
        return style;
    }

    public String getTitle() {
        return title;
    }

    public String getTrackingVariable() {
        return track;
    }

    public String getViewPermission() {
        return viewPermission;
    }

    private boolean isBoss(Entity entity) {
        boolean isBoss = entity.getType() == EntityType.ENDER_DRAGON || entity.getType() == EntityType.WITHER;
        if (isBoss) {
            onDespawn();
        }
        return isBoss;
    }

    public boolean isVisible() {
        return visible;
    }

    @Override
    public void onDespawn() {
        if (activeBar == null)
            return;

        activeBar.removeAll();
        activeBar.hide();
        activeBar = null;
    }

    @Override
    public void onRemove() {
        onDespawn();
    }

    @Override
    public void run() {
        if (!npc.isSpawned())
            return;

        BossBar bar = getBar();
        if (bar == null)
            return;

        if (track != null && !track.isEmpty()) {
            if (track.equalsIgnoreCase("health")) {
                if (npc.getEntity() instanceof LivingEntity) {
                    LivingEntity entity = (LivingEntity) npc.getEntity();
                    double maxHealth = entity.getMaxHealth();
                    if (SUPPORT_ATTRIBUTES) {
                        try {
                            maxHealth = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                        } catch (Throwable t) {
                            SUPPORT_ATTRIBUTES = false;
                        }
                    }
                    bar.setProgress(entity.getHealth() / maxHealth);
                }
            } else {
                String replaced = Placeholders.replace(track,
                        npc.getEntity() instanceof Player ? (Player) npc.getEntity() : null);
                Double number = Doubles.tryParse(replaced);
                if (number == null)
                    return;
                if (number >= 1 && number <= 100) {
                    number /= 100.0;
                }
                bar.setProgress(Math.max(0, Math.min(1, number)));
            }
        }
        bar.setTitle(title);
        bar.setVisible(visible);
        if (progressProvider != null) {
            bar.setProgress(progressProvider.get());
        }
        if (style != null) {
            bar.setStyle(style);
        }
        if (color != null) {
            bar.setColor(color);
        }
        for (BarFlag flag : BarFlag.values()) {
            bar.removeFlag(flag);
        }
        for (BarFlag flag : flags) {
            bar.addFlag(flag);
        }
        bar.removeAll();

        for (Player player : CitizensAPI.getLocationLookup().getNearbyPlayers(npc.getEntity().getLocation(),
                range > 0 ? range : Setting.BOSSBAR_RANGE.asInt())) {
            if (viewPermission != null && !player.hasPermission(viewPermission))
                continue;

            bar.addPlayer(player);
        }
    }

    public void setColor(BarColor color) {
        this.color = color;
    }

    public void setFlags(Collection<BarFlag> flags) {
        this.flags = Lists.newArrayList(flags);
    }

    public void setFlags(List<BarFlag> flags) {
        this.flags = flags;
    }

    public void setProgressProvider(Supplier<Double> provider) {
        progressProvider = provider;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public void setStyle(BarStyle style) {
        this.style = style;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTrackVariable(String variable) {
        track = variable;
    }

    public void setViewPermission(String viewpermission) {
        viewPermission = viewpermission;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Command(
            aliases = { "npc" },
            usage = "bossbar --style [style] --color [color] --title [title] --visible [visible] --viewpermission [permission] --flags [flags] --track [health | placeholder] --range [range]",
            desc = "",
            modifiers = { "bossbar" },
            min = 1,
            max = 1)
    @Requirements(selected = true, ownership = true)
    public static void bossbar(CommandContext args, CommandSender sender, NPC npc, @Flag("style") BarStyle style,
            @Flag("track") String track, @Flag("color") BarColor color, @Flag("visible") Boolean visible,
            @Flag("range") Integer range, @Flag("title") String title, @Flag("flags") String flags,
            @Flag("viewpermission") String viewpermission) throws CommandException {
        BossBarTrait trait = npc.getOrAddTrait(BossBarTrait.class);
        if (style != null) {
            trait.setStyle(style);
        }
        if (color != null) {
            trait.setColor(color);
        }
        if (track != null) {
            trait.setTrackVariable(track);
        }
        if (title != null) {
            trait.setTitle(Messaging.parseComponents(title));
        }
        if (visible != null) {
            trait.setVisible(visible);
        }
        if (range != null) {
            trait.setRange(range);
        }
        if (viewpermission != null) {
            trait.setViewPermission(viewpermission);
        }
        if (flags != null) {
            List<BarFlag> parsed = Lists.newArrayList();
            for (String s : Splitter.on(',').omitEmptyStrings().trimResults().split(flags)) {
                BarFlag flag = Util.matchEnum(BarFlag.values(), s);
                if (flag != null) {
                    parsed.add(flag);
                }
            }
            trait.setFlags(parsed);
        }
    }

    private static boolean SUPPORT_ATTRIBUTES = true;
}
