package net.citizensnpcs.nms.v1_17_R1.util;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;

import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.npc.BlockBreaker;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.PlayerAnimation;
import net.citizensnpcs.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class CitizensBlockBreaker extends BlockBreaker {
    private final BlockBreakerConfiguration configuration;
    private int currentDamage;
    private int currentTick;
    private final Entity entity;
    private boolean isDigging = true;
    private final Location location;
    private boolean setTarget;
    private int startDigTick;
    private final int x, y, z;

    public CitizensBlockBreaker(org.bukkit.entity.Entity entity, org.bukkit.block.Block target,
            BlockBreakerConfiguration config) {
        this.entity = ((CraftEntity) entity).getHandle();
        this.x = target.getX();
        this.y = target.getY();
        this.z = target.getZ();
        this.location = target.getLocation();
        this.startDigTick = (int) (System.currentTimeMillis() / 50);
        this.configuration = config;
    }

    private double distanceSquared() {
        return Math.pow(entity.getX() - x, 2) + Math.pow(NMS.getHeight(entity.getBukkitEntity()) + entity.getY() - y, 2)
                + Math.pow(entity.getZ() - z, 2);
    }

    private ItemStack getCurrentItem() {
        return configuration.item() != null ? CraftItemStack.asNMSCopy(configuration.item())
                : entity instanceof LivingEntity ? ((LivingEntity) entity).getMainHandItem() : null;
    }

    private float getStrength(BlockState block) {
        float base = block.getDestroySpeed(null, BlockPos.ZERO);
        return base < 0.0F ? 0.0F : (!isDestroyable(block) ? 1.0F / base / 100.0F : strengthMod(block) / base / 30.0F);
    }

    private boolean isDestroyable(BlockState block) {
        if (block.requiresCorrectToolForDrops()) {
            return true;
        } else {
            ItemStack current = getCurrentItem();
            return current != null ? current.isCorrectToolForDrops(block) : false;
        }
    }

    @Override
    public void reset() {
        if (setTarget && entity instanceof NPCHolder) {
            NPC npc = ((NPCHolder) entity).getNPC();
            if (npc != null && npc.getNavigator().isNavigating()) {
                npc.getNavigator().cancelNavigation();
            }
        }
        setTarget = false;
        if (configuration.callback() != null) {
            configuration.callback().run();
        }
        isDigging = false;
        setBlockDamage(currentDamage = -1);
    }

    @Override
    public BehaviorStatus run() {
        if (entity.isRemoved()) {
            return BehaviorStatus.FAILURE;
        }
        if (!isDigging) {
            return BehaviorStatus.SUCCESS;
        }
        currentTick = (int) (System.currentTimeMillis() / 50); // CraftBukkit
        if (configuration.radiusSquared() > 0 && distanceSquared() >= configuration.radiusSquared()) {
            startDigTick = currentTick;
            if (entity instanceof NPCHolder) {
                NPC npc = ((NPCHolder) entity).getNPC();
                if (npc != null && !npc.getNavigator().isNavigating()) {
                    npc.getNavigator()
                            .setTarget(entity.level.getWorld().getBlockAt(x, y, z).getLocation().add(0, 1, 0));
                    setTarget = true;
                }
            }
            return BehaviorStatus.RUNNING;
        }
        Util.faceLocation(entity.getBukkitEntity(), location);
        if (entity instanceof ServerPlayer) {
            PlayerAnimation.ARM_SWING.play((Player) entity.getBukkitEntity());
        }
        BlockState block = entity.level.getBlockState(new BlockPos(x, y, z));
        if (block == null || block.getBlock() == Blocks.AIR) {
            return BehaviorStatus.SUCCESS;
        } else {
            int tickDifference = currentTick - startDigTick;
            float damage = getStrength(block) * (tickDifference + 1) * configuration.blockStrengthModifier();
            if (damage >= 1F) {
                entity.level.getWorld().getBlockAt(x, y, z)
                        .breakNaturally(CraftItemStack.asCraftMirror(getCurrentItem()));
                return BehaviorStatus.SUCCESS;
            }
            int modifiedDamage = (int) (damage * 10.0F);
            if (modifiedDamage != currentDamage) {
                setBlockDamage(modifiedDamage);
                currentDamage = modifiedDamage;
            }
        }
        return BehaviorStatus.RUNNING;
    }

    private void setBlockDamage(int modifiedDamage) {
        ((ServerLevel) entity.level).destroyBlockProgress(entity.getId(), new BlockPos(x, y, z), modifiedDamage);
        // TODO: check this works
    }

    @Override
    public boolean shouldExecute() {
        return entity.level.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.AIR;
    }

    private float strengthMod(BlockState block) {
        ItemStack itemstack = getCurrentItem();
        float f = itemstack.getDestroySpeed(block);
        if (entity instanceof LivingEntity) {
            LivingEntity handle = (LivingEntity) entity;
            if (f > 1.0F) {
                int i = EnchantmentHelper.getBlockEfficiency(handle);
                if (i > 0) {
                    f += i * i + 1;
                }
            }
            if (MobEffectUtil.hasDigSpeed(handle)) {
                f *= 1.0F + (MobEffectUtil.getDigSpeedAmplification(handle) + 1) * 0.2F;
            }
            if (handle.hasEffect(MobEffects.DIG_SLOWDOWN)) {
                float f1 = 1.0F;
                switch (handle.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) {
                    case 0:
                        f1 = 0.3F;
                        break;
                    case 1:
                        f1 = 0.09F;
                        break;
                    case 2:
                        f1 = 0.0027F;
                        break;
                    case 3:
                    default:
                        f1 = 8.1E-4F;
                }
                f *= f1;
            }

            if (handle.isEyeInFluid(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(handle)) {
                f /= 5.0F;
            }

        }
        if (!entity.isOnGround()) {
            f /= 5.0F;
        }
        return f;
    }
}