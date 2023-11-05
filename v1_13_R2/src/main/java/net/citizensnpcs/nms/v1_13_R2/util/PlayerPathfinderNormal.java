package net.citizensnpcs.nms.v1_13_R2.util;

import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;

import com.google.common.collect.Sets;

import net.citizensnpcs.api.util.BoundingBox;
import net.citizensnpcs.nms.v1_13_R2.entity.EntityHumanNPC;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_13_R2.AxisAlignedBB;
import net.minecraft.server.v1_13_R2.Block;
import net.minecraft.server.v1_13_R2.BlockCobbleWall;
import net.minecraft.server.v1_13_R2.BlockDoor;
import net.minecraft.server.v1_13_R2.BlockFence;
import net.minecraft.server.v1_13_R2.BlockFenceGate;
import net.minecraft.server.v1_13_R2.BlockMinecartTrackAbstract;
import net.minecraft.server.v1_13_R2.BlockPosition;
import net.minecraft.server.v1_13_R2.BlockPosition.MutableBlockPosition;
import net.minecraft.server.v1_13_R2.Blocks;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityInsentient;
import net.minecraft.server.v1_13_R2.EnumDirection;
import net.minecraft.server.v1_13_R2.EnumDirection.EnumAxis;
import net.minecraft.server.v1_13_R2.Fluid;
import net.minecraft.server.v1_13_R2.IBlockAccess;
import net.minecraft.server.v1_13_R2.IBlockData;
import net.minecraft.server.v1_13_R2.Material;
import net.minecraft.server.v1_13_R2.MathHelper;
import net.minecraft.server.v1_13_R2.PathMode;
import net.minecraft.server.v1_13_R2.PathPoint;
import net.minecraft.server.v1_13_R2.PathType;
import net.minecraft.server.v1_13_R2.TagsFluid;
import net.minecraft.server.v1_13_R2.VoxelShape;

public class PlayerPathfinderNormal extends PlayerPathfinderAbstract {
    protected float j;

    @Override
    public void a() {
        this.b.a(PathType.WATER, this.j);
        super.a();
    }

    @Override
    public PathPoint a(double var1, double var3, double var5) {
        return this.a(MathHelper.floor(var1), MathHelper.floor(var3), MathHelper.floor(var5));
    }

    private PathType a(EntityHumanNPC var1, BlockPosition var2) {
        return this.a(var1, var2.getX(), var2.getY(), var2.getZ());
    }

    private PathType a(EntityHumanNPC var1, int var2, int var3, int var4) {
        return this.a(this.a, var2, var3, var4, var1, this.d, this.e, this.f, this.d(), this.c());
    }

    @Override
    public void a(IBlockAccess var1, EntityHumanNPC var2) {
        super.a(var1, var2);
        this.j = var2.a(PathType.WATER);
    }

    @Override
    public PathType a(IBlockAccess var1, int var2, int var3, int var4) {
        PathType var5 = this.b(var1, var2, var3, var4);
        if (var5 == PathType.OPEN && var3 >= 1) {
            Block var6 = var1.getType(new BlockPosition(var2, var3 - 1, var4)).getBlock();
            PathType var7 = this.b(var1, var2, var3 - 1, var4);
            var5 = var7 != PathType.WALKABLE && var7 != PathType.OPEN && var7 != PathType.WATER && var7 != PathType.LAVA
                    ? PathType.WALKABLE
                    : PathType.OPEN;
            if (var7 == PathType.DAMAGE_FIRE || var6 == Blocks.MAGMA_BLOCK) {
                var5 = PathType.DAMAGE_FIRE;
            }
            if (var7 == PathType.DAMAGE_CACTUS) {
                var5 = PathType.DAMAGE_CACTUS;
            }
        }
        var5 = this.a(var1, var2, var3, var4, var5);
        return var5;
    }

