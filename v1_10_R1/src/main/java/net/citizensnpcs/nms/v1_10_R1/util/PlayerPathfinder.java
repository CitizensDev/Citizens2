package net.citizensnpcs.nms.v1_10_R1.util;

import java.util.HashSet;
import java.util.Set;

import net.citizensnpcs.nms.v1_10_R1.entity.EntityHumanNPC;
import net.minecraft.server.v1_10_R1.BlockPosition;
import net.minecraft.server.v1_10_R1.Entity;
import net.minecraft.server.v1_10_R1.IBlockAccess;
import net.minecraft.server.v1_10_R1.Path;
import net.minecraft.server.v1_10_R1.PathEntity;
import net.minecraft.server.v1_10_R1.PathPoint;

public class PlayerPathfinder {
    private final Path a = new Path();
    private final Set<PathPoint> b = new HashSet<>();
    private final PathPoint[] c = new PathPoint[32];
    private final PlayerPathfinderNormal d;

    public PlayerPathfinder(PlayerPathfinderNormal paramPathfinderAbstract) {
        this.d = paramPathfinderAbstract;
    }

    public PathEntity a(IBlockAccess paramIBlockAccess, EntityHumanNPC paramEntityInsentient,
            BlockPosition paramBlockPosition, float paramFloat) {
        return a(paramIBlockAccess, paramEntityInsentient, paramBlockPosition.getX() + 0.5F,
                paramBlockPosition.getY() + 0.5F, paramBlockPosition.getZ() + 0.5F, paramFloat);
    }

    private PathEntity a(IBlockAccess paramIBlockAccess, EntityHumanNPC paramEntityInsentient, double paramDouble1,
            double paramDouble2, double paramDouble3, float paramFloat) {
        this.a.a();
        this.d.a(paramIBlockAccess, paramEntityInsentient);
        PathPoint localPathPoint1 = this.d.b();
        PathPoint localPathPoint2 = this.d.a(paramDouble1, paramDouble2, paramDouble3);
        PathEntity localPathEntity = a(localPathPoint1, localPathPoint2, paramFloat);
        this.d.a();
        return localPathEntity;
    }

    public PathEntity a(IBlockAccess paramIBlockAccess, EntityHumanNPC paramEntityInsentient, Entity paramEntity,
            float paramFloat) {
        return a(paramIBlockAccess, paramEntityInsentient, paramEntity.locX, paramEntity.getBoundingBox().b,
                paramEntity.locZ, paramFloat);
    }

    private PathEntity a(PathPoint paramPathPoint1, PathPoint paramPathPoint2) {
        int i = 1;
        PathPoint localPathPoint = paramPathPoint2;
        while (localPathPoint.h != null) {
            i++;
            localPathPoint = localPathPoint.h;
        }
        PathPoint[] arrayOfPathPoint = new PathPoint[i];
        localPathPoint = paramPathPoint2;
        arrayOfPathPoint[--i] = localPathPoint;
        while (localPathPoint.h != null) {
            localPathPoint = localPathPoint.h;
            arrayOfPathPoint[--i] = localPathPoint;
        }
        return new PathEntity(arrayOfPathPoint);
    }

    private PathEntity a(PathPoint paramPathPoint1, PathPoint paramPathPoint2, float paramFloat) {
        paramPathPoint1.e = 0.0F;
        paramPathPoint1.f = paramPathPoint1.c(paramPathPoint2);
        paramPathPoint1.g = paramPathPoint1.f;
        this.a.a();
        this.b.clear();
        this.a.a(paramPathPoint1);
        Object localObject1 = paramPathPoint1;
        int i = 0;
        while (!this.a.e()) {
            i++;
            if (i >= 2000) {
                break;
            }
            PathPoint localObject2 = this.a.c();
            if (localObject2.equals(paramPathPoint2)) {
                localObject1 = paramPathPoint2;
                break;
            }
            if (localObject2.c(paramPathPoint2) < ((PathPoint) localObject1).c(paramPathPoint2)) {
                localObject1 = localObject2;
            }
            localObject2.i = true;
            int j = this.d.a(this.c, localObject2, paramPathPoint2, paramFloat);
            for (int k = 0; k < j; k++) {
                PathPoint localPathPoint = this.c[k];
                float f1 = localObject2.c(localPathPoint);
                localPathPoint.j = localObject2.j + f1;
                localPathPoint.k = f1 + localPathPoint.l;
                float f2 = localObject2.e + localPathPoint.k;
                if (localPathPoint.j < paramFloat && (!localPathPoint.a() || f2 < localPathPoint.e)) {
                    localPathPoint.h = localObject2;
                    localPathPoint.e = f2;
                    localPathPoint.f = localPathPoint.c(paramPathPoint2) + localPathPoint.l;
                    if (localPathPoint.a()) {
                        this.a.a(localPathPoint, localPathPoint.e + localPathPoint.f);
                    } else {
                        localPathPoint.g = localPathPoint.e + localPathPoint.f;
                        this.a.a(localPathPoint);
                    }
                }
            }
        }
        if (localObject1 == paramPathPoint1)
            return null;
        Object localObject2 = a(paramPathPoint1, (PathPoint) localObject1);
        return (PathEntity) localObject2;
    }
}
