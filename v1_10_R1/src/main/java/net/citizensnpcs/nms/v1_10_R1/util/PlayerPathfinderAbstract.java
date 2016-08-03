package net.citizensnpcs.nms.v1_10_R1.util;

import net.citizensnpcs.nms.v1_10_R1.entity.EntityHumanNPC;
import net.minecraft.server.v1_10_R1.EntityInsentient;
import net.minecraft.server.v1_10_R1.IBlockAccess;
import net.minecraft.server.v1_10_R1.MathHelper;
import net.minecraft.server.v1_10_R1.PathPoint;
import net.minecraft.server.v1_10_R1.PathType;
import net.minecraft.server.v1_10_R1.PathfinderAbstract;

public abstract class PlayerPathfinderAbstract extends PathfinderAbstract {
    protected IBlockAccess a;
    protected EntityHumanNPC b;
    protected int d;
    protected int e;
    protected int f;
    protected boolean g;
    protected boolean h;
    protected boolean i;

    @Override
    public void a() {
    }

    @Override
    public void a(boolean paramBoolean) {
        this.g = paramBoolean;
    }

    @Override
    public abstract PathPoint a(double paramDouble1, double paramDouble2, double paramDouble3);

    public void a(IBlockAccess paramIBlockAccess, EntityHumanNPC paramEntityInsentient) {
        this.a = paramIBlockAccess;
        this.b = paramEntityInsentient;
        this.c.c();

        this.d = MathHelper.d(paramEntityInsentient.width + 1.0F);
        this.e = MathHelper.d(paramEntityInsentient.length + 1.0F);
        this.f = MathHelper.d(paramEntityInsentient.width + 1.0F);
    }

    public abstract PathType a(IBlockAccess paramIBlockAccess, int paramInt1, int paramInt2, int paramInt3,
            EntityHumanNPC paramEntityInsentient, int paramInt4, int paramInt5, int paramInt6, boolean paramBoolean1,
            boolean paramBoolean2);

    @Override
    public abstract PathType a(IBlockAccess paramIBlockAccess, int paramInt1, int paramInt2, int paramInt3,
            EntityInsentient paramEntityInsentient, int paramInt4, int paramInt5, int paramInt6, boolean paramBoolean1,
            boolean paramBoolean2);

    @Override
    protected PathPoint a(int paramInt1, int paramInt2, int paramInt3) {
        int j = PathPoint.b(paramInt1, paramInt2, paramInt3);
        PathPoint localPathPoint = this.c.get(j);
        if (localPathPoint == null) {
            localPathPoint = new PathPoint(paramInt1, paramInt2, paramInt3);
            this.c.a(j, localPathPoint);
        }
        return localPathPoint;
    }

    @Override
    public abstract int a(PathPoint[] paramArrayOfPathPoint, PathPoint paramPathPoint1, PathPoint paramPathPoint2,
            float paramFloat);

    @Override
    public abstract PathPoint b();

    @Override
    public void b(boolean paramBoolean) {
        this.h = paramBoolean;
    }

    @Override
    public boolean c() {
        return this.g;
    }

    @Override
    public void c(boolean paramBoolean) {
        this.i = paramBoolean;
    }

    @Override
    public boolean d() {
        return this.h;
    }

    @Override
    public boolean e() {
        return this.i;
    }
}