    public PathType a(IBlockAccess var1, int var2, int var3, int var4, EntityHumanNPC var5, int var6, int var7,
            int var8, boolean var9, boolean var10) {
        EnumSet var11 = EnumSet.noneOf(PathType.class);
        PathType var12 = PathType.BLOCKED;
        double var13 = var5.width / 2.0D;
        BlockPosition var15 = new BlockPosition(var5);
        var12 = this.a(var1, var2, var3, var4, var6, var7, var8, var9, var10, var11, var12, var15);
        if (var11.contains(PathType.FENCE))
            return PathType.FENCE;
        else {
            PathType var16 = PathType.BLOCKED;
            Iterator var17 = var11.iterator();
            while (var17.hasNext()) {
                PathType var18 = (PathType) var17.next();
                if (var5.a(var18) < 0.0F)
                    return var18;
                if (var5.a(var18) >= var5.a(var16)) {
                    var16 = var18;
                }
            }
            if (var12 == PathType.OPEN && var5.a(var16) == 0.0F)
                return PathType.OPEN;
            else
                return var16;
        }
    }

    @Override
    public PathType a(IBlockAccess var1, int var2, int var3, int var4, EntityInsentient var5, int var6, int var7,
            int var8, boolean var9, boolean var10) {
        EnumSet var11 = EnumSet.noneOf(PathType.class);
        PathType var12 = PathType.BLOCKED;
        double var13 = var5.width / 2.0D;
        BlockPosition var15 = new BlockPosition(var5);
        var12 = this.a(var1, var2, var3, var4, var6, var7, var8, var9, var10, var11, var12, var15);
        if (var11.contains(PathType.FENCE))
            return PathType.FENCE;
        else {
            PathType var16 = PathType.BLOCKED;
            Iterator var17 = var11.iterator();
            while (var17.hasNext()) {
                PathType var18 = (PathType) var17.next();
                if (var5.a(var18) < 0.0F)
                    return var18;
                if (var5.a(var18) >= var5.a(var16)) {
                    var16 = var18;
                }
            }
            if (var12 == PathType.OPEN && var5.a(var16) == 0.0F)
                return PathType.OPEN;
            else
                return var16;
        }
    }

    public PathType a(IBlockAccess var1, int var2, int var3, int var4, int var5, int var6, int var7, boolean var8,
            boolean var9, EnumSet var10, PathType var11, BlockPosition var12) {
        for (int var13 = 0; var13 < var5; ++var13) {
            for (int var14 = 0; var14 < var6; ++var14) {
                for (int var15 = 0; var15 < var7; ++var15) {
                    int var16 = var13 + var2;
                    int var17 = var14 + var3;
                    int var18 = var15 + var4;
                    PathType var19 = this.a(var1, var16, var17, var18);
                    if (var19 == PathType.DOOR_WOOD_CLOSED && var8 && var9) {
                        var19 = PathType.WALKABLE;
                    }
                    if (var19 == PathType.DOOR_OPEN && !var9) {
                        var19 = PathType.BLOCKED;
                    }
                    if (var19 == PathType.RAIL
                            && !(var1.getType(var12).getBlock() instanceof BlockMinecartTrackAbstract)
                            && !(var1.getType(var12.down()).getBlock() instanceof BlockMinecartTrackAbstract)) {
                        var19 = PathType.FENCE;
                    }
                    if (var13 == 0 && var14 == 0 && var15 == 0) {
                        var11 = var19;
                    }
                    var10.add(var19);
                }
            }
        }
        return var11;
    }

    public PathType a(IBlockAccess var1, int var2, int var3, int var4, PathType var5) {
        if (var5 == PathType.WALKABLE) {
            BlockPosition.PooledBlockPosition var6 = BlockPosition.PooledBlockPosition.r();
            Throwable var7 = null;
            try {
                for (int var8 = -1; var8 <= 1; ++var8) {
                    for (int var9 = -1; var9 <= 1; ++var9) {
                        if (var8 != 0 || var9 != 0) {
                            BlockPosition blockPos;
                            try {
                                blockPos = var6.c(var8 + var2, var3, var9 + var4);
                            } catch (NoSuchMethodError ex) {
                                try {
                                    blockPos = (BlockPosition.PooledBlockPosition) BLOCK_POSITION_B_C.invoke(var6,
                                            var8 + var2, var3, var9 + var4);
                                } catch (Throwable ex2) {
                                    ex2.printStackTrace();
                                    return PathType.BLOCKED;
                                }
                            }
                            Block var10 = var1.getType(blockPos).getBlock();
                            if (var10 == Blocks.CACTUS) {
                                var5 = PathType.DANGER_CACTUS;
                            } else if (var10 == Blocks.FIRE) {
                                var5 = PathType.DANGER_FIRE;
                            }
                        }
                    }
                }
            } catch (Throwable var18) {
                var7 = var18;
                throw var18;
            } finally {
                if (var6 != null) {
                    if (var7 != null) {
                        try {
                            var6.close();
                        } catch (Throwable var17) {
                            var7.addSuppressed(var17);
                        }
                    } else {
                        var6.close();
                    }
                }
            }
        }
        return var5;
    }

