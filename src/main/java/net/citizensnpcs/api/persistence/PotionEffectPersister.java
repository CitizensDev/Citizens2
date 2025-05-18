package net.citizensnpcs.api.persistence;

import org.bukkit.Bukkit;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.SpigotUtil;

public class PotionEffectPersister implements Persister<PotionEffect> {
    @Override
    public PotionEffect create(DataKey root) {
        return new PotionEffect(
                Bukkit.getRegistry(PotionEffectType.class).get(SpigotUtil.getKey(root.getString("type"))),
                root.getInt("duration"), root.getInt("amplifier"), root.getBoolean("ambient"),
                root.getBoolean("particles"), root.getBoolean("icon"));
    }

    @Override
    public void save(PotionEffect effect, DataKey root) {
        root.setString("type", effect.getType().getKeyOrNull().toString());
        root.setInt("amplifier", effect.getAmplifier());
        root.setInt("duration", effect.getDuration());
        root.setBoolean("particles", effect.hasParticles());
        root.setBoolean("ambient", effect.isAmbient());
        root.setBoolean("icon", effect.hasIcon());
    }
}
