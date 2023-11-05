package net.citizensnpcs.nms.v1_16_R3.util;

import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;

import net.citizensnpcs.util.AbstractBlockBreaker;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.EnchantmentManager;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityLiving;
import net.minecraft.server.v1_16_R3.EnumItemSlot;
import net.minecraft.server.v1_16_R3.IBlockData;
import net.minecraft.server.v1_16_R3.ItemStack;
import net.minecraft.server.v1_16_R3.MobEffects;
import net.minecraft.server.v1_16_R3.TagsFluid;
import net.minecraft.server.v1_16_R3.WorldServer;

public class CitizensBlockBreaker extends AbstractBlockBreaker {
    public CitizensBlockBreaker(org.bukkit.entity.Entity entity, org.bukkit.block.Block target,
            BlockBreakerConfiguration config) {
        super(entity, target, config);
    }

    private net.minecraft.server.v1_16_R3.ItemStack getCurrentItem() {
        return configuration.item() != null ? CraftItemStack.asNMSCopy(configuration.item())
                : getHandle() instanceof EntityLiving ? ((EntityLiving) getHandle()).getEquipment(EnumItemSlot.MAINHAND)
                        : null;
    }

    @Override
    protected float getDamage(int tickDifference) {
        return getStrength(getHandle().world.getType(new BlockPosition(x, y, z))) * (tickDifference + 1)
                * configuration.blockStrengthModifier();
    }

    private Entity getHandle() {
        return NMSImpl.getHandle(entity);
    }

    private float getStrength(IBlockData block) {
        float base = block.h(null, new BlockPosition(0, 0, 0));
        return base < 0.0F ? 0.0F : !isDestroyable(block) ? 1.0F / base / 100.0F : strengthMod(block) / base / 30.0F;
    }

    private boolean isDestroyable(IBlockData block) {
        if (block.isRequiresSpecialTool())
            return true;
        else {
            ItemStack current = getCurrentItem();
            return current != null ? current.canDestroySpecialBlock(block) : false;
        }
    }

    @Override
    protected void setBlockDamage(int modifiedDamage) {
        ((WorldServer) getHandle().world).a(getHandle().getId(), new BlockPosition(x, y, z), modifiedDamage);
    }

    private float strengthMod(IBlockData block) {
        ItemStack itemstack = getCurrentItem();
        float f = itemstack.a(block);
        if (getHandle() instanceof EntityLiving) {
            EntityLiving handle = (EntityLiving) getHandle();
            if (f > 1.0F) {
                int i = EnchantmentManager.getDigSpeedEnchantmentLevel(handle);
                if (i > 0) {
                    f += i * i + 1;
                }
            }
            if (handle.hasEffect(MobEffects.FASTER_DIG)) {
                f *= 1.0F + (handle.getEffect(MobEffects.FASTER_DIG).getAmplifier() + 1) * 0.2F;
            }
            if (handle.hasEffect(MobEffects.SLOWER_DIG)) {
                float f1 = 1.0F;
                switch (handle.getEffect(MobEffects.SLOWER_DIG).getAmplifier()) {
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
            if (handle.a(TagsFluid.WATER) && !EnchantmentManager.h(handle)) {
                f /= 5.0F;
            }
        }
        if (!getHandle().isOnGround()) {
            f /= 5.0F;
        }
        return f;
    }
}