package net.citizensnpcs.util.nms;

import net.minecraft.server.Block;
import net.minecraft.server.MathHelper;
import net.minecraft.server.v1_6_R3.EntityLiving;

import org.bukkit.entity.EntityType;

public class FlyingUtil {
    public static boolean isAlwaysFlyable(EntityType type) {
        switch (type) {
            case BAT:
            case BLAZE:
            case GHAST:
            case ENDER_DRAGON:
            case WITHER:
                return true;
            default:
                return false;
        }
    }

    public static void moveLogic(EntityLiving entity, float f, float f1) {
        if (entity.G()) {
            entity.a(f, f1, 0.02F);
            entity.move(entity.motX, entity.motY, entity.motZ);
            entity.motX *= 0.800000011920929D;
            entity.motY *= 0.800000011920929D;
            entity.motZ *= 0.800000011920929D;
        } else if (entity.I()) {
            entity.a(f, f1, 0.02F);
            entity.move(entity.motX, entity.motY, entity.motZ);
            entity.motX *= 0.5D;
            entity.motY *= 0.5D;
            entity.motZ *= 0.5D;
        } else {
            float f2 = 0.91F;

            if (entity.onGround) {
                f2 = 0.54600006F;
                int i = entity.world.getTypeId(MathHelper.floor(entity.locX),
                        MathHelper.floor(entity.boundingBox.b) - 1, MathHelper.floor(entity.locZ));

                if (i > 0) {
                    f2 = Block.byId[i].frictionFactor * 0.91F;
                }
            }

            float f3 = 0.16277136F / (f2 * f2 * f2);

            entity.a(f, f1, entity.onGround ? 0.1F * f3 : 0.02F);
            f2 = 0.91F;
            if (entity.onGround) {
                f2 = 0.54600006F;
                int j = entity.world.getTypeId(MathHelper.floor(entity.locX),
                        MathHelper.floor(entity.boundingBox.b) - 1, MathHelper.floor(entity.locZ));

                if (j > 0) {
                    f2 = Block.byId[j].frictionFactor * 0.91F;
                }
            }

            entity.move(entity.motX, entity.motY, entity.motZ);
            entity.motX *= f2;
            entity.motY *= f2;
            entity.motZ *= f2;
        }

        entity.aF = entity.aG;
        double d0 = entity.locX - entity.lastX;
        double d1 = entity.locZ - entity.lastZ;
        float f4 = MathHelper.sqrt(d0 * d0 + d1 * d1) * 4.0F;

        if (f4 > 1.0F) {
            f4 = 1.0F;
        }

        entity.aG += (f4 - entity.aG) * 0.4F;
        entity.aH += entity.aG;
    }
}
