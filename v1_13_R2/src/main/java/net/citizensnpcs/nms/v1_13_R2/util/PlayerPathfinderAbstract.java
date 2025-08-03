package net.citizensnpcs.nms.v1_13_R2.util;

import net.citizensnpcs.nms.v1_13_R2.entity.EntityHumanNPC;
import net.minecraft.server.v1_13_R2.IBlockAccess;
import net.minecraft.server.v1_13_R2.IntHashMap;
import net.minecraft.server.v1_13_R2.MathHelper;
import net.minecraft.server.v1_13_R2.PathPoint;
import net.minecraft.server.v1_13_R2.PathfinderAbstract;

public abstract class PlayerPathfinderAbstract extends PathfinderAbstract {
    protected IBlockAccess a;
    protected EntityHumanNPC b;
    protected final IntHashMap<PathPoint> c = new IntHashMap<>();
    protected int d;
    protected int e;
    protected int f;
    protected boolean g;
    protected boolean h;
    protected boolean i;

    @Override
    public void a() {
        this.a = null;
        this.b = null;
    }

    @Override
    public void a(boolean paramBoolean) {
        this.g = paramBoolean;
    }

    public void a(IBlockAccess paramIBlockAccess, EntityHumanNPC paramEntityInsentient) {
        this.a = paramIBlockAccess;
        this.b = paramEntityInsentient;
        this.c.c();
        this.d = MathHelper.d(paramEntityInsentient.width + 1.0F);
        this.e = MathHelper.d(paramEntityInsentient.length + 1.0F);
        this.f = MathHelper.d(paramEntityInsentient.width + 1.0F);
    }

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
