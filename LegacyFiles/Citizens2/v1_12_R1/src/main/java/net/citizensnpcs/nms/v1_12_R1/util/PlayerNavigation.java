package net.citizensnpcs.nms.v1_12_R1.util;

import net.citizensnpcs.nms.v1_12_R1.entity.EntityHumanNPC;
import net.minecraft.server.v1_12_R1.AttributeInstance;
import net.minecraft.server.v1_12_R1.AxisAlignedBB;
import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.Blocks;
import net.minecraft.server.v1_12_R1.ChunkCache;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityInsentient;
import net.minecraft.server.v1_12_R1.GenericAttributes;
import net.minecraft.server.v1_12_R1.Material;
import net.minecraft.server.v1_12_R1.MathHelper;
import net.minecraft.server.v1_12_R1.NavigationAbstract;
import net.minecraft.server.v1_12_R1.PathEntity;
import net.minecraft.server.v1_12_R1.PathPoint;
import net.minecraft.server.v1_12_R1.PathType;
import net.minecraft.server.v1_12_R1.Pathfinder;
import net.minecraft.server.v1_12_R1.PathfinderAbstract;
import net.minecraft.server.v1_12_R1.Vec3D;
import net.minecraft.server.v1_12_R1.World;

public class PlayerNavigation extends NavigationAbstract {
    protected EntityHumanNPC a;
    protected World b;
    protected PathEntity c;
    protected double d;
    protected int e;
    protected float f = 0.5F;
    protected boolean g;
    protected PlayerPathfinderNormal h;
    private final AttributeInstance i;
    private boolean ii;
    private int j;
    private Vec3D k = Vec3D.a;
    private Vec3D l = Vec3D.a;
    private long m;
    private long n;
    private double o;
    private long p;
    private BlockPosition q;
    private final PlayerPathfinder r;

    public PlayerNavigation(EntityHumanNPC entityinsentient, World world) {
        super(getDummyInsentient(entityinsentient, world), world);
        this.a = entityinsentient;
        this.b = world;
        this.i = entityinsentient.getAttributeInstance(GenericAttributes.FOLLOW_RANGE);
        this.setRange(24);
        this.h = new PlayerPathfinderNormal();
        this.h.a(true);
        this.r = new PlayerPathfinder(this.h);
        // this.b.C().a(this);
    }

    @Override
    protected Pathfinder a() {
        return null;
    }

    @Override
    public boolean a(BlockPosition paramBlockPosition) {
        return this.b.getType(paramBlockPosition.down()).b();
    }

    public void a(boolean paramBoolean) {
        this.h.b(paramBoolean);
    }

    @Override
    public void a(double paramDouble) {
        this.d = paramDouble;
    }

    @Override
    public boolean a(double paramDouble1, double paramDouble2, double paramDouble3, double paramDouble4) {
        return a(a(paramDouble1, paramDouble2, paramDouble3), paramDouble4);
    }

    @Override
    public PathEntity a(Entity paramEntity) {
        return b(new BlockPosition(paramEntity));
    }

    @Override
    public boolean a(Entity paramEntity, double paramDouble) {
        PathEntity localPathEntity = a(paramEntity);
        return localPathEntity != null && a(localPathEntity, paramDouble);
    }

