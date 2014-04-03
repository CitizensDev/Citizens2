package net.citizensnpcs.trait;

import java.util.List;

import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.MobType;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Slime;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.google.common.collect.Lists;

public class PlayerSkin extends Trait {
    private final List<Entity> nameCarriers = Lists.newArrayList();

    public PlayerSkin() {
        super("playerskin");
    }

    private void despawnNameCarriers() {
        if (nameCarriers.isEmpty())
            return;
        for (Entity entity : nameCarriers) {
            entity.remove();
        }
        nameCarriers.clear();
    }

    public String getSkinName() {
        String skin = npc.data().get(NPC.PLAYER_SKIN_NAME_METADATA, "");
        if (skin.isEmpty())
            skin = npc.getFullName();
        return skin;
    }

    public boolean isEnabled() {
        return npc.getTrait(MobType.class).getType() == EntityType.PLAYER
                && !npc.data().get(NPC.PLAYER_SKIN_NAME_METADATA, "").isEmpty();
    }

    @Override
    public void onDespawn() {
        despawnNameCarriers();
    }

    @Override
    public void onRemove() {
        despawnNameCarriers();
    }

    private Entity prepareEntity(String name, EntityType type) {
        NPC npcEntity = npc.getOwningRegistry().createNPC(type, name);
        npcEntity.data().set(NPC.AMBIENT_SOUND_METADATA, "");
        npcEntity.data().set(NPC.DEFAULT_PROTECTED_METADATA, true);
        npcEntity.data().set(NPC.DEATH_SOUND_METADATA, "");
        npcEntity.data().set(NPC.HURT_SOUND_METADATA, "");
        npcEntity.data().set(NPC.SHOULD_SAVE_METADATA, false);
        npcEntity.spawn(npc.getStoredLocation());
        if (name.isEmpty() || !(npcEntity.getEntity() instanceof Slime)) {
            ((LivingEntity) npcEntity.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20
                    * 60 * 60 * 24 * 7, 1));
        } else {
            ((Slime) npcEntity.getEntity()).setSize(-2);
        }
        return npcEntity.getEntity();
    }

    private void refreshPlayer() {
        despawnNameCarriers();

        Location last = npc.getEntity().getLocation();
        npc.despawn(DespawnReason.PENDING_RESPAWN);
        npc.spawn(last);
    }

    @Override
    public void run() {
        if (!npc.isSpawned() || npc.getEntity().getType() != EntityType.PLAYER
                || npc.data().get(NPC.PLAYER_SKIN_NAME_METADATA, "").isEmpty()) {
            despawnNameCarriers();
            return;
        }
        if (nameCarriers.size() == 0) {
            Entity previous = npc.getEntity();
            for (int i = 0; i < 2; i++) {
                Entity heightCarrier = prepareEntity("", EntityType.SKELETON);
                previous.setPassenger(heightCarrier);
                nameCarriers.add(previous = heightCarrier);
            }
            Entity nameCarrier = prepareEntity(npc.getFullName(), EntityType.SLIME);
            previous.setPassenger(nameCarrier);

            nameCarriers.add(nameCarrier);
        }
    }

    public void setSkinName(String name) {
        npc.data().setPersistent(NPC.PLAYER_SKIN_NAME_METADATA, name);
        if (npc.isSpawned()) {
            refreshPlayer();
        }
    }
}
