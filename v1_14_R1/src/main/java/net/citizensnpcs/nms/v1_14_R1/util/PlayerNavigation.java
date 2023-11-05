package net.citizensnpcs.nms.v1_14_R1.util;

import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSet;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.nms.v1_14_R1.entity.EntityHumanNPC;
import net.minecraft.server.v1_14_R1.AttributeInstance;
import net.minecraft.server.v1_14_R1.Block;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.Blocks;
import net.minecraft.server.v1_14_R1.ChunkCache;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityInsentient;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.GenericAttributes;
import net.minecraft.server.v1_14_R1.IBlockData;
import net.minecraft.server.v1_14_R1.IWorldReader;
import net.minecraft.server.v1_14_R1.MathHelper;
import net.minecraft.server.v1_14_R1.NavigationAbstract;
import net.minecraft.server.v1_14_R1.PathEntity;
import net.minecraft.server.v1_14_R1.PathMode;
import net.minecraft.server.v1_14_R1.PathPoint;
import net.minecraft.server.v1_14_R1.PathType;
import net.minecraft.server.v1_14_R1.Pathfinder;
import net.minecraft.server.v1_14_R1.PathfinderAbstract;
import net.minecraft.server.v1_14_R1.PathfinderNormal;
import net.minecraft.server.v1_14_R1.SystemUtils;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.World;

public class PlayerNavigation extends NavigationAbstract {
    protected EntityHumanNPC a;
    protected final World b;
    protected PathEntity c;
    protected double d;
    protected int e;
    protected int f;
    protected Vec3D g;
    protected Vec3D h;
    protected long i;
    protected long j;
    protected double k;
    protected float l;
    protected boolean m;
    protected long n;
    protected PlayerPathfinderNormal o;
    private final AttributeInstance p;
    private boolean pp;
    private BlockPosition q;
    private int r;
    private final PlayerPathfinder s;

    public PlayerNavigation(EntityHumanNPC entityinsentient, World world) {
        super(getDummyInsentient(entityinsentient, world), world);
        this.g = Vec3D.a;
        this.h = Vec3D.a;
        this.l = 0.5F;
        this.a = entityinsentient;
        this.b = world;
        this.p = entityinsentient.getAttributeInstance(GenericAttributes.FOLLOW_RANGE);
        this.o = new PlayerPathfinderNormal();
        this.o.a(true);
        this.s = new PlayerPathfinder(this.o, Setting.MAXIMUM_VISITED_NODES.asInt());
        this.setRange(24);
        // this.b.C().a(this);
    }

    @Override
    protected boolean a() {
        return this.a.onGround || this.p() || this.a.isPassenger();
    }

    @Override
    public boolean a(BlockPosition var0) {
        BlockPosition var1 = var0.down();
        return this.b.getType(var1).g(this.b, var1);
    }

    @Override
    public PathEntity a(BlockPosition var0, int var1) {
        BlockPosition var2;
        if (this.b.getType(var0).isAir()) {
            for (var2 = var0.down(); var2.getY() > 0 && this.b.getType(var2).isAir(); var2 = var2.down()) {
            }
            if (var2.getY() > 0)
                return supera(var2.up(), var1);
            while (var2.getY() < this.b.getBuildHeight() && this.b.getType(var2).isAir()) {
                var2 = var2.up();
            }
            var0 = var2;
        }
        if (!this.b.getType(var0).getMaterial().isBuildable())
            return supera(var0, var1);
        else {
            for (var2 = var0.up(); var2.getY() < this.b.getBuildHeight()
                    && this.b.getType(var2).getMaterial().isBuildable(); var2 = var2.up()) {
            }
            return supera(var2, var1);
        }
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
        return this.a(this.a(var0, var2, var4, 1), var6);
    }

    @Override
    public boolean a(Entity var0, double var1) {
        PathEntity var3 = this.a(var0, 1);
        return var3 != null && this.a(var3, var1);
    }

    @Override
    public PathEntity a(Entity var0, int var1) {
        return this.a(ImmutableSet.of(new BlockPosition(var0)), 16, true, var1);
    }

    @Override
    protected Pathfinder a(int var1) {
        return null;
    }

