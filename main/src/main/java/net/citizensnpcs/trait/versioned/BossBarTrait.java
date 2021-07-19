package net.citizensnpcs.trait.versioned;

import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.util.NMS;

@TraitName("bossbar")
public class BossBarTrait extends Trait {
    private BossBar barCache;
    @Persist("color")
    private BarColor color = BarColor.PURPLE;
    @Persist("flags")
    private List<BarFlag> flags = Lists.newArrayList();
    @Persist("style")
    private BarStyle style = BarStyle.SOLID;
    @Persist("title")
    private String title = "";
    @Persist("visible")
    private boolean visible = true;

    public BossBarTrait() {
        super("bossbar");
    }

    public BarColor getColor() {
        return color;
    }

    public List<BarFlag> getFlags() {
        return flags;
    }

    public BarStyle getStyle() {
        return style;
    }

    public String getTitle() {
        return title;
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
    public void run() {
        if (!npc.isSpawned())
            return;
        BossBar bar = isBoss(npc.getEntity()) ? (BossBar) NMS.getBossBar(npc.getEntity())
                : barCache == null ? barCache = Bukkit.getServer().createBossBar(npc.getFullName(), color, style,
                        flags.toArray(new BarFlag[flags.size()])) : barCache;
        if (bar == null) {
            return;
        }
        bar.setStyle(style);
        bar.setVisible(visible);
        if (color != null) {
            bar.setColor(color);
        }
        if (title != null) {
            bar.setTitle(title);
        }
        for (BarFlag flag : BarFlag.values()) {
            bar.removeFlag(flag);
        }
        for (BarFlag flag : flags) {
            bar.addFlag(flag);
        }
        if (barCache != null) {
            barCache.removeAll();
            for (Entity entity : npc.getEntity().getNearbyEntities(Setting.BOSSBAR_RANGE.asInt() / 2,
                    Setting.BOSSBAR_RANGE.asInt() / 2, Setting.BOSSBAR_RANGE.asInt() / 2)) {
                if (entity instanceof Player) {
                    barCache.addPlayer((Player) entity);
                }
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

    public void setStyle(BarStyle style) {
        this.style = style;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
