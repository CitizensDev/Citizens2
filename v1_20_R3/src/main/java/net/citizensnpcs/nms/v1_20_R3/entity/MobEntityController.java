package net.citizensnpcs.nms.v1_20_R3.entity;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.entity.Entity;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_20_R3.util.NMSImpl;
import net.citizensnpcs.nms.v1_20_R3.util.PitchableLookControl;
import net.citizensnpcs.npc.AbstractEntityController;
import net.citizensnpcs.trait.ScoreboardTrait;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.level.Level;

public abstract class MobEntityController extends AbstractEntityController {
    private final Class<?> clazz;

    protected MobEntityController(Class<?> clazz) {
        super(clazz);
        this.clazz = clazz;
    }

    @Override
    protected Entity createEntity(Location at, NPC npc) {
        EntityType<?> type = NMSImpl.getEntityType(clazz);
        net.minecraft.world.entity.Entity entity = createEntityFromClass(type, ((CraftWorld) at.getWorld()).getHandle(),
                npc);
        if (entity instanceof Mob) {
            NMSImpl.clearGoals(npc, ((Mob) entity).goalSelector, ((Mob) entity).targetSelector);
            Mob mob = (Mob) entity;
            if (mob.getLookControl().getClass() == LookControl.class) {
                NMSImpl.setLookControl(mob, new PitchableLookControl(mob));
            }
        }
        entity.absMoveTo(at.getX(), at.getY(), at.getZ(), at.getYaw(), at.getPitch());
        if (npc != null) {
            // entity.onGround isn't updated right away - we approximate here so
            // that things like pathfinding still work *immediately* after spawn.
            org.bukkit.Material beneath = at.getBlock().getRelative(BlockFace.DOWN).getType();
            if (beneath.isSolid()) {
                entity.setOnGround(true);
            }
            entity.setUUID(npc.getUniqueId());
            if (Setting.USE_SCOREBOARD_TEAMS.asBoolean()) {
                npc.getOrAddTrait(ScoreboardTrait.class).createTeam(npc.getUniqueId().toString());
            }
        }
        return entity.getBukkitEntity();
    }

    private net.minecraft.world.entity.Entity createEntityFromClass(Object... args) {
        try {
            return (net.minecraft.world.entity.Entity) getConstructor(clazz).newInstance(args);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static Constructor<?> getConstructor(Class<?> clazz) {
        Constructor<?> constructor = CONSTRUCTOR_CACHE.get(clazz);
        if (constructor != null)
            return constructor;
        try {
            CONSTRUCTOR_CACHE.put(clazz, constructor = clazz.getConstructor(EntityType.class, Level.class, NPC.class));
            return constructor;
        } catch (Exception ex) {
            throw new IllegalStateException("unable to find an entity constructor");
        }
    }

    private static final Map<Class<?>, Constructor<?>> CONSTRUCTOR_CACHE = new WeakHashMap<>();
}