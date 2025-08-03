package net.citizensnpcs.nms.v1_16_R3.util;

import java.util.EnumSet;

import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;

import net.minecraft.server.v1_16_R3.AxisAlignedBB;
import net.minecraft.server.v1_16_R3.Block;
import net.minecraft.server.v1_16_R3.BlockCampfire;
import net.minecraft.server.v1_16_R3.BlockDoor;
import net.minecraft.server.v1_16_R3.BlockFenceGate;
import net.minecraft.server.v1_16_R3.BlockLeaves;
import net.minecraft.server.v1_16_R3.BlockMinecartTrackAbstract;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.Blocks;
import net.minecraft.server.v1_16_R3.ChunkCache;
import net.minecraft.server.v1_16_R3.EntityInsentient;
import net.minecraft.server.v1_16_R3.EntityLiving;
import net.minecraft.server.v1_16_R3.EnumDirection;
import net.minecraft.server.v1_16_R3.Fluid;
import net.minecraft.server.v1_16_R3.FluidTypes;
import net.minecraft.server.v1_16_R3.IBlockAccess;
import net.minecraft.server.v1_16_R3.IBlockData;
import net.minecraft.server.v1_16_R3.Material;
import net.minecraft.server.v1_16_R3.MathHelper;
import net.minecraft.server.v1_16_R3.PathDestination;
import net.minecraft.server.v1_16_R3.PathMode;
import net.minecraft.server.v1_16_R3.PathPoint;
import net.minecraft.server.v1_16_R3.PathType;
import net.minecraft.server.v1_16_R3.TagsBlock;
import net.minecraft.server.v1_16_R3.TagsFluid;
import net.minecraft.server.v1_16_R3.Vec3D;
import net.minecraft.server.v1_16_R3.VoxelShape;

public class EntityPathfinderNormal extends EntityPathfinderAbstract {
    protected float j;
    private final Long2ObjectMap<PathType> k = new Long2ObjectOpenHashMap();
    private final Object2BooleanMap<AxisAlignedBB> l = new Object2BooleanOpenHashMap();

    @Override
    public void a() {
        this.mvmt.setPathfindingMalus(PathType.WATER, this.j);
        this.k.clear();
        this.l.clear();
        super.a();
    }

    private boolean a(AxisAlignedBB var0) {
        return this.l.computeIfAbsent(var0, var1 -> !this.a.getCubes(this.b, var0));
    }

    @Override
    public void a(ChunkCache var0, EntityInsentient var1) {
        super.a(var0, var1);
        this.j = var1.a(PathType.WATER);
    }

    @Override
    public void a(ChunkCache var0, EntityLiving var1) {
        super.a(var0, var1);
        this.j = mvmt.getPathfindingMalus(PathType.WATER);
    }

    @Override
    public PathDestination a(double var0, double var2, double var4) {
        return new PathDestination(a(MathHelper.floor(var0), MathHelper.floor(var2), MathHelper.floor(var4)));
    }

    private PathType a(EntityLiving var0, BlockPosition var1) {
        return a(var0, var1.getX(), var1.getY(), var1.getZ());
    }

    private PathType a(EntityLiving var0, int var1, int var2, int var3) {
        return this.k.computeIfAbsent(BlockPosition.a(var1, var2, var3),
                var4 -> this.a(this.a, var1, var2, var3, var0, this.d, this.e, this.f, this.d(), this.c()));
    }

    protected PathType a(IBlockAccess var0, boolean var1, boolean var2, BlockPosition var3, PathType var4) {
        if (var4 == PathType.DOOR_WOOD_CLOSED && var1 && var2) {
            var4 = PathType.WALKABLE;
        }
        if (var4 == PathType.DOOR_OPEN && !var2) {
            var4 = PathType.BLOCKED;
        }
        if (var4 == PathType.RAIL && !(var0.getType(var3).getBlock() instanceof BlockMinecartTrackAbstract)
                && !(var0.getType(var3.down()).getBlock() instanceof BlockMinecartTrackAbstract)) {
            var4 = PathType.UNPASSABLE_RAIL;
        }
        if (var4 == PathType.LEAVES) {
            var4 = PathType.BLOCKED;
        }
        return var4;
    }

    @Override
    public PathType a(IBlockAccess var0, int var1, int var2, int var3) {
        return a(var0, new BlockPosition.MutableBlockPosition(var1, var2, var3));
    }