    private PathPoint a(int var1, int var2, int var3, int var4, double var5, EnumDirection var7) {
        PathPoint var8 = null;
        BlockPosition var9 = new BlockPosition(var1, var2, var3);
        double var10 = a(this.a, var9);
        if (var10 - var5 > 1.125D)
            return null;
        else {
            PathType var12 = this.a(this.b, var1, var2, var3);
            float var13 = this.b.a(var12);
            double var14 = this.b.width / 2.0D;
            if (var13 >= 0.0F) {
                var8 = this.a(var1, var2, var3);
                var8.m = var12;
                var8.l = Math.max(var8.l, var13);
            }
            if ((var12 != PathType.WALKABLE)) {
                if (var8 == null && var4 > 0 && var12 != PathType.FENCE && var12 != PathType.TRAPDOOR) {
                    var8 = this.a(var1, var2 + 1, var3, var4 - 1, var5, var7);
                    if (var8 != null && (var8.m == PathType.OPEN || var8.m == PathType.WALKABLE)
                            && this.b.width < 1.0F) {
                        double var16 = var1 - var7.getAdjacentX() + 0.5D;
                        double var18 = var3 - var7.getAdjacentZ() + 0.5D;
                        AxisAlignedBB var20 = new AxisAlignedBB(var16 - var14, var2 + 0.001D, var18 - var14,
                                var16 + var14, this.b.length + a(this.a, var9.up()) - 0.002D, var18 + var14);
                        if (!this.b.world.getCubes((Entity) null, var20)) {
                            var8 = null;
                        }
                    }
                }
                if (var12 == PathType.WATER && !this.e()) {
                    if (this.a(this.b, var1, var2 - 1, var3) != PathType.WATER)
                        return var8;
                    while (var2 > 0) {
                        --var2;
                        var12 = this.a(this.b, var1, var2, var3);
                        if (var12 != PathType.WATER)
                            return var8;
                        var8 = this.a(var1, var2, var3);
                        var8.m = var12;
                        var8.l = Math.max(var8.l, this.b.a(var12));
                    }
                }
                if (var12 == PathType.OPEN) {
                    AxisAlignedBB var21 = new AxisAlignedBB(var1 - var14 + 0.5D, var2 + 0.001D, var3 - var14 + 0.5D,
                            var1 + var14 + 0.5D, var2 + this.b.length, var3 + var14 + 0.5D);
                    if (!this.b.world.getCubes((Entity) null, var21))
                        return null;
                    if (this.b.width >= 1.0F) {
                        PathType var17 = this.a(this.b, var1, var2 - 1, var3);
                        if (var17 == PathType.BLOCKED) {
                            var8 = this.a(var1, var2, var3);
                            var8.m = PathType.WALKABLE;
                            var8.l = Math.max(var8.l, var13);
                            return var8;
                        }
                    }
                    int var22 = 0;
                    while (var2 > 0 && var12 == PathType.OPEN) {
                        --var2;
                        if (var22++ >= b.bn())
                            return null;
                        var12 = this.a(this.b, var1, var2, var3);
                        var13 = this.b.a(var12);
                        if (var12 != PathType.OPEN && var13 >= 0.0F) {
                            var8 = this.a(var1, var2, var3);
                            var8.m = var12;
                            var8.l = Math.max(var8.l, var13);
                            break;
                        }
                        if (var13 < 0.0F)
                            return null;
                    }
                }
            }
            return var8;
        }
    }

