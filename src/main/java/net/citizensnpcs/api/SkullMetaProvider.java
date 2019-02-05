package net.citizensnpcs.api;

import org.bukkit.inventory.meta.SkullMeta;

public interface SkullMetaProvider {
    public String getTexture(SkullMeta meta);

    public void setTexture(String string, SkullMeta meta);
}