    @Override
    public PathType a(IBlockAccess var0, int var1, int var2, int var3, EntityInsentient var4, int var5, int var6,
            int var7, boolean var8, boolean var9) {
        EnumSet<PathType> var10 = EnumSet.noneOf(PathType.class);
        PathType var11 = PathType.BLOCKED;
        BlockPosition var12 = var4.getChunkCoordinates();
        var11 = a(var0, var1, var2, var3, var5, var6, var7, var8, var9, var10, var11, var12);
        if (var10.contains(PathType.FENCE))
            return PathType.FENCE;
        if (var10.contains(PathType.UNPASSABLE_RAIL))
            return PathType.UNPASSABLE_RAIL;
        PathType var13 = PathType.BLOCKED;
        for (PathType var15 : var10) {
            if (mvmt.getPathfindingMalus(var15) < 0.0F)
                return var15;
            if (mvmt.getPathfindingMalus(var15) >= mvmt.getPathfindingMalus(var13)) {
                var13 = var15;
            }
        }
        if (var11 == PathType.OPEN && mvmt.getPathfindingMalus(var13) == 0.0F && var5 <= 1)
            return PathType.OPEN;
        return var13;
    }

    public PathType a(IBlockAccess var0, int var1, int var2, int var3, EntityLiving var4, int var5, int var6, int var7,
            boolean var8, boolean var9) {
        EnumSet<PathType> var10 = EnumSet.noneOf(PathType.class);
        PathType var11 = PathType.BLOCKED;
        BlockPosition var12 = var4.getChunkCoordinates();
        var11 = a(var0, var1, var2, var3, var5, var6, var7, var8, var9, var10, var11, var12);
        if (var10.contains(PathType.FENCE))
            return PathType.FENCE;
        if (var10.contains(PathType.UNPASSABLE_RAIL))
            return PathType.UNPASSABLE_RAIL;
        PathType var13 = PathType.BLOCKED;
        for (PathType var15 : var10) {
            if (mvmt.getPathfindingMalus(var15) < 0.0F)
                return var15;
            if (mvmt.getPathfindingMalus(var15) >= mvmt.getPathfindingMalus(var13)) {
                var13 = var15;
            }
        }
        if (var11 == PathType.OPEN && mvmt.getPathfindingMalus(var13) == 0.0F && var5 <= 1)
            return PathType.OPEN;
        return var13;
    }

    public PathType a(IBlockAccess var0, int var1, int var2, int var3, int var4, int var5, int var6, boolean var7,
            boolean var8, EnumSet<PathType> var9, PathType var10, BlockPosition var11) {
        for (int var12 = 0; var12 < var4; var12++) {
            for (int var13 = 0; var13 < var5; var13++) {
                for (int var14 = 0; var14 < var6; var14++) {
                    int var15 = var12 + var1;
                    int var16 = var13 + var2;
                    int var17 = var14 + var3;
                    PathType var18 = a(var0, var15, var16, var17);
                    var18 = a(var0, var7, var8, var11, var18);
                    if (var12 == 0 && var13 == 0 && var14 == 0) {
                        var10 = var18;
                    }
                    var9.add(var18);
                }
            }
        }
        return var10;
    }