    private boolean a(int var0, int var1, int var2, int var3, int var4, int var5, Vec3D var6, double var7,
            double var9) {
        int var11 = var0 - var3 / 2;
        int var12 = var2 - var5 / 2;
        if (!this.b(var11, var1, var12, var3, var4, var5, var6, var7, var9))
            return false;
        else {
            for (int var13 = var11; var13 < var11 + var3; ++var13) {
                for (int var14 = var12; var14 < var12 + var5; ++var14) {
                    double var15 = var13 + 0.5D - var6.x;
                    double var17 = var14 + 0.5D - var6.z;
                    if (var15 * var7 + var17 * var9 >= 0.0D) {
                        PathType var19 = this.o.a(this.b, var13, var1 - 1, var14, this.a, var3, var4, var5, true, true);
                        if (var19 == PathType.WATER || var19 == PathType.LAVA || var19 == PathType.OPEN)
                            return false;
                        var19 = this.o.a(this.b, var13, var1, var14, this.a, var3, var4, var5, true, true);
                        float var20 = this.a.a(var19);
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
    }

    @Override
    public boolean a(PathEntity var0, double var1) {
        if (var0 == null) {
            this.c = null;
            return false;
        } else {
            if (!var0.a(this.c)) {
                this.c = var0;
            }
            this.D_();
            if (this.c.e() <= 0)
                return false;
            else {
                this.d = var1;
                Vec3D var3 = this.b();
                this.f = this.e;
                this.g = var3;
                return true;
            }
        }
    }

    @Override
    protected PathEntity a(Set var0, int var1, boolean var2, int var3) {
        if (var0.isEmpty() || (this.a.locY < 0.0D) || !this.a())
            return null;
        else if (this.c != null && !this.c.b() && var0.contains(this.q))
            return this.c;
        else {
            this.b.getMethodProfiler().enter("pathfind");
            float var4 = this.i();
            BlockPosition var5 = var2 ? new BlockPosition(this.a).up() : new BlockPosition(this.a);
            int var6 = (int) (var4 + var1);
            IWorldReader var7 = new ChunkCache(this.b, var5.b(-var6, -var6, -var6), var5.b(var6, var6, var6));
            PathEntity var8 = this.s.a(var7, this.a, var0, var4, var3);
            this.b.getMethodProfiler().exit();
            if (var8 != null && var8.k() != null) {
                this.q = var8.k();
                this.r = var3;
            }
            return var8;
        }
    }

    @Override
    public PathEntity a(Stream var0, int var1) {
        return this.a((Set) var0.collect(Collectors.toSet()), 8, false, var1);
    }

    @Override
    protected void a(Vec3D var0) {
        if (this.e - this.f > 100) {
            if (var0.distanceSquared(this.g) < 2.25D) {
                this.o();
            }
            this.f = this.e;
            this.g = var0;
        }
        if (this.c != null && !this.c.b()) {
            Vec3D var1 = this.c.g();
            if (var1.equals(this.h)) {
                this.i += SystemUtils.getMonotonicMillis() - this.j;
            } else {
                this.h = var1;
                double var2 = var0.f(this.h);
                this.k = this.a.db() > 0.0F ? var2 / this.a.db() * 1000.0D : 0.0D;
            }
            if (this.k > 0.0D && this.i > this.k * 3.0D) {
                this.h = Vec3D.a;
                this.i = 0L;
                this.k = 0.0D;
                this.o();
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
        else {
            double var13 = 1.0D / Math.sqrt(var11);
            var7 *= var13;
            var9 *= var13;
            var2 += 2;
            var4 += 2;
            if (!this.a(var5, MathHelper.floor(var0.y), var6, var2, var3, var4, var0, var7, var9)) {
                var2 -= 2;
                var4 -= 2;
                double var15 = 1.0D / Math.abs(var7);
                double var17 = 1.0D / Math.abs(var9);
                double var19 = var5 - var0.x;
                double var21 = var6 - var0.z;
                if (var7 >= 0.0D) {
                    ++var19;
                }
                if (var9 >= 0.0D) {
                    ++var21;
                }
                var19 /= var7;
                var21 /= var9;
                int var23 = var7 < 0.0D ? -1 : 1;
                int var24 = var9 < 0.0D ? -1 : 1;
                int var25 = MathHelper.floor(var1.x);
                int var26 = MathHelper.floor(var1.z);
                int var27 = var25 - var5;
                int var28 = var26 - var6;
                do {
                    if (var27 * var23 <= 0 && var28 * var24 <= 0)
                        return true;
                    if (var19 < var21) {
                        var19 += var15;
                        var5 += var23;
                        var27 = var25 - var5;
                    } else {
                        var21 += var17;
                        var6 += var24;
                        var28 = var26 - var6;
                    }
                } while (this.a(var5, MathHelper.floor(var0.y), var6, var2, var3, var4, var0, var7, var9));
            }
            return false;
        }
    }

    @Override
    protected Vec3D b() {
        return new Vec3D(this.a.locX, this.t(), this.a.locZ);
    }

    @Override
    public void b(BlockPosition var0) {
        if (this.c != null && !this.c.b() && this.c.e() != 0) {
            PathPoint var1 = this.c.c();
            Vec3D var2 = new Vec3D((var1.a + this.a.locX) / 2.0D, (var1.b + this.a.locY) / 2.0D,
                    (var1.c + this.a.locZ) / 2.0D);
            if (var0.a(var2, this.c.e() - this.c.f())) {
                this.k();
            }
        }
    }

    private boolean b(int var0, int var1, int var2, int var3, int var4, int var5, Vec3D var6, double var7,
            double var9) {
        Iterator var12 = BlockPosition.a(new BlockPosition(var0, var1, var2),
                new BlockPosition(var0 + var3 - 1, var1 + var4 - 1, var2 + var5 - 1)).iterator();
        BlockPosition var14;
        double var13;
        double var15;
        do {
            if (!var12.hasNext())
                return true;
            var14 = (BlockPosition) var12.next();
            var13 = var14.getX() + 0.5D - var6.x;
            var15 = var14.getZ() + 0.5D - var6.z;
        } while (var13 * var7 + var15 * var9 < 0.0D || this.b.getType(var14).a(this.b, var14, PathMode.LAND));
        return false;
    }

    @Override
    public void c() {
        ++this.e;
        if (this.m) {
            this.k();
        }
        if (!this.n()) {
            Vec3D var0;
            if (this.a()) {
                this.m();
            } else if (this.c != null && this.c.f() < this.c.e()) {
                var0 = this.b();
                Vec3D var1 = this.c.a(this.a, this.c.f());
                if (var0.y > var1.y && !this.a.onGround && MathHelper.floor(var0.x) == MathHelper.floor(var1.x)
                        && MathHelper.floor(var0.z) == MathHelper.floor(var1.z)) {
                    this.c.c(this.c.f() + 1);
                }
            }
            if (!this.n()) {
                var0 = this.c.a(this.a);
                BlockPosition var1 = new BlockPosition(var0);
                this.a.getControllerMove().a(var0.x,
                        this.b.getType(var1.down()).isAir() ? var0.y : PathfinderNormal.a(this.b, var1), var0.z,
                        this.d);
            }
        }
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
            if (this.b.f(new BlockPosition(this.a.locX, this.a.getBoundingBox().minY + 0.5D, this.a.locZ)))
                return;
            for (int var0 = 0; var0 < this.c.e(); ++var0) {
                PathPoint var1 = this.c.a(var0);
                if (this.b.f(new BlockPosition(var1.a, var1.b, var1.c))) {
                    this.c.b(var0);
                    return;
                }
            }
        }
    }

    public boolean f() {
        return this.o.c();
    }

    @Override
    public BlockPosition h() {
        return this.q;
    }

    @Override
    public float i() {
        return (float) this.p.getValue();
    }

    @Override
    public boolean j() {
        return this.m;
    }

    @Override
    public void k() {
        if (this.b.getTime() - this.n > 20L) {
            if (this.q != null) {
                this.c = null;
                this.c = this.a(this.q, this.r);
                this.n = this.b.getTime();
                this.m = false;
            }
        } else {
            this.m = true;
        }
    }

    @Override
    public PathEntity l() {
        return this.c;
    }

    @Override
    protected void m() {
        Vec3D var0 = this.b();
        this.l = this.a.getWidth() > 0.75F ? this.a.getWidth() / 2.0F : 0.75F - this.a.getWidth() / 2.0F;
        Vec3D var1 = this.c.g();
        if (Math.abs(this.a.locX - (var1.x + 0.5D)) < this.l && Math.abs(this.a.locZ - (var1.z + 0.5D)) < this.l
                && Math.abs(this.a.locY - var1.y) < 1.0D) {
            this.c.c(this.c.f() + 1);
        }
        this.a(var0);
    }

    @Override
    public boolean n() {
        return this.c == null || this.c.b();
    }

    @Override
    public void o() {
        this.c = null;
    }

    @Override
    protected boolean p() {
        return this.a.av() || this.a.aD();
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
        this.p.setValue(pathfindingRange);
    }

    public PathEntity supera(BlockPosition var0, int var1) {
        return this.a(ImmutableSet.of(var0), 8, false, var1);
    }

    protected void superD_() {
        if (this.c != null) {
            for (int var0 = 0; var0 < this.c.e(); ++var0) {
                PathPoint var1 = this.c.a(var0);
                PathPoint var2 = var0 + 1 < this.c.e() ? this.c.a(var0 + 1) : null;
                IBlockData var3 = this.b.getType(new BlockPosition(var1.a, var1.b, var1.c));
                Block var4 = var3.getBlock();
                if (var4 == Blocks.CAULDRON) {
                    this.c.a(var0, var1.a(var1.a, var1.b + 1, var1.c));
                    if (var2 != null && var1.b >= var2.b) {
                        this.c.a(var0 + 1, var2.a(var2.a, var1.b + 1, var2.c));
                    }
                }
            }
        }
    }

    private int t() {
        if (this.a.isInWater() && this.r()) {
            int var0 = MathHelper.floor(this.a.getBoundingBox().minY);
            Block var1 = this.b.getType(new BlockPosition(this.a.locX, var0, this.a.locZ)).getBlock();
            int var2 = 0;
            do {
                if (var1 != Blocks.WATER)
                    return var0;
                ++var0;
                var1 = this.b.getType(new BlockPosition(this.a.locX, var0, this.a.locZ)).getBlock();
                ++var2;
            } while (var2 <= 16);
            return MathHelper.floor(this.a.getBoundingBox().minY);
        } else
            return MathHelper.floor(this.a.getBoundingBox().minY + 0.5D);
    }

    private static EntityInsentient getDummyInsentient(EntityHumanNPC from, World world) {
        return new EntityInsentient(EntityTypes.VILLAGER, world) {
        };
    }
}