    @Override
    public int a(PathPoint[] var1, PathPoint var2, PathPoint var3, float var4) {
        int var5 = 0;
        int var6 = 0;
        PathType var7 = this.a(this.b, var2.a, var2.b + 1, var2.c);
        if (this.b.a(var7) >= 0.0F) {
            var6 = MathHelper.d(Math.max(1.0F, this.b.Q));
        }
        double var8 = a(this.a, new BlockPosition(var2.a, var2.b, var2.c));
        PathPoint var10 = this.a(var2.a, var2.b, var2.c + 1, var6, var8, EnumDirection.SOUTH);
        PathPoint var11 = this.a(var2.a - 1, var2.b, var2.c, var6, var8, EnumDirection.WEST);
        PathPoint var12 = this.a(var2.a + 1, var2.b, var2.c, var6, var8, EnumDirection.EAST);
        PathPoint var13 = this.a(var2.a, var2.b, var2.c - 1, var6, var8, EnumDirection.NORTH);
        if (var10 != null && !var10.i && var10.a(var3) < var4) {
            var1[var5++] = var10;
        }
        if (var11 != null && !var11.i && var11.a(var3) < var4) {
            var1[var5++] = var11;
        }
        if (var12 != null && !var12.i && var12.a(var3) < var4) {
            var1[var5++] = var12;
        }
        if (var13 != null && !var13.i && var13.a(var3) < var4) {
            var1[var5++] = var13;
        }
        boolean var14 = var13 == null || var13.m == PathType.OPEN || var13.l != 0.0F;
        boolean var15 = var10 == null || var10.m == PathType.OPEN || var10.l != 0.0F;
        boolean var16 = var12 == null || var12.m == PathType.OPEN || var12.l != 0.0F;
        boolean var17 = var11 == null || var11.m == PathType.OPEN || var11.l != 0.0F;
        PathPoint var18;
        if (var14 && var17) {
            var18 = this.a(var2.a - 1, var2.b, var2.c - 1, var6, var8, EnumDirection.NORTH);
            if (var18 != null && !var18.i && var18.a(var3) < var4) {
                var1[var5++] = var18;
            }
        }
        if (var14 && var16) {
            var18 = this.a(var2.a + 1, var2.b, var2.c - 1, var6, var8, EnumDirection.NORTH);
            if (var18 != null && !var18.i && var18.a(var3) < var4) {
                var1[var5++] = var18;
            }
        }
        if (var15 && var17) {
            var18 = this.a(var2.a - 1, var2.b, var2.c + 1, var6, var8, EnumDirection.SOUTH);
            if (var18 != null && !var18.i && var18.a(var3) < var4) {
                var1[var5++] = var18;
            }
        }
        if (var15 && var16) {
            var18 = this.a(var2.a + 1, var2.b, var2.c + 1, var6, var8, EnumDirection.SOUTH);
            if (var18 != null && !var18.i && var18.a(var3) < var4) {
                var1[var5++] = var18;
            }
        }
        return var5;
    }

    @Override
    public PathPoint b() {
        int var1;
        BlockPosition var2;
        BoundingBox bb = NMSBoundingBox.wrap(this.b.getBoundingBox());
        if (this.e() && this.b.isInWater()) {
            var1 = (int) bb.minY;
            MutableBlockPosition var8 = new MutableBlockPosition(MathHelper.floor(this.b.locX), var1,
                    MathHelper.floor(this.b.locZ));
            for (Block var3 = this.a.getType(var8).getBlock(); var3 == Blocks.WATER; var3 = this.a.getType(var8)
                    .getBlock()) {
                ++var1;
                var8.c(MathHelper.floor(this.b.locX), var1, MathHelper.floor(this.b.locZ));
            }
        } else if (this.b.onGround) {
            var1 = MathHelper.floor(bb.minY + 0.5D);
        } else {
            for (var2 = new BlockPosition(
                    this.b); (this.a.getType(var2).isAir() || this.a.getType(var2).a(this.a, var2, PathMode.LAND))
                            && var2.getY() > 0; var2 = var2.down()) {
            }
            var1 = var2.up().getY();
        }
        var2 = new BlockPosition(this.b);
        PathType var9 = this.a(this.b, var2.getX(), var1, var2.getZ());
        if (this.b.a(var9) < 0.0F) {
            HashSet var4 = Sets.newHashSet();
            bb = NMSBoundingBox.wrap(this.b.getBoundingBox());
            var4.add(new BlockPosition(bb.minX, var1, bb.minZ));
            var4.add(new BlockPosition(bb.minX, var1, bb.maxZ));
            var4.add(new BlockPosition(bb.maxX, var1, bb.minZ));
            var4.add(new BlockPosition(bb.maxX, var1, bb.maxZ));
            Iterator var5 = var4.iterator();
            while (var5.hasNext()) {
                BlockPosition var6 = (BlockPosition) var5.next();
                PathType var7 = this.a(this.b, var6);
                if (this.b.a(var7) >= 0.0F)
                    return this.a(var6.getX(), var6.getY(), var6.getZ());
            }
        }
        return this.a(var2.getX(), var1, var2.getZ());
    }

