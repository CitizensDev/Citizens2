package net.citizensnpcs.nms.v1_8_R3.util;

import java.util.List;

import net.citizensnpcs.nms.v1_8_R3.entity.EntityHumanNPC;
import net.minecraft.server.v1_8_R3.AttributeInstance;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Blocks;
import net.minecraft.server.v1_8_R3.ChunkCache;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.GenericAttributes;
import net.minecraft.server.v1_8_R3.Material;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.NavigationAbstract;
import net.minecraft.server.v1_8_R3.PathEntity;
import net.minecraft.server.v1_8_R3.PathPoint;
import net.minecraft.server.v1_8_R3.Pathfinder;
import net.minecraft.server.v1_8_R3.Vec3D;
import net.minecraft.server.v1_8_R3.World;

public class PlayerNavigation extends NavigationAbstract {
    private final AttributeInstance a;
    protected EntityHumanNPC b;
    protected World c;
    protected PathEntity d;
    protected double e;
    private int f;
    private boolean fb;
    private int g;
    private Vec3D h = new Vec3D(0.0D, 0.0D, 0.0D);
    private float i = 1.0F;
    private final PlayerPathfinder j;
    private final PlayerPathfinderNormal s;

    public PlayerNavigation(EntityHumanNPC entityinsentient, World world) {
        super(getDummyInsentient(entityinsentient, world), world);
        this.b = entityinsentient;
        this.c = world;
        this.a = entityinsentient.getAttributeInstance(GenericAttributes.FOLLOW_RANGE);
        this.a.setValue(24);
        this.s = new PlayerPathfinderNormal();
        this.s.a(true);
        this.j = new PlayerPathfinder(this.s);
        // this.b.C().a(this);
    }

    @Override
    protected Pathfinder a() {
        return null;
    }

    @Override
    public PathEntity a(BlockPosition paramBlockPosition) {
        if (!b())
            return null;
        float f1 = i();
        this.c.methodProfiler.a("pathfind");
        BlockPosition localBlockPosition = new BlockPosition(this.b);
        int k = (int) (f1 + 8.0F);
        ChunkCache localChunkCache = new ChunkCache(this.c, localBlockPosition.a(-k, -k, -k),
                localBlockPosition.a(k, k, k), 0);
        PathEntity localPathEntity = this.j.a(localChunkCache, this.b, paramBlockPosition, f1);
        this.c.methodProfiler.b();
        return localPathEntity;
    }

    public void a(boolean paramBoolean) {
        this.s.c(paramBoolean);
    }

    @Override
    public void a(double paramDouble) {
        this.e = paramDouble;
    }

    @Override
    public boolean a(double paramDouble1, double paramDouble2, double paramDouble3, double paramDouble4) {
        PathEntity localPathEntity = a(MathHelper.floor(paramDouble1), (int) paramDouble2,
                MathHelper.floor(paramDouble3));
        return a(localPathEntity, paramDouble4);
    }

    @Override
    public PathEntity a(Entity paramEntity) {
        if (!b())
            return null;
        float f1 = i();
        this.c.methodProfiler.a("pathfind");
        BlockPosition localBlockPosition = new BlockPosition(this.b).up();
        int k = (int) (f1 + 16.0F);
        ChunkCache localChunkCache = new ChunkCache(this.c, localBlockPosition.a(-k, -k, -k),
                localBlockPosition.a(k, k, k), 0);
        PathEntity localPathEntity = this.j.a(localChunkCache, this.b, paramEntity, f1);
        this.c.methodProfiler.b();
        return localPathEntity;
    }

    @Override
    public boolean a(Entity paramEntity, double paramDouble) {
        PathEntity localPathEntity = a(paramEntity);
        if (localPathEntity != null)
            return a(localPathEntity, paramDouble);
        return false;
    }

    @Override
    public void a(float paramFloat) {
        this.i = paramFloat;
    }

