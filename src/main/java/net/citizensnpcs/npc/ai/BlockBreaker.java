package net.citizensnpcs.npc.ai;

import net.minecraft.server.v1_4_R1.Block;
import net.minecraft.server.v1_4_R1.Enchantment;
import net.minecraft.server.v1_4_R1.EnchantmentManager;
import net.minecraft.server.v1_4_R1.EntityHuman;
import net.minecraft.server.v1_4_R1.EntityLiving;
import net.minecraft.server.v1_4_R1.ItemStack;
import net.minecraft.server.v1_4_R1.Material;
import net.minecraft.server.v1_4_R1.MobEffectList;

public class BlockBreaker {
    private EntityLiving entity;
    private ItemStack in;
    private int lastDigTick, currentTick;

    public boolean destroyable(Block block) {
        if (block.material.isAlwaysDestroyable()) {
            return true;
        } else {
            return in != null ? in.b(block) : false;
        }
    }

    public void dig(int i, int j, int k, int l) {
        if (true) {
            entity.world.douseFire(null, i, j, k, l);
            lastDigTick = currentTick;
            float f = 1.0F;
            int i1 = entity.world.getTypeId(i, j, k);
            if (i1 > 0) {
                Block.byId[i1].attack(entity.world, i, j, k, null);
                // Allow fire punching to be blocked
                entity.world.douseFire((EntityHuman) null, i, j, k, l);
            }
            if (i1 > 0) {
                f = getStr(Block.byId[i1]);
            }
            EntityHuman m;
            if (i1 > 0 && f >= 1.0F) {
                // this.breakBlock(i, j, k);
            } else {
                // this.d = true;
                // this.f = i;
                // this.g = j;
                // this.h = k;
                int j1 = (int) (f * 10.0F);
                entity.world.g(entity.id, i, j, k, j1);
                // entity.o = j1;
            }
        }
    }

    public float getStr(Block block) {
        float base = block.m(null, 0, 0, 0);
        return base < 0.0F ? 0.0F : (!destroyable(block) ? 1.0F / base / 100.0F : strengthMod(block) / base / 30.0F);
    }

    public float strengthMod(Block block) {
        float f = in.a(block);
        int i = EnchantmentManager.getEnchantmentLevel(Enchantment.DURABILITY.id, in);
        ItemStack itemstack = in;

        if (i > 0 && itemstack != null) {
            float f1 = i * i + 1;

            if (!itemstack.b(block) && f <= 1.0F) {
                f += f1 * 0.08F;
            } else {
                f += f1;
            }
        }

        if (entity.hasEffect(MobEffectList.FASTER_DIG)) {
            f *= 1.0F + (entity.getEffect(MobEffectList.FASTER_DIG).getAmplifier() + 1) * 0.2F;
        }

        if (entity.hasEffect(MobEffectList.SLOWER_DIG)) {
            f *= 1.0F - (entity.getEffect(MobEffectList.SLOWER_DIG).getAmplifier() + 1) * 0.2F;
        }

        if (entity.a(Material.WATER) && !EnchantmentManager.hasWaterWorkerEnchantment(entity)) {
            f /= 5.0F;
        }

        if (!entity.onGround) {
            f /= 5.0F;
        }

        return f;
    }
}