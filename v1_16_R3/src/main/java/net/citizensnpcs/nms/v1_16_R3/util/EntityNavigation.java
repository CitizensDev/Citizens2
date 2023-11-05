package net.citizensnpcs.nms.v1_16_R3.util;

import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSet;

import net.citizensnpcs.Settings.Setting;
import net.minecraft.server.v1_16_R3.AttributeModifiable;
import net.minecraft.server.v1_16_R3.BaseBlockPosition;
import net.minecraft.server.v1_16_R3.Block;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.Blocks;
import net.minecraft.server.v1_16_R3.ChunkCache;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityInsentient;
import net.minecraft.server.v1_16_R3.EntityLiving;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.GenericAttributes;
import net.minecraft.server.v1_16_R3.IBlockData;
import net.minecraft.server.v1_16_R3.MathHelper;
import net.minecraft.server.v1_16_R3.NavigationAbstract;
import net.minecraft.server.v1_16_R3.PathEntity;
import net.minecraft.server.v1_16_R3.PathMode;
import net.minecraft.server.v1_16_R3.PathPoint;
import net.minecraft.server.v1_16_R3.PathType;
import net.minecraft.server.v1_16_R3.Pathfinder;
import net.minecraft.server.v1_16_R3.PathfinderAbstract;
import net.minecraft.server.v1_16_R3.PathfinderNormal;
import net.minecraft.server.v1_16_R3.SystemUtils;
import net.minecraft.server.v1_16_R3.Vec3D;
import net.minecraft.server.v1_16_R3.World;

public class EntityNavigation extends NavigationAbstract {
    protected final EntityLiving a;
    protected PathEntity c;
    protected double d;
    protected int e;
    protected int f;
    private final AttributeModifiable followRange;
    protected Vec3D g = Vec3D.ORIGIN;
    protected BaseBlockPosition h = BaseBlockPosition.ZERO;
    protected long i;
    protected long j;
    protected double k;
    protected float l = 0.5F;
    protected boolean m;
    private final MobAI mvmt;
    protected long n;
    protected EntityPathfinderNormal o;
    private BlockPosition p;
    private boolean pp;
    private int q;
    private float r = 1.0F;
    private final EntityPathfinder s;
    private boolean t;

    public EntityNavigation(EntityLiving entityinsentient, World world) {
        super(getDummyInsentient(entityinsentient, world), world);
        this.g = Vec3D.ORIGIN;
        this.l = 0.5F;
        this.r = 1.0F;
        this.mvmt = MobAI.from(entityinsentient);
        this.a = entityinsentient;
        this.followRange = entityinsentient.getAttributeInstance(GenericAttributes.FOLLOW_RANGE);
        this.o = new EntityPathfinderNormal();
        this.o.a(true);
        this.s = new EntityPathfinder(this.o, Setting.MAXIMUM_VISITED_NODES.asInt());
        this.setRange(24);
        // this.b.C().a(this);
    }

    @Override
    protected boolean a() {
        return this.a.isOnGround() || p() || this.a.isPassenger();
    }

    @Override
    public boolean a(BlockPosition var0) {
        BlockPosition var1 = var0.down();
        return this.b.getType(var1).i(this.b, var1);
    }

    @Override
    public PathEntity a(BlockPosition var0, int var1) {
        if (this.b.getType(var0).isAir()) {
            BlockPosition var2 = var0.down();
            while (var2.getY() > 0 && this.b.getType(var2).isAir()) {
                var2 = var2.down();
            }
            if (var2.getY() > 0)
                return supera(var2.up(), var1);
            while (var2.getY() < this.b.getBuildHeight() && this.b.getType(var2).isAir()) {
                var2 = var2.up();
            }
            var0 = var2;
        }
        if (this.b.getType(var0).getMaterial().isBuildable()) {
            BlockPosition var2 = var0.up();
            while (var2.getY() < this.b.getBuildHeight() && this.b.getType(var2).getMaterial().isBuildable()) {
                var2 = var2.up();
            }
            return supera(var2, var1);
        }
        return supera(var0, var1);
    }

    public void a(boolean var0) {
        this.o.b(var0);
    }

    @Override
    public void a(double var0) {
        this.d = var0;
    }

    @Override
    public boolean a(double var0, double var2, double var4, double var6) {
        return a(a(var0, var2, var4, 1), var6);
    }

    @Override
    public boolean a(Entity var0, double var1) {
        PathEntity var3 = a(var0, 1);
        return var3 != null && a(var3, var1);
    }