    private boolean a(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6,
            Vec3D paramVec3D, double paramDouble1, double paramDouble2) {
        int i = paramInt1 - paramInt4 / 2;
        int j = paramInt3 - paramInt6 / 2;
        if (!b(i, paramInt2, j, paramInt4, paramInt5, paramInt6, paramVec3D, paramDouble1, paramDouble2))
            return false;
        for (int k = i; k < i + paramInt4; k++) {
            for (int m = j; m < j + paramInt6; m++) {
                double d1 = k + 0.5D - paramVec3D.a;
                double d2 = m + 0.5D - paramVec3D.c;
                if (d1 * paramDouble1 + d2 * paramDouble2 >= 0.0D) {
                    Block localBlock = this.c.getType(new BlockPosition(k, paramInt2 - 1, m)).getBlock();
                    Material localMaterial = localBlock.getMaterial();
                    if (localMaterial == Material.AIR || localMaterial == Material.WATER && !this.b.V()
                            || localMaterial == Material.LAVA)
                        return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean a(PathEntity paramPathEntity, double paramDouble) {
        if (paramPathEntity == null) {
            this.d = null;
            return false;
        }
        if (!paramPathEntity.a(this.d)) {
            this.d = paramPathEntity;
        }
        d();
        if (this.d.d() == 0)
            return false;
        this.e = paramDouble;
        Vec3D localVec3D = c();
        this.g = this.f;
        this.h = localVec3D;
        return true;
    }

    @Override
    protected void a(Vec3D paramVec3D) {
        if (this.f - this.g > 100) {
            if (paramVec3D.distanceSquared(this.h) < 2.25D) {
                n();
            }
            this.g = this.f;
            this.h = paramVec3D;
        }
    }

    @Override
    protected boolean a(Vec3D paramVec3D1, Vec3D paramVec3D2, int paramInt1, int paramInt2, int paramInt3) {
        int i = MathHelper.floor(paramVec3D1.a);
        int j = MathHelper.floor(paramVec3D1.c);
        double d1 = paramVec3D2.a - paramVec3D1.a;
        double d2 = paramVec3D2.c - paramVec3D1.c;
        double d3 = d1 * d1 + d2 * d2;
        if (d3 < 1.0E-8D)
            return false;
        double d4 = 1.0D / Math.sqrt(d3);
        d1 *= d4;
        d2 *= d4;
        paramInt1 += 2;
        paramInt3 += 2;
        if (!a(i, (int) paramVec3D1.b, j, paramInt1, paramInt2, paramInt3, paramVec3D1, d1, d2))
            return false;
        paramInt1 -= 2;
        paramInt3 -= 2;
        double d5 = 1.0D / Math.abs(d1);
        double d6 = 1.0D / Math.abs(d2);
        double d7 = i * 1 - paramVec3D1.a;
        double d8 = j * 1 - paramVec3D1.c;
        if (d1 >= 0.0D) {
            d7 += 1.0D;
        }
        if (d2 >= 0.0D) {
            d8 += 1.0D;
        }
        d7 /= d1;
        d8 /= d2;
        int k = d1 < 0.0D ? -1 : 1;
        int m = d2 < 0.0D ? -1 : 1;
        int n = MathHelper.floor(paramVec3D2.a);
        int i1 = MathHelper.floor(paramVec3D2.c);
        int i2 = n - i;
        int i3 = i1 - j;
        while (i2 * k > 0 || i3 * m > 0) {
            if (d7 < d8) {
                d7 += d5;
                i += k;
                i2 = n - i;
            } else {
                d8 += d6;
                j += m;
                i3 = i1 - j;
            }
            if (!a(i, (int) paramVec3D1.b, j, paramInt1, paramInt2, paramInt3, paramVec3D1, d1, d2))
                return false;
        }
        return true;
    }

    @Override
    protected boolean b() {
        return this.b.onGround || h() && o() || this.b.au();
    }

    public void b(boolean paramBoolean) {
        this.s.b(paramBoolean);
    }

    private boolean b(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6,
            Vec3D paramVec3D, double paramDouble1, double paramDouble2) {
        for (BlockPosition localBlockPosition : BlockPosition.a(new BlockPosition(paramInt1, paramInt2, paramInt3),
                new BlockPosition(paramInt1 + paramInt4 - 1, paramInt2 + paramInt5 - 1, paramInt3 + paramInt6 - 1))) {
            double d1 = localBlockPosition.getX() + 0.5D - paramVec3D.a;
            double d2 = localBlockPosition.getZ() + 0.5D - paramVec3D.c;
            if (d1 * paramDouble1 + d2 * paramDouble2 >= 0.0D) {
                Block localBlock = this.c.getType(localBlockPosition).getBlock();
                if (!localBlock.b(this.c, localBlockPosition))
                    return false;
            }
        }
        return true;
    }

    @Override
    protected Vec3D c() {
        return new Vec3D(this.b.locX, p(), this.b.locZ);
    }

    public void c(boolean paramBoolean) {
        this.s.a(paramBoolean);
    }

    @Override
    protected void d() {
        super.d();
        if (this.fb) {
            if (this.c.i(new BlockPosition(MathHelper.floor(this.b.locX), (int) (this.b.getBoundingBox().b + 0.5D),
                    MathHelper.floor(this.b.locZ))))
                return;
            for (int i = 0; i < this.d.d(); i++) {
                PathPoint localPathPoint = this.d.a(i);
                if (this.c.i(new BlockPosition(localPathPoint.a, localPathPoint.b, localPathPoint.c))) {
                    this.d.b(i - 1);
                    return;
                }
            }
        }
    }

    public void d(boolean paramBoolean) {
        this.s.d(paramBoolean);
    }

    public boolean e() {
        return this.s.e();
    }

    public void e(boolean paramBoolean) {
        this.fb = paramBoolean;
    }

    public boolean g() {
        return this.s.b();
    }

    public boolean h() {
        return this.s.d();
    }

    @Override
    public float i() {
        return (float) this.a.getValue();
    }

    @Override
    public PathEntity j() {
        return this.d;
    }

    @Override
    public void k() {
        this.f += 1;
        if (m())
            return;
        if (b()) {
            l();
        } else if (this.d != null && this.d.e() < this.d.d()) {
            Vec3D localVec3D = c();
            Vec3D localObject = this.d.a(this.b, this.d.e());
            if (localVec3D.b > localObject.b && !this.b.onGround
                    && MathHelper.floor(localVec3D.a) == MathHelper.floor(localObject.a)
                    && MathHelper.floor(localVec3D.c) == MathHelper.floor(localObject.c)) {
                this.d.c(this.d.e() + 1);
            }
        }
        if (m())
            return;
        Vec3D localVec3D = this.d.a(this.b);
        if (localVec3D == null)
            return;
        Object localObject = new AxisAlignedBB(localVec3D.a, localVec3D.b, localVec3D.c, localVec3D.a, localVec3D.b,
                localVec3D.c).grow(0.5D, 0.5D, 0.5D);
        List<AxisAlignedBB> localList = this.c.getCubes(this.b, ((AxisAlignedBB) localObject).a(0.0D, -1.0D, 0.0D));
        double d1 = -1.0D;
        localObject = ((AxisAlignedBB) localObject).c(0.0D, 1.0D, 0.0D);
        for (AxisAlignedBB localAxisAlignedBB : localList) {
            d1 = localAxisAlignedBB.b((AxisAlignedBB) localObject, d1);
        }
        this.b.getControllerMove().a(localVec3D.a, localVec3D.b + d1, localVec3D.c, this.e);
    }

    @Override
    protected void l() {
        Vec3D localVec3D1 = c();
        int k = this.d.d();
        for (int m = this.d.e(); m < this.d.d(); m++) {
            if (this.d.a(m).b != (int) localVec3D1.b) {
                k = m;
                break;
            }
        }
        float f1 = this.b.width * this.b.width * this.i;
        for (int n = this.d.e(); n < k; n++) {
            Vec3D localVec3D2 = this.d.a(this.b, n);
            if (localVec3D1.distanceSquared(localVec3D2) < f1) {
                this.d.c(n + 1);
            }
        }
        int n = MathHelper.f(this.b.width);
        int i1 = (int) this.b.length + 1;
        int i2 = n;
        for (int i3 = k - 1; i3 >= this.d.e(); i3--) {
            if (a(localVec3D1, this.d.a(this.b, i3), n, i1, i2)) {
                this.d.c(i3);
                break;
            }
        }
        a(localVec3D1);
    }

    @Override
    public boolean m() {
        return this.d == null || this.d.b();
    }

    @Override
    public void n() {
        this.d = null;
    }

    @Override
    protected boolean o() {
        return this.b.V() || this.b.ab();
    }

    private int p() {
        if (!this.b.V() || !h())
            return (int) (this.b.getBoundingBox().b + 0.5D);
        int i = (int) this.b.getBoundingBox().b;
        Block localBlock = this.c
                .getType(new BlockPosition(MathHelper.floor(this.b.locX), i, MathHelper.floor(this.b.locZ))).getBlock();
        int j = 0;
        while (localBlock == Blocks.FLOWING_WATER || localBlock == Blocks.WATER) {
            i++;
            localBlock = this.c
                    .getType(new BlockPosition(MathHelper.floor(this.b.locX), i, MathHelper.floor(this.b.locZ)))
                    .getBlock();
            j++;
            if (j > 16)
                return (int) this.b.getBoundingBox().b;
        }
        return i;
    }

    public void setRange(float pathfindingRange) {
        this.a.setValue(pathfindingRange);
    }

    private static EntityInsentient getDummyInsentient(EntityHumanNPC from, World world) {
        return new EntityInsentient(world) {
        };
    }
}
