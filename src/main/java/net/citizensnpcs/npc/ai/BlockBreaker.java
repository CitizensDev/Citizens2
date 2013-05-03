package net.citizensnpcs.npc.ai;

import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.util.PlayerAnimation;
import net.minecraft.server.v1_5_R3.Block;
import net.minecraft.server.v1_5_R3.Enchantment;
import net.minecraft.server.v1_5_R3.EnchantmentManager;
import net.minecraft.server.v1_5_R3.EntityLiving;
import net.minecraft.server.v1_5_R3.EntityPlayer;
import net.minecraft.server.v1_5_R3.ItemStack;
import net.minecraft.server.v1_5_R3.Material;
import net.minecraft.server.v1_5_R3.MobEffectList;

import org.bukkit.craftbukkit.v1_5_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_5_R3.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class BlockBreaker extends BehaviorGoalAdapter {
    private final Configuration configuration;
    private int currentDamage;
    private int currentTick;
    private final EntityLiving entity;
    private boolean isDigging;
    private int startDigTick;
    private final int x, y, z;

    private BlockBreaker(LivingEntity entity, org.bukkit.block.Block target, Configuration config) {
        this.entity = ((CraftLivingEntity) entity).getHandle();
        this.x = target.getX();
        this.y = target.getY();
        this.z = target.getZ();
        this.startDigTick = (int) (System.currentTimeMillis() / 50);
        this.configuration = config;
    }

    private double distanceSquared() {
        return Math.pow(entity.locX - x, 2) + Math.pow(entity.locY - y, 2) + Math.pow(entity.locZ - z, 2);
    }

    private net.minecraft.server.v1_5_R3.ItemStack getCurrentItem() {
        return configuration.item() != null ? CraftItemStack.asNMSCopy(configuration.item()) : entity.getEquipment(0);
    }

    private float getStrength(Block block) {
        float base = block.l(null, 0, 0, 0);
        return base < 0.0F ? 0.0F : (!isDestroyable(block) ? 1.0F / base / 100.0F : strengthMod(block) / base / 30.0F);
    }

    private boolean isDestroyable(Block block) {
        if (block.material.isAlwaysDestroyable()) {
            return true;
        } else {
            ItemStack current = getCurrentItem();
            return current != null ? current.b(block) : false;
        }
    }

    public boolean isFinished() {
        return !isDigging;
    }

    @Override
    public void reset() {
        if (configuration.callback() != null) {
            configuration.callback().run();
        }
        isDigging = false;
        setBlockDamage(currentDamage = -1);
    }

    @Override
    public BehaviorStatus run() {
        if (!isDigging) {
            reset();
            return BehaviorStatus.SUCCESS;
        }
        currentTick = (int) (System.currentTimeMillis() / 50); // CraftBukkit
        if (configuration.radiusSquared() > 0 && distanceSquared() >= configuration.radiusSquared()) {
            startDigTick = currentTick;
            return BehaviorStatus.RUNNING;
        }
        if (entity instanceof EntityPlayer)
            PlayerAnimation.ARM_SWING.play((Player) entity.getBukkitEntity());
        Block block = Block.byId[entity.world.getTypeId(x, y, z)];
        if (block == null) {
            return BehaviorStatus.SUCCESS;
        } else {
            int tickDifference = currentTick - startDigTick;
            float damage = getStrength(block) * (tickDifference + 1);
            if (damage >= 1F) {
                entity.world.getWorld().getBlockAt(x, y, z)
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
        entity.world.f(entity.id, x, y, z, modifiedDamage);
    }

    @Override
    public boolean shouldExecute() {
        return org.bukkit.Material.getMaterial(entity.world.getTypeId(x, y, z)) != null;
    }

    private float strengthMod(Block block) {
        ItemStack itemstack = getCurrentItem();
        float strength = itemstack != null ? itemstack.a(block) : 1;
        int ench = EnchantmentManager.getEnchantmentLevel(Enchantment.DURABILITY.id, getCurrentItem());

        if (ench > 0 && itemstack != null) {
            float levelSquared = ench * ench + 1;

            if (!itemstack.b(block) && strength <= 1.0F) {
                strength += levelSquared * 0.08F;
            } else {
                strength += levelSquared;
            }
        }
        if (entity.hasEffect(MobEffectList.FASTER_DIG)) {
            strength *= 1.0F + (entity.getEffect(MobEffectList.FASTER_DIG).getAmplifier() + 1) * 0.2F;
        }
        if (entity.hasEffect(MobEffectList.SLOWER_DIG)) {
            strength *= 1.0F - (entity.getEffect(MobEffectList.SLOWER_DIG).getAmplifier() + 1) * 0.2F;
        }
        if (entity.a(Material.WATER) && !EnchantmentManager.hasWaterWorkerEnchantment(entity)) {
            strength /= 5.0F;
        }
        if (!entity.onGround) {
            strength /= 5.0F;
        }
        return strength;
    }

    public static class Configuration {
        private Runnable callback;
        private org.bukkit.inventory.ItemStack itemStack;
        private double radius;

        private Runnable callback() {
            return callback;
        }

        public Configuration callback(Runnable callback) {
            this.callback = callback;
            return this;
        }

        private org.bukkit.inventory.ItemStack item() {
            return itemStack;
        }

        public Configuration item(org.bukkit.inventory.ItemStack stack) {
            itemStack = stack;
            return this;
        }

        public Configuration radius(double radius) {
            this.radius = radius;
            return this;
        }

        private double radiusSquared() {
            return Math.pow(radius, 2);
        }
    }

    private static final Configuration EMPTY = new Configuration();

    public static BlockBreaker create(LivingEntity entity, org.bukkit.block.Block target) {
        return createWithConfiguration(entity, target, EMPTY);
    }

    public static BlockBreaker createWithConfiguration(LivingEntity entity, org.bukkit.block.Block target,
            Configuration config) {
        return new BlockBreaker(entity, target, config);
    }
}