    private boolean a(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6,
            Vec3D paramVec3D, double paramDouble1, double paramDouble2) {
        int j = paramInt1 - paramInt4 / 2;
        int k = paramInt3 - paramInt6 / 2;
        if (!b(j, paramInt2, k, paramInt4, paramInt5, paramInt6, paramVec3D, paramDouble1, paramDouble2))
            return false;
        for (int m = j; m < j + paramInt4; m++) {
            for (int n = k; n < k + paramInt6; n++) {
                double d1 = m + 0.5D - paramVec3D.x;
                double d2 = n + 0.5D - paramVec3D.z;
                if (d1 * paramDouble1 + d2 * paramDouble2 >= 0.0D) {
                    PathType localPathType = this.h.a(this.b, m, paramInt2 - 1, n, this.a, paramInt4, paramInt5,
                            paramInt6, true, true);
                    if (localPathType == PathType.WATER || localPathType == PathType.LAVA
                            || localPathType == PathType.OPEN)
                        return false;
                    localPathType = this.h.a(this.b, m, paramInt2, n, this.a, paramInt4, paramInt5, paramInt6, true,
                            true);
                    float f = this.a.a(localPathType);
                    if (f < 0.0F || f >= 8.0F)
                        return false;
                    if (localPathType == PathType.DAMAGE_FIRE || localPathType == PathType.DANGER_FIRE
                            || localPathType == PathType.DAMAGE_OTHER)
                        return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean a(PathEntity paramPathEntity, double paramDouble) {
        if (paramPathEntity == null) {
            this.c = null;
            return false;
        }
        if (!paramPathEntity.a(this.c)) {
            this.c = paramPathEntity;
        }
        q_();
        if (this.c.d() <= 0)
            return false;
        this.d = paramDouble;
        Vec3D localVec3D = c();
        this.j = this.e;
        this.k = localVec3D;
        return true;
    }

    @Override
    protected void a(Vec3D paramVec3D) {
        if (this.e - this.j > 100) {
            if (paramVec3D.distanceSquared(this.k) < 2.25D) {
                p();
            }
            this.j = this.e;
            this.k = paramVec3D;
        }
        if (this.c != null && !this.c.b()) {
            Vec3D localVec3D = this.c.f();
            if (localVec3D.equals(this.l)) {
                this.m += System.currentTimeMillis() - this.n;
            } else {
                this.l = localVec3D;
                double d1 = paramVec3D.f(this.l);
                this.o = this.a.cy() > 0.0F ? d1 / this.a.cy() * 1000.0D : 0.0D;
            }
            if (this.o > 0.0D && this.m > this.o * 3.0D) {
                this.l = Vec3D.a;
                this.m = 0L;
                this.o = 0.0D;
                p();
            }
            this.n = System.currentTimeMillis();
        }
    }

    @Override
    protected boolean a(Vec3D paramVec3D1, Vec3D paramVec3D2, int paramInt1, int paramInt2, int paramInt3) {
        int j = MathHelper.floor(paramVec3D1.x);
        int k = MathHelper.floor(paramVec3D1.z);
        double d1 = paramVec3D2.x - paramVec3D1.x;
        double d2 = paramVec3D2.z - paramVec3D1.z;
        double d3 = d1 * d1 + d2 * d2;
        if (d3 < 1.0E-8D)
            return false;
        double d4 = 1.0D / Math.sqrt(d3);
        d1 *= d4;
        d2 *= d4;
        paramInt1 += 2;
        paramInt3 += 2;
        if (!a(j, (int) paramVec3D1.y, k, paramInt1, paramInt2, paramInt3, paramVec3D1, d1, d2))
            return false;
        paramInt1 -= 2;
        paramInt3 -= 2;
        double d5 = 1.0D / Math.abs(d1);
        double d6 = 1.0D / Math.abs(d2);
        double d7 = j - paramVec3D1.x;
        double d8 = k - paramVec3D1.z;
        if (d1 >= 0.0D) {
            d7 += 1.0D;
        }
        if (d2 >= 0.0D) {
            d8 += 1.0D;
        }
        d7 /= d1;
        d8 /= d2;
        int m = d1 < 0.0D ? -1 : 1;
        int n = d2 < 0.0D ? -1 : 1;
        int i1 = MathHelper.floor(paramVec3D2.x);
        int i2 = MathHelper.floor(paramVec3D2.z);
        int i3 = i1 - j;
        int i4 = i2 - k;
        while (i3 * m > 0 || i4 * n > 0) {
            if (d7 < d8) {
                d7 += d5;
                j += m;
                i3 = i1 - j;
            } else {
                d8 += d6;
                k += n;
                i4 = i2 - k;
            }
            if (!a(j, (int) paramVec3D1.y, k, paramInt1, paramInt2, paramInt3, paramVec3D1, d1, d2))
                return false;
        }
        return true;
    }

    @Override
    protected boolean b() {
        return this.a.onGround || h() && q() || this.a.isPassenger();
    }

    @Override
    public PathEntity b(BlockPosition paramBlockPosition) {
        BlockPosition localBlockPosition;
        if (this.b.getType(paramBlockPosition).getMaterial() == Material.AIR) {
            localBlockPosition = paramBlockPosition.down();
            while (localBlockPosition.getY() > 0 && this.b.getType(localBlockPosition).getMaterial() == Material.AIR) {
                localBlockPosition = localBlockPosition.down();
            }
            if (localBlockPosition.getY() > 0)
                return b2(localBlockPosition.up());
            while (localBlockPosition.getY() < this.b.getHeight()
                    && this.b.getType(localBlockPosition).getMaterial() == Material.AIR) {
                localBlockPosition = localBlockPosition.up();
            }
            paramBlockPosition = localBlockPosition;
        }
        if (this.b.getType(paramBlockPosition).getMaterial().isBuildable()) {
            localBlockPosition = paramBlockPosition.up();
            while (localBlockPosition.getY() < this.b.getHeight()
                    && this.b.getType(localBlockPosition).getMaterial().isBuildable()) {
                localBlockPosition = localBlockPosition.up();
            }
            return b2(localBlockPosition);
        }
        return b2(paramBlockPosition);
    }

    public void b(boolean paramBoolean) {
        this.h.a(paramBoolean);
    }

    private boolean b(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6,
            Vec3D paramVec3D, double paramDouble1, double paramDouble2) {
        for (BlockPosition localBlockPosition : BlockPosition.a(new BlockPosition(paramInt1, paramInt2, paramInt3),
                new BlockPosition(paramInt1 + paramInt4 - 1, paramInt2 + paramInt5 - 1, paramInt3 + paramInt6 - 1))) {
            double d1 = localBlockPosition.getX() + 0.5D - paramVec3D.x;
            double d2 = localBlockPosition.getZ() + 0.5D - paramVec3D.z;
            if (d1 * paramDouble1 + d2 * paramDouble2 >= 0.0D) {
                Block localBlock = this.b.getType(localBlockPosition).getBlock();
                if (!localBlock.b(this.b, localBlockPosition))
                    return false;
            }
        }
        return true;
    }

    private PathEntity b2(BlockPosition paramBlockPosition) {
        if (!b())
            return null;
        if (this.c != null && !this.c.b() && paramBlockPosition.equals(this.q))
            return this.c;
        this.q = paramBlockPosition;
        float f1 = i();
        this.b.methodProfiler.a("pathfind");
        BlockPosition localBlockPosition = new BlockPosition(this.a);
        int i1 = (int) (f1 + 8.0F);
        ChunkCache localChunkCache = new ChunkCache(this.b, localBlockPosition.a(-i1, -i1, -i1),
                localBlockPosition.a(i1, i1, i1), 0);
        PathEntity localPathEntity = this.r.a(localChunkCache, this.a, this.q, f1);
        this.b.methodProfiler.b();
        return localPathEntity;
    }

    @Override
    protected Vec3D c() {
        return new Vec3D(this.a.locX, s(), this.a.locZ);
    }

    public void c(boolean paramBoolean) {
        this.h.c(paramBoolean);
    }

    @Override
    public void d() {
        this.e += 1;
        if (this.g) {
            k();
        }
        if (o())
            return;
        if (b()) {
            n();
        } else if (this.c != null && this.c.e() < this.c.d()) {
            Vec3D localVec3D = c();
            Vec3D localObject = this.c.a(this.a, this.c.e());
            if (localVec3D.y > localObject.y && !this.a.onGround
                    && MathHelper.floor(localVec3D.x) == MathHelper.floor(localObject.x)
                    && MathHelper.floor(localVec3D.z) == MathHelper.floor(localObject.z)) {
                this.c.c(this.c.e() + 1);
            }
        }
        m();
        if (o())
            return;
        Vec3D localVec3D = this.c.a(this.a);
        Object localObject = new BlockPosition(localVec3D).down();
        AxisAlignedBB localAxisAlignedBB = this.b.getType((BlockPosition) localObject).e(this.b,
                (BlockPosition) localObject);
        localVec3D = localVec3D.a(0.0D, 1.0D - localAxisAlignedBB.e, 0.0D);
        this.a.getControllerMove().a(localVec3D.x, localVec3D.y, localVec3D.z, this.d);
    }

    public void d(boolean paramBoolean) {
        this.ii = paramBoolean;
    }

    public boolean g() {
        return this.h.c();
    }

    public boolean h() {
        return this.h.e();
    }

    @Override
    public float i() {
        return (float) this.i.getValue();
    }

    @Override
    public boolean j() {
        return this.g;
    }

    @Override
    public void k() {
        if (this.b.getTime() - this.p > 20L) {
            if (this.q != null) {
                this.c = null;
                this.c = b(this.q);
                this.p = this.b.getTime();
                this.g = false;
            }
        } else {
            this.g = true;
        }
    }

    @Override
    public PathEntity l() {
        return this.c;
    }

    @Override
    protected void m() {
    }

    @Override
    protected void n() {
        Vec3D localVec3D1 = c();
        int i1 = this.c.d();
        for (int i2 = this.c.e(); i2 < this.c.d(); i2++) {
            if (this.c.a(i2).b != Math.floor(localVec3D1.y)) {
                i1 = i2;
                break;
            }
        }
        this.f = this.a.width > 0.75F ? this.a.width / 2.0F : 0.75F - this.a.width / 2.0F;
        Vec3D localVec3D2 = this.c.f();
        if (MathHelper.e((float) (this.a.locX - (localVec3D2.x + 0.5D))) < this.f
                && MathHelper.e((float) (this.a.locZ - (localVec3D2.z + 0.5D))) < this.f
                && Math.abs(this.a.locY - localVec3D2.y) < 1.0D) {
            this.c.c(this.c.e() + 1);
        }
        int i3 = MathHelper.f(this.a.width);
        int i4 = MathHelper.f(this.a.length);
        int i5 = i3;
        for (int i6 = i1 - 1; i6 >= this.c.e(); i6--) {
            if (a(localVec3D1, this.c.a(this.a, i6), i3, i4, i5)) {
                this.c.c(i6);
                break;
            }
        }
        a(localVec3D1);
    }

    @Override
    public boolean o() {
        return this.c == null || this.c.b();
    }

    @Override
    public void p() {
        this.c = null;
    }

    @Override
    protected boolean q() {
        return this.a.isInWater() || this.a.au();
    }

    @Override
    protected void q_() {
        super.q_();
        if (this.ii) {
            if (this.b.h(new BlockPosition(MathHelper.floor(this.a.locX), (int) (this.a.getBoundingBox().b + 0.5D),
                    MathHelper.floor(this.a.locZ))))
                return;
            for (int j = 0; j < this.c.d(); j++) {
                PathPoint localPathPoint = this.c.a(j);
                if (this.b.h(new BlockPosition(localPathPoint.a, localPathPoint.b, localPathPoint.c))) {
                    this.c.b(j - 1);
                    return;
                }
            }
        }
    }

    @Override
    public PathfinderAbstract r() {
        return this.h;
    }

    private int s() {
        if (!this.a.isInWater() || !h())
            return (int) (this.a.getBoundingBox().b + 0.5D);
        int j = (int) this.a.getBoundingBox().b;
        Block localBlock = this.b
                .getType(new BlockPosition(MathHelper.floor(this.a.locX), j, MathHelper.floor(this.a.locZ))).getBlock();
        int k = 0;
        while (localBlock == Blocks.FLOWING_WATER || localBlock == Blocks.WATER) {
            j++;
            localBlock = this.b
                    .getType(new BlockPosition(MathHelper.floor(this.a.locX), j, MathHelper.floor(this.a.locZ)))
                    .getBlock();
            k++;
            if (k > 16)
                return (int) this.a.getBoundingBox().b;
        }
        return j;
    }

    public void setRange(float pathfindingRange) {
        this.i.setValue(pathfindingRange);
    }

    private static EntityInsentient getDummyInsentient(EntityHumanNPC from, World world) {
        return new EntityInsentient(world) {
        };
    }
}