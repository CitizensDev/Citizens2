package net.citizensnpcs.trait.versioned;

import java.util.Collection;
import java.util.List;

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
    private BossBar barCache;
    @Persist
    private BarColor color = BarColor.PURPLE;
    @Persist
    private List<BarFlag> flags = Lists.newArrayList();
    @Persist
    private int range = -1;
    @Persist
    private BarStyle style = BarStyle.SOLID;
    @Persist
    private String title = "";
    @Persist
    private String track;
    @Persist
    private boolean visible = true;

    public BossBarTrait() {
        super("bossbar");
    }

    private BossBar getBar() {
        if (npc.isSpawned() && isBoss(npc.getEntity()))
            return (BossBar) NMS.getBossBar(npc.getEntity());
        if (barCache == null) {
            barCache = Bukkit.getServer().createBossBar(npc.getFullName(), color, style,
                    flags.toArray(new BarFlag[flags.size()]));
        }
        return barCache;
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

    private boolean isBoss(Entity entity) {
        boolean isBoss = entity.getType() == EntityType.ENDER_DRAGON || entity.getType() == EntityType.WITHER
                || entity.getType() == EntityType.GUARDIAN;
        if (isBoss) {
            barCache = null;
        }
        return isBoss;
    }

    public boolean isVisible() {
        return visible;
    }

    @Override
    public void onDespawn() {
        if (barCache == null)
            return;
        barCache.removeAll();
        barCache.hide();
        barCache = null;
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
        if (bar == null) {
            return;
        }
        if (track != null && !track.isEmpty() && npc.getEntity() instanceof LivingEntity) {
            LivingEntity entity = (LivingEntity) npc.getEntity();
            if (track.equalsIgnoreCase("health")) {
                double maxHealth = entity.getMaxHealth();
                if (SUPPORT_ATTRIBUTES) {
                    try {
                        maxHealth = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                    } catch (Throwable t) {
                        SUPPORT_ATTRIBUTES = false;
                    }
                }
                bar.setProgress(entity.getHealth() / maxHealth);
            } else if (track != null && !track.isEmpty()) {
                String replaced = Placeholders.replace(track,
                        npc.getEntity() instanceof Player ? (Player) npc.getEntity() : null);
                Double number = Doubles.tryParse(replaced);
                if (number == null)
                    return;
                if (number >= 1 && number <= 100) {
                    number /= 100.0;
                }
                bar.setProgress(Math.min(0, Math.max(1, number)));
            }
        }
        bar.setTitle(title);
        bar.setVisible(visible);
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
        if (barCache != null) {
            barCache.removeAll();
            for (Player player : CitizensAPI.getLocationLookup().getNearbyPlayers(npc.getEntity().getLocation(),
                    range > 0 ? range : Setting.BOSSBAR_RANGE.asInt())) {
                barCache.addPlayer(player);
            }
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
        this.track = variable;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Command(
            aliases = { "npc" },
            usage = "bossbar --style [style] --color [color] --title [title] --visible [visible] --flags [flags] --track [health | placeholder] --range [range]",
            desc = "Edit bossbar properties",
            modifiers = { "bossbar" },
            min = 1,
            max = 1)
    @Requirements(selected = true, ownership = true)
    public static void bossbar(CommandContext args, CommandSender sender, NPC npc, @Flag("style") BarStyle style,
            @Flag("track") String track, @Flag("color") BarColor color, @Flag("visible") Boolean visible,
            @Flag("range") Integer range, @Flag("title") String title, @Flag("flags") String flags)
            throws CommandException {
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
