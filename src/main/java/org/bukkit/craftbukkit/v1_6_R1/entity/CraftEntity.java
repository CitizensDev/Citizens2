package org.bukkit.craftbukkit.v1_6_R1.entity;

import org.bukkit.entity.Entity;

public abstract class CraftEntity implements Entity {
    public net.minecraft.server.v1_6_R1.Entity getHandle() {
        return null;
    }
}
