package net.citizensnpcs.trait.versioned;

import java.util.Collection;
import java.util.List;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import com.google.common.collect.Lists;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.util.NMS;

@TraitName("bossbar")
public class BossBarTrait extends Trait {
    @Persist("color")
    private BarColor color = null;
    @Persist("flags")
    private List<BarFlag> flags = Lists.newArrayList();
    @Persist("title")
    private String title = null;
    @Persist("visible")
    private boolean visible = true;

    public BossBarTrait() {
        super("bossbar");
    }

    private boolean isBoss(Entity entity) {
        return entity.getType() == EntityType.ENDER_DRAGON || entity.getType() == EntityType.WITHER
                || entity.getType() == EntityType.GUARDIAN;
    }

    @Override
    public void run() {
        if (!npc.isSpawned() || !isBoss(npc.getEntity()))
            return;
        BossBar bar = (BossBar) NMS.getBossBar(npc.getEntity());
        if (bar == null) {
            return;
        }
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
    }

    public void setColor(BarColor color) {
        this.color = color;
    }

    public void setFlags(Collection<BarFlag> flags) {
        this.flags = Lists.newArrayList(flags);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