    private PathPoint a(int var0, int var1, int var2, int var3, double var4, EnumDirection var6, PathType var7) {
        PathPoint var8 = null;
        BlockPosition.MutableBlockPosition var9 = new BlockPosition.MutableBlockPosition();
        double var10 = aa(this.a, var9.d(var0, var1, var2));
        if (var10 - var4 > 1.125D)
            return null;
        PathType var12 = a(this.b, var0, var1, var2);
        float var13 = this.mvmt.getPathfindingMalus(var12);
        double var14 = this.b.getWidth() / 2.0D;
        if (var13 >= 0.0F) {
            var8 = a(var0, var1, var2);
            var8.l = var12;
            var8.k = Math.max(var8.k, var13);
        }
        if (var7 == PathType.FENCE && var8 != null && var8.k >= 0.0F && !a(var8)) {
            var8 = null;
        }
        if (var12 == PathType.WALKABLE)
            return var8;
        if ((var8 == null || var8.k < 0.0F) && var3 > 0 && var12 != PathType.FENCE && var12 != PathType.UNPASSABLE_RAIL
                && var12 != PathType.TRAPDOOR) {
            var8 = a(var0, var1 + 1, var2, var3 - 1, var4, var6, var7);
            if (var8 != null && (var8.l == PathType.OPEN || var8.l == PathType.WALKABLE) && this.b.getWidth() < 1.0F) {
                double var16 = var0 - var6.getAdjacentX() + 0.5D;
                double var18 = var2 - var6.getAdjacentZ() + 0.5D;
                AxisAlignedBB var20 = new AxisAlignedBB(var16 - var14,
                        aa(this.a, var9.c(var16, var1 + 1, var18)) + 0.001D, var18 - var14, var16 + var14,
                        this.b.getHeight() + aa(this.a, var9.c(var8.a, var8.b, var8.c)) - 0.002D, var18 + var14);
                if (a(var20)) {
                    var8 = null;
                }
            }
        }
        if (var12 == PathType.WATER && !e()) {
            if (a(this.b, var0, var1 - 1, var2) != PathType.WATER)
                return var8;
            while (var1 > 0) {
                var1--;
                var12 = a(this.b, var0, var1, var2);
                if (var12 == PathType.WATER) {
                    var8 = a(var0, var1, var2);
                    var8.l = var12;
                    var8.k = Math.max(var8.k, mvmt.getPathfindingMalus(var12));
                    continue;
                }
                return var8;
            }
        }
        if (var12 == PathType.OPEN) {
            int var16 = 0;
            int var17 = var1;
            while (var12 == PathType.OPEN) {
                var1--;
                if (var1 < 0) {
                    PathPoint var18 = a(var0, var17, var2);
                    var18.l = PathType.BLOCKED;
                    var18.k = -1.0F;
                    return var18;
                }
                if (var16++ >= this.b.bP()) {
                    PathPoint var18 = a(var0, var1, var2);
                    var18.l = PathType.BLOCKED;
                    var18.k = -1.0F;
                    return var18;
                }
                var12 = a(this.b, var0, var1, var2);
                var13 = mvmt.getPathfindingMalus(var12);
                if (var12 != PathType.OPEN && var13 >= 0.0F) {
                    var8 = a(var0, var1, var2);
                    var8.l = var12;
                    var8.k = Math.max(var8.k, var13);
                    break;
                }
                if (var13 < 0.0F) {
                    PathPoint var18 = a(var0, var1, var2);
                    var18.l = PathType.BLOCKED;
                    var18.k = -1.0F;
                    return var18;
                }
            }
        }
        if (var12 == PathType.FENCE) {
            var8 = a(var0, var1, var2);
            var8.i = true;
            var8.l = var12;
            var8.k = var12.a();
        }
        return var8;
    }

    private boolean a(PathPoint var0) {
        Vec3D var1 = new Vec3D(var0.a - this.b.locX(), var0.b - this.b.locY(), var0.c - this.b.locZ());
        AxisAlignedBB var2 = this.b.getBoundingBox();
        int var3 = MathHelper.f(var1.f() / var2.a());
        var1 = var1.a(1.0F / var3);
        for (int var4 = 1; var4 <= var3; var4++) {
            var2 = var2.c(var1);
            if (a(var2))
                return false;
        }
        return true;
    }

    private boolean a(PathPoint var0, PathPoint var1) {
        return var0 != null && !var0.i && (var0.k >= 0.0F || var1.k < 0.0F);
    }

    private boolean a(PathPoint var0, PathPoint var1, PathPoint var2, PathPoint var3) {
        if (var3 == null || var2 == null || var1 == null || var3.i)
            return false;
        if (var2.b > var0.b || var1.b > var0.b)
            return false;
        if (var1.l == PathType.WALKABLE_DOOR || var2.l == PathType.WALKABLE_DOOR || var3.l == PathType.WALKABLE_DOOR)
            return false;
        boolean var4 = var2.l == PathType.FENCE && var1.l == PathType.FENCE && this.b.getWidth() < 0.5D;
        return var3.k >= 0.0F && (var2.b < var0.b || var2.k >= 0.0F || var4)
                && (var1.b < var0.b || var1.k >= 0.0F || var4);
    }

    @Override
    public int a(PathPoint[] var0, PathPoint var1) {
        int var2 = 0;
        int var3 = 0;
        PathType var4 = a(this.b, var1.a, var1.b + 1, var1.c);
        PathType var5 = a(this.b, var1.a, var1.b, var1.c);
        if (mvmt.getPathfindingMalus(var4) >= 0.0F && var5 != PathType.STICKY_HONEY) {
            var3 = MathHelper.d(Math.max(1.0F, this.b.G));
        }
        double var6 = aa(this.a, new BlockPosition(var1.a, var1.b, var1.c));
        PathPoint var8 = a(var1.a, var1.b, var1.c + 1, var3, var6, EnumDirection.SOUTH, var5);
        if (a(var8, var1)) {
            var0[var2++] = var8;
        }
        PathPoint var9 = a(var1.a - 1, var1.b, var1.c, var3, var6, EnumDirection.WEST, var5);
        if (a(var9, var1)) {
            var0[var2++] = var9;
        }
        PathPoint var10 = a(var1.a + 1, var1.b, var1.c, var3, var6, EnumDirection.EAST, var5);
        if (a(var10, var1)) {
            var0[var2++] = var10;
        }
        PathPoint var11 = a(var1.a, var1.b, var1.c - 1, var3, var6, EnumDirection.NORTH, var5);
        if (a(var11, var1)) {
            var0[var2++] = var11;
        }
        PathPoint var12 = a(var1.a - 1, var1.b, var1.c - 1, var3, var6, EnumDirection.NORTH, var5);
        if (a(var1, var9, var11, var12)) {
            var0[var2++] = var12;
        }
        PathPoint var13 = a(var1.a + 1, var1.b, var1.c - 1, var3, var6, EnumDirection.NORTH, var5);
        if (a(var1, var10, var11, var13)) {
            var0[var2++] = var13;
        }
        PathPoint var14 = a(var1.a - 1, var1.b, var1.c + 1, var3, var6, EnumDirection.SOUTH, var5);
        if (a(var1, var9, var8, var14)) {
            var0[var2++] = var14;
        }
        PathPoint var15 = a(var1.a + 1, var1.b, var1.c + 1, var3, var6, EnumDirection.SOUTH, var5);
        if (a(var1, var10, var8, var15)) {
            var0[var2++] = var15;
        }
        return var2;
    }

    @Override
    public PathPoint b() {
        BlockPosition.MutableBlockPosition var1 = new BlockPosition.MutableBlockPosition();
        int var0 = MathHelper.floor(this.b.locY());
        IBlockData var2 = this.a.getType(var1.c(this.b.locX(), var0, this.b.locZ()));
        if (this.b.a(var2.getFluid().getType())) {
            while (this.b.a(var2.getFluid().getType())) {
                var0++;
                var2 = this.a.getType(var1.c(this.b.locX(), var0, this.b.locZ()));
            }
            var0--;
        } else if (e() && this.b.isInWater()) {
            while (var2.getBlock() == Blocks.WATER || var2.getFluid() == FluidTypes.WATER.a(false)) {
                var0++;
                var2 = this.a.getType(var1.c(this.b.locX(), var0, this.b.locZ()));
            }
            var0--;
        } else if (this.b.isOnGround()) {
            var0 = MathHelper.floor(this.b.locY() + 0.5D);
        } else {
            BlockPosition blockPosition = this.b.getChunkCoordinates();
            while ((this.a.getType(blockPosition).isAir()
                    || this.a.getType(blockPosition).a(this.a, blockPosition, PathMode.LAND))
                    && blockPosition.getY() > 0) {
                blockPosition = blockPosition.down();
            }
            var0 = blockPosition.up().getY();
        }
        BlockPosition var3 = this.b.getChunkCoordinates();
        PathType var4 = a(this.b, var3.getX(), var0, var3.getZ());
        if (mvmt.getPathfindingMalus(var4) < 0.0F) {
            AxisAlignedBB axisAlignedBB = this.b.getBoundingBox();
            if (b(var1.c(axisAlignedBB.minX, var0, axisAlignedBB.minZ))
                    || b(var1.c(axisAlignedBB.minX, var0, axisAlignedBB.maxZ))
                    || b(var1.c(axisAlignedBB.maxX, var0, axisAlignedBB.minZ))
                    || b(var1.c(axisAlignedBB.maxX, var0, axisAlignedBB.maxZ))) {
                PathPoint var6 = a(var1);
                var6.l = a(this.b, var6.a());
                var6.k = mvmt.getPathfindingMalus(var6.l);
                return var6;
            }
        }
        PathPoint var5 = a(var3.getX(), var0, var3.getZ());
        var5.l = a(this.b, var5.a());
        var5.k = mvmt.getPathfindingMalus(var5.l);
        return var5;
    }

    private boolean b(BlockPosition var0) {
        PathType var1 = a(this.b, var0);
        return mvmt.getPathfindingMalus(var1) >= 0.0F;
    }

    public static PathType a(IBlockAccess var0, BlockPosition.MutableBlockPosition var1) {
        int var2 = var1.getX();
        int var3 = var1.getY();
        int var4 = var1.getZ();
        PathType var5 = b(var0, var1);
        if (var5 == PathType.OPEN && var3 >= 1) {
            PathType var6 = b(var0, var1.d(var2, var3 - 1, var4));
            var5 = var6 == PathType.WALKABLE || var6 == PathType.OPEN || var6 == PathType.WATER || var6 == PathType.LAVA
                    ? PathType.OPEN
                    : PathType.WALKABLE;
            if (var6 == PathType.DAMAGE_FIRE) {
                var5 = PathType.DAMAGE_FIRE;
            }
            if (var6 == PathType.DAMAGE_CACTUS) {
                var5 = PathType.DAMAGE_CACTUS;
            }
            if (var6 == PathType.DAMAGE_OTHER) {
                var5 = PathType.DAMAGE_OTHER;
            }
            if (var6 == PathType.STICKY_HONEY) {
                var5 = PathType.STICKY_HONEY;
            }
        }
        if (var5 == PathType.WALKABLE) {
            var5 = a(var0, var1.d(var2, var3, var4), var5);
        }
        return var5;
    }

    public static PathType a(IBlockAccess var0, BlockPosition.MutableBlockPosition var1, PathType var2) {
        int var3 = var1.getX();
        int var4 = var1.getY();
        int var5 = var1.getZ();
        for (int var6 = -1; var6 <= 1; var6++) {
            for (int var7 = -1; var7 <= 1; var7++) {
                for (int var8 = -1; var8 <= 1; var8++) {
                    if (var6 != 0 || var8 != 0) {
                        var1.d(var3 + var6, var4 + var7, var5 + var8);
                        IBlockData var9 = var0.getType(var1);
                        if (var9.a(Blocks.CACTUS))
                            return PathType.DANGER_CACTUS;
                        if (var9.a(Blocks.SWEET_BERRY_BUSH))
                            return PathType.DANGER_OTHER;
                        if (a(var9))
                            return PathType.DANGER_FIRE;
                        if (var0.getFluid(var1).a(TagsFluid.WATER))
                            return PathType.WATER_BORDER;
                    }
                }
            }
        }
        return var2;
    }

    private static boolean a(IBlockData var0) {
        return var0.a(TagsBlock.FIRE) || var0.a(Blocks.LAVA) || var0.a(Blocks.MAGMA_BLOCK) || BlockCampfire.g(var0);
    }

    public static double aa(IBlockAccess var0, BlockPosition var1) {
        BlockPosition var2 = var1.down();
        VoxelShape var3 = var0.getType(var2).getCollisionShape(var0, var2);
        return var2.getY() + (var3.isEmpty() ? 0.0D : var3.c(EnumDirection.EnumAxis.Y));
    }

    protected static PathType b(IBlockAccess var0, BlockPosition var1) {
        IBlockData var2 = var0.getType(var1);
        Block var3 = var2.getBlock();
        Material var4 = var2.getMaterial();
        if (var2.isAir())
            return PathType.OPEN;
        if (var2.a(TagsBlock.TRAPDOORS) || var2.a(Blocks.LILY_PAD))
            return PathType.TRAPDOOR;
        if (var2.a(Blocks.CACTUS))
            return PathType.DAMAGE_CACTUS;
        if (var2.a(Blocks.SWEET_BERRY_BUSH))
            return PathType.DAMAGE_OTHER;
        if (var2.a(Blocks.HONEY_BLOCK))
            return PathType.STICKY_HONEY;
        if (var2.a(Blocks.COCOA))
            return PathType.COCOA;
        Fluid var5 = var0.getFluid(var1);
        if (var5.a(TagsFluid.WATER))
            return PathType.WATER;
        if (var5.a(TagsFluid.LAVA))
            return PathType.LAVA;
        if (a(var2))
            return PathType.DAMAGE_FIRE;
        if (BlockDoor.l(var2) && !var2.get(BlockDoor.OPEN).booleanValue())
            return PathType.DOOR_WOOD_CLOSED;
        if (var3 instanceof BlockDoor && var4 == Material.ORE && !var2.get(BlockDoor.OPEN).booleanValue())
            return PathType.DOOR_IRON_CLOSED;
        if (var3 instanceof BlockDoor && var2.get(BlockDoor.OPEN).booleanValue())
            return PathType.DOOR_OPEN;
        if (var3 instanceof BlockMinecartTrackAbstract)
            return PathType.RAIL;
        if (var3 instanceof BlockLeaves)
            return PathType.LEAVES;
        if (var3.a(TagsBlock.FENCES) || var3.a(TagsBlock.WALLS)
                || var3 instanceof BlockFenceGate && !var2.get(BlockFenceGate.OPEN).booleanValue())
            return PathType.FENCE;
        if (!var2.a(var0, var1, PathMode.LAND))
            return PathType.BLOCKED;
        return PathType.OPEN;
    }
}