    @Override
    public PathEntity a(Entity var0, int var1) {
        return a(var0.getChunkCoordinates(), var1);
    }

    @Override
    public void a(float var0) {
        this.r = var0;
    }

    @Override
    protected Pathfinder a(int arg0) {
        return null;
    }

    private boolean a(int var0, int var1, int var2, int var3, int var4, int var5, Vec3D var6, double var7,
            double var9) {
        int var11 = var0 - var3 / 2;
        int var12 = var2 - var5 / 2;
        if (!b(var11, var1, var12, var3, var4, var5, var6, var7, var9))
            return false;
        for (int var13 = var11; var13 < var11 + var3; var13++) {
            for (int var14 = var12; var14 < var12 + var5; var14++) {
                double var15 = var13 + 0.5D - var6.x;
                double var17 = var14 + 0.5D - var6.z;
                if (var15 * var7 + var17 * var9 >= 0.0D) {
                    PathType var19 = this.o.a(this.b, var13, var1 - 1, var14, this.a, var3, var4, var5, true, true);
                    if (!a(var19))
                        return false;
                    var19 = this.o.a(this.b, var13, var1, var14, this.a, var3, var4, var5, true, true);
                    float var20 = mvmt.getPathfindingMalus(var19);
                    if (var20 < 0.0F || var20 >= 8.0F)
                        return false;
                    if (var19 == PathType.DAMAGE_FIRE || var19 == PathType.DANGER_FIRE
                            || var19 == PathType.DAMAGE_OTHER)
                        return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean a(PathEntity var0, double var1) {
        if (var0 == null) {
            this.c = null;
            return false;
        }
        if (!var0.a(this.c)) {
            this.c = var0;
        }
        if (m())
            return false;
        D_();
        if (this.c.e() <= 0)
            return false;
        this.d = var1;
        Vec3D var3 = b();
        this.f = this.e;
        this.g = var3;
        return true;
    }

    protected boolean a(PathType var0) {
        if (var0 == PathType.WATER || var0 == PathType.LAVA || var0 == PathType.OPEN)
            return false;
        return true;
    }

    @Override
    public PathEntity a(Set<BlockPosition> var0, int var1) {
        return a(var0, 8, false, var1);
    }

    @Override
    protected PathEntity a(Set<BlockPosition> var0, int var1, boolean var2, int var3) {
        if (var0.isEmpty() || this.a.locY() < 0.0D || !a())
            return null;
        if (this.c != null && !this.c.c() && var0.contains(this.p))
            return this.c;
        this.b.getMethodProfiler().enter("pathfind");
        float var4 = (float) this.a.b(GenericAttributes.FOLLOW_RANGE);
        BlockPosition var5 = var2 ? this.a.getChunkCoordinates().up() : this.a.getChunkCoordinates();
        int var6 = (int) (var4 + var1);
        ChunkCache var7 = new ChunkCache(this.b, var5.b(-var6, -var6, -var6), var5.b(var6, var6, var6));
        PathEntity var8 = this.s.a(var7, this.a, var0, var4, var3, this.r);
        this.b.getMethodProfiler().exit();
        if (var8 != null && var8.m() != null) {
            this.p = var8.m();
            this.q = var3;
            e();
        }
        return var8;
    }

    @Override
    public PathEntity a(Stream<BlockPosition> var0, int var1) {
        return a((Set<BlockPosition>) var0.collect((Collector) Collectors.toSet()), 8, false, var1);
    }

    @Override
    protected void a(Vec3D var0) {
        if (this.e - this.f > 100) {
            if (var0.distanceSquared(this.g) < 2.25D) {
                this.t = true;
                o();
            } else {
                this.t = false;
            }
            this.f = this.e;
            this.g = var0;
        }
        if (this.c != null && !this.c.c()) {
            BaseBlockPosition var1 = this.c.g();
            if (var1.equals(this.h)) {
                this.i += SystemUtils.getMonotonicMillis() - this.j;
            } else {
                this.h = var1;
                double var2 = var0.f(Vec3D.c(this.h));
                this.k = this.a.dN() > 0.0F ? var2 / this.a.dN() * 1000.0D : 0.0D;
            }
            if (this.k > 0.0D && this.i > this.k * 3.0D) {
                e();
                o();
            }
            this.j = SystemUtils.getMonotonicMillis();
        }
    }

    @Override
    protected boolean a(Vec3D var0, Vec3D var1, int var2, int var3, int var4) {
        int var5 = MathHelper.floor(var0.x);
        int var6 = MathHelper.floor(var0.z);
        double var7 = var1.x - var0.x;
        double var9 = var1.z - var0.z;
        double var11 = var7 * var7 + var9 * var9;
        if (var11 < 1.0E-8D)
            return false;
        double var13 = 1.0D / Math.sqrt(var11);
        var7 *= var13;
        var9 *= var13;
        var2 += 2;
        var4 += 2;
        if (!a(var5, MathHelper.floor(var0.y), var6, var2, var3, var4, var0, var7, var9))
            return false;
        var2 -= 2;
        var4 -= 2;
        double var15 = 1.0D / Math.abs(var7);
        double var17 = 1.0D / Math.abs(var9);
        double var19 = var5 - var0.x;
        double var21 = var6 - var0.z;
        if (var7 >= 0.0D) {
            var19++;
        }
        if (var9 >= 0.0D) {
            var21++;
        }
        var19 /= var7;
        var21 /= var9;
        int var23 = var7 < 0.0D ? -1 : 1;
        int var24 = var9 < 0.0D ? -1 : 1;
        int var25 = MathHelper.floor(var1.x);
        int var26 = MathHelper.floor(var1.z);
        int var27 = var25 - var5;
        int var28 = var26 - var6;
        while (var27 * var23 > 0 || var28 * var24 > 0) {
            if (var19 < var21) {
                var19 += var15;
                var5 += var23;
                var27 = var25 - var5;
            } else {
                var21 += var17;
                var6 += var24;
                var28 = var26 - var6;
            }
            if (!a(var5, MathHelper.floor(var0.y), var6, var2, var3, var4, var0, var7, var9))
                return false;
        }
        return true;
    }

    @Override
    protected Vec3D b() {
        return new Vec3D(this.a.locX(), u(), this.a.locZ());
    }

    @Override
    public void b(BlockPosition var0) {
        if (this.c == null || this.c.c() || this.c.e() == 0)
            return;
        PathPoint var1 = this.c.d();
        Vec3D var2 = new Vec3D((var1.a + this.a.locX()) / 2.0D, (var1.b + this.a.locY()) / 2.0D,
                (var1.c + this.a.locZ()) / 2.0D);
        if (var0.a(var2, this.c.e() - this.c.f())) {
            j();
        }
    }

    private boolean b(int var0, int var1, int var2, int var3, int var4, int var5, Vec3D var6, double var7,
            double var9) {
        for (BlockPosition var12 : BlockPosition.a(new BlockPosition(var0, var1, var2),
                new BlockPosition(var0 + var3 - 1, var1 + var4 - 1, var2 + var5 - 1))) {
            double var13 = var12.getX() + 0.5D - var6.x;
            double var15 = var12.getZ() + 0.5D - var6.z;
            if (var13 * var7 + var15 * var9 < 0.0D) {
                continue;
            }
            if (!this.b.getType(var12).a(this.b, var12, PathMode.LAND))
                return false;
        }
        return true;
    }

    public boolean b(PathType pathtype) {
        return pathtype != PathType.DANGER_FIRE && pathtype != PathType.DANGER_CACTUS
                && pathtype != PathType.DANGER_OTHER;
    }

    private boolean b(Vec3D var0) {
        if (this.c.f() + 1 >= this.c.e())
            return false;
        Vec3D var1 = Vec3D.c(this.c.g());
        if (!var0.a(var1, 2.0D))
            return false;
        Vec3D var2 = Vec3D.c(this.c.d(this.c.f() + 1));
        Vec3D var3 = var2.d(var1);
        Vec3D var4 = var0.d(var1);
        return var3.b(var4) > 0.0D;
    }

    @Override
    public void c() {
        this.e++;
        if (this.m) {
            j();
        }
        if (m())
            return;
        if (a()) {
            l();
        } else if (this.c != null && !this.c.c()) {
            Vec3D vec3D1 = b();
            Vec3D vec3D2 = this.c.a(this.a);
            if (vec3D1.y > vec3D2.y && !this.a.isOnGround() && MathHelper.floor(vec3D1.x) == MathHelper.floor(vec3D2.x)
                    && MathHelper.floor(vec3D1.z) == MathHelper.floor(vec3D2.z)) {
                this.c.a();
            }
        }
        if (m())
            return;
        Vec3D var0 = this.c.a(this.a);
        BlockPosition var1 = new BlockPosition(var0);
        mvmt.getMoveControl().a(var0.x, this.b.getType(var1.down()).isAir() ? var0.y : PathfinderNormal.a(this.b, var1),
                var0.z, this.d);
    }

    public void c(boolean var0) {
        this.pp = var0;
    }

    @Override
    public void d(boolean var0) {
        this.o.c(var0);
    }

    @Override
    protected void D_() {
        superD_();
        if (this.pp) {
            if (this.b.e(new BlockPosition(this.a.locX(), this.a.locY() + 0.5D, this.a.locZ())))
                return;
            for (int var0 = 0; var0 < this.c.e(); var0++) {
                PathPoint var1 = this.c.a(var0);
                if (this.b.e(new BlockPosition(var1.a, var1.b, var1.c))) {
                    this.c.b(var0);
                    return;
                }
            }
        }
    }

    private void e() {
        this.h = BaseBlockPosition.ZERO;
        this.i = 0L;
        this.k = 0.0D;
        this.t = false;
    }

    public boolean f() {
        return this.o.c();
    }

    @Override
    public void g() {
        this.r = 1.0F;
    }

    @Override
    public BlockPosition h() {
        return this.p;
    }

    @Override
    public boolean i() {
        return this.m;
    }

    @Override
    public void j() {
        if (this.b.getTime() - this.n > 20L) {
            if (this.p != null) {
                this.c = null;
                this.c = a(this.p, this.q);
                this.n = this.b.getTime();
                this.m = false;
            }
        } else {
            this.m = true;
        }
    }

    @Override
    public PathEntity k() {
        return this.c;
    }

    @Override
    protected void l() {
        Vec3D var0 = b();
        this.l = this.a.getWidth() > 0.75F ? this.a.getWidth() / 2.0F : 0.75F - this.a.getWidth() / 2.0F;
        BaseBlockPosition var1 = this.c.g();
        double var2 = Math.abs(this.a.locX() - (var1.getX() + 0.5D));
        double var4 = Math.abs(this.a.locY() - var1.getY());
        double var6 = Math.abs(this.a.locZ() - (var1.getZ() + 0.5D));
        boolean var8 = var2 < this.l && var6 < this.l && var4 < 1.0D;
        boolean b2 = Math.abs(this.a.locX() - (var1.getX() + 0.5D)) < this.l
                && Math.abs(this.a.locZ() - (var1.getZ() + 0.5D)) < this.l
                && Math.abs(this.a.locY() - var1.getY()) < 1.0D; // old-style calc
        if (var8 || b2 || b(this.c.h().l) && b(var0)) {
            this.c.a();
        }
        a(var0);
    }

    @Override
    public boolean m() {
        return this.c == null || this.c.c();
    }

    @Override
    public boolean n() {
        return !m();
    }

    @Override
    public void o() {
        this.c = null;
    }

    @Override
    protected boolean p() {
        return this.a.aH() || this.a.aQ();
    }

    @Override
    public PathfinderAbstract q() {
        return this.o;
    }

    @Override
    public boolean r() {
        return this.o.e();
    }

    public void setRange(float pathfindingRange) {
        this.followRange.setValue(pathfindingRange);
    }

    public PathEntity supera(BlockPosition var0, int var1) {
        return a(ImmutableSet.of(var0), 16, true, var1);
    }

    protected void superD_() {
        if (this.c == null)
            return;
        for (int var0 = 0; var0 < this.c.e(); var0++) {
            PathPoint var1 = this.c.a(var0);
            PathPoint var2 = var0 + 1 < this.c.e() ? this.c.a(var0 + 1) : null;
            IBlockData var3 = this.b.getType(new BlockPosition(var1.a, var1.b, var1.c));
            if (var3.a(Blocks.CAULDRON)) {
                this.c.a(var0, var1.a(var1.a, var1.b + 1, var1.c));
                if (var2 != null && var1.b >= var2.b) {
                    this.c.a(var0 + 1, var1.a(var2.a, var1.b + 1, var2.c));
                }
            }
        }
    }

    @Override
    public boolean t() {
        return this.t;
    }

    private int u() {
        if (!this.a.isInWater() || !r())
            return MathHelper.floor(this.a.locY() + 0.5D);
        int var0 = MathHelper.floor(this.a.locY());
        Block var1 = this.b.getType(new BlockPosition(this.a.locX(), var0, this.a.locZ())).getBlock();
        int var2 = 0;
        while (var1 == Blocks.WATER) {
            var0++;
            var1 = this.b.getType(new BlockPosition(this.a.locX(), var0, this.a.locZ())).getBlock();
            if (++var2 > 16)
                return MathHelper.floor(this.a.locY());
        }
        return var0;
    }

    private static EntityInsentient getDummyInsentient(EntityLiving from, World world) {
        return new EntityInsentient(EntityTypes.VILLAGER, world) {
        };
    }
}
