package net.citizensnpcs.util.nms;

import net.citizensnpcs.npc.entity.EntityHumanNPC;
import net.minecraft.server.v1_9_R1.EntityInsentient;
import net.minecraft.server.v1_9_R1.IBlockAccess;
import net.minecraft.server.v1_9_R1.IntHashMap;
import net.minecraft.server.v1_9_R1.MathHelper;
import net.minecraft.server.v1_9_R1.PathPoint;
import net.minecraft.server.v1_9_R1.PathType;
import net.minecraft.server.v1_9_R1.PathfinderAbstract;

public abstract class PlayerPathfinderAbstract extends PathfinderAbstract {
    protected IBlockAccess a;
    protected EntityHumanNPC b;
    protected final IntHashMap<PathPoint> c = new IntHashMap();
    protected int d;
    protected int e;
    protected int f;
    protected boolean g;
    protected boolean h;
    protected boolean i;

    public void a() {
    }

    public void a(boolean paramBoolean) {
        this.g = paramBoolean;
    }

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

    public abstract PathType a(IBlockAccess paramIBlockAccess, int paramInt1, int paramInt2, int paramInt3,
            EntityInsentient paramEntityInsentient, int paramInt4, int paramInt5, int paramInt6, boolean paramBoolean1,
            boolean paramBoolean2);

    protected PathPoint a(int paramInt1, int paramInt2, int paramInt3) {
        int j = PathPoint.b(paramInt1, paramInt2, paramInt3);
        PathPoint localPathPoint = this.c.get(j);
        if (localPathPoint == null) {
            localPathPoint = new PathPoint(paramInt1, paramInt2, paramInt3);
            this.c.a(j, localPathPoint);
        }
        return localPathPoint;
    }

    public abstract int a(PathPoint[] paramArrayOfPathPoint, PathPoint paramPathPoint1, PathPoint paramPathPoint2,
            float paramFloat);

    public abstract PathPoint b();

    public void b(boolean paramBoolean) {
        this.h = paramBoolean;
    }

    public boolean c() {
        return this.g;
    }

    public void c(boolean paramBoolean) {
        this.i = paramBoolean;
    }

    public boolean d() {
        return this.h;
    }

    public boolean e() {
        return this.i;
    }
}