    protected PathType b(IBlockAccess var1, int var2, int var3, int var4) {
        BlockPosition var5 = new BlockPosition(var2, var3, var4);
        IBlockData var6 = var1.getType(var5);
        Block var7 = var6.getBlock();
        Material var8 = var6.getMaterial();
        if (var6.isAir())
            return PathType.OPEN;
        else if (var7 != Blocks.OAK_TRAPDOOR && var7 != Blocks.IRON_TRAPDOOR && var7 != Blocks.LILY_PAD) {
            if (var7 == Blocks.FIRE)
                return PathType.DAMAGE_FIRE;
            else if (var7 == Blocks.CACTUS)
                return PathType.DAMAGE_CACTUS;
            else if (var7 instanceof BlockDoor && var8 == Material.WOOD && !var6.get(BlockDoor.OPEN).booleanValue())
                return PathType.DOOR_WOOD_CLOSED;
            else if (var7 instanceof BlockDoor && var8 == Material.ORE && !var6.get(BlockDoor.OPEN).booleanValue())
                return PathType.DOOR_IRON_CLOSED;
            else if (var7 instanceof BlockDoor && var6.get(BlockDoor.OPEN).booleanValue())
                return PathType.DOOR_OPEN;
            else if (var7 instanceof BlockMinecartTrackAbstract)
                return PathType.RAIL;
            else if (var7 instanceof BlockFence || var7 instanceof BlockCobbleWall
                    || var7 instanceof BlockFenceGate && !var6.get(BlockFenceGate.OPEN).booleanValue())
                return PathType.FENCE;
            else {
                Fluid var9;
                try {
                    var9 = var1.getFluid(var5);
                } catch (NoSuchMethodError ex) {
                    try {
                        var9 = (Fluid) GET_FLUID.invoke(var1, var5);
                    } catch (Throwable ex2) {
                        ex2.printStackTrace();
                        return PathType.BLOCKED;
                    }
                }
                if (var9.a(TagsFluid.WATER))
                    return PathType.WATER;
                else if (var9.a(TagsFluid.LAVA))
                    return PathType.LAVA;
                else
                    return var6.a(var1, var5, PathMode.LAND) ? PathType.OPEN : PathType.BLOCKED;
            }
        } else
            return PathType.TRAPDOOR;
    }

    public static double a(IBlockAccess var0, BlockPosition var1) {
        BlockPosition var2 = var1.down();
        try {
            VoxelShape var3 = var0.getType(var2).getCollisionShape(var0, var2);
            return var2.getY() + (var3.isEmpty() ? 0.0D : var3.c(EnumAxis.Y));
        } catch (NoSuchMethodError ex) {
            try {
                VoxelShape var3 = (VoxelShape) GET_COLLISION_SHAPE.invoke(var0.getType(var2), var0, var2);
                return var2.getY() + ((Boolean) IS_EMPTY.invoke(var3) ? 0.0D : var3.c(EnumAxis.Y));
            } catch (Exception ex2) {
                ex2.printStackTrace();
                return 0;
            }
        }
    }

    private static final Method BLOCK_POSITION_B_C = NMS.getMethod(BlockPosition.PooledBlockPosition.class, "f", false,
            int.class, int.class, int.class);
    private static final Method GET_COLLISION_SHAPE = NMS.getMethod(IBlockData.class, "h", false, IBlockAccess.class,
            BlockPosition.class);
    private static final Method GET_FLUID = NMS.getMethod(IBlockAccess.class, "b", false, BlockPosition.class);
    private static final Method IS_EMPTY = NMS.getMethod(VoxelShape.class, "b", false);
}
