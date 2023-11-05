package net.citizensnpcs.nms.v1_14_R1.util;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.Sets;

import net.citizensnpcs.nms.v1_14_R1.entity.EntityHumanNPC;
import net.minecraft.server.v1_14_R1.AxisAlignedBB;
import net.minecraft.server.v1_14_R1.Block;
import net.minecraft.server.v1_14_R1.BlockDoor;
import net.minecraft.server.v1_14_R1.BlockFenceGate;
import net.minecraft.server.v1_14_R1.BlockLeaves;
import net.minecraft.server.v1_14_R1.BlockMinecartTrackAbstract;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.BlockPosition.MutableBlockPosition;
import net.minecraft.server.v1_14_R1.BlockPosition.PooledBlockPosition;
import net.minecraft.server.v1_14_R1.Blocks;
import net.minecraft.server.v1_14_R1.EntityInsentient;
import net.minecraft.server.v1_14_R1.EnumDirection;
import net.minecraft.server.v1_14_R1.EnumDirection.EnumAxis;
import net.minecraft.server.v1_14_R1.Fluid;
import net.minecraft.server.v1_14_R1.FluidTypes;
import net.minecraft.server.v1_14_R1.IBlockAccess;
import net.minecraft.server.v1_14_R1.IBlockData;
import net.minecraft.server.v1_14_R1.Material;
import net.minecraft.server.v1_14_R1.MathHelper;
import net.minecraft.server.v1_14_R1.PathDestination;
import net.minecraft.server.v1_14_R1.PathMode;
import net.minecraft.server.v1_14_R1.PathPoint;
import net.minecraft.server.v1_14_R1.PathType;
import net.minecraft.server.v1_14_R1.TagsBlock;
import net.minecraft.server.v1_14_R1.TagsFluid;
import net.minecraft.server.v1_14_R1.VoxelShape;

public class PlayerPathfinderNormal extends PlayerPathfinderAbstract {
    protected float j;

    @Override
    public void a() {
        this.b.a(PathType.WATER, this.j);
        super.a();
    }

    @Override
    public PathDestination a(double var0, double var2, double var4) {
        return new PathDestination(this.a(MathHelper.floor(var0), MathHelper.floor(var2), MathHelper.floor(var4)));
    }

    private PathType a(EntityHumanNPC var0, BlockPosition var1) {
        return this.a(var0, var1.getX(), var1.getY(), var1.getZ());
    }

    private PathType a(EntityHumanNPC var0, int var1, int var2, int var3) {
        return this.a(this.a, var1, var2, var3, var0, this.d, this.e, this.f, this.d(), this.c());
    }

    private PathType a(EntityInsentient var0, BlockPosition var1) {
        return this.a(var0, var1.getX(), var1.getY(), var1.getZ());
    }

    private PathType a(EntityInsentient var0, int var1, int var2, int var3) {
        return this.a(this.a, var1, var2, var3, var0, this.d, this.e, this.f, this.d(), this.c());
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
            var4 = PathType.FENCE;
        }
        if (var4 == PathType.LEAVES) {
            var4 = PathType.BLOCKED;
        }
        return var4;
    }

    @Override
    public PathType a(IBlockAccess var0, int var1, int var2, int var3) {
        PathType var4 = this.b(var0, var1, var2, var3);
        if (var4 == PathType.OPEN && var2 >= 1) {
            Block var5 = var0.getType(new BlockPosition(var1, var2 - 1, var3)).getBlock();
            PathType var6 = this.b(var0, var1, var2 - 1, var3);
            var4 = var6 != PathType.WALKABLE && var6 != PathType.OPEN && var6 != PathType.WATER && var6 != PathType.LAVA
                    ? PathType.WALKABLE
                    : PathType.OPEN;
            if (var6 == PathType.DAMAGE_FIRE || var5 == Blocks.MAGMA_BLOCK || var5 == Blocks.CAMPFIRE) {
                var4 = PathType.DAMAGE_FIRE;
            }
            if (var6 == PathType.DAMAGE_CACTUS) {
                var4 = PathType.DAMAGE_CACTUS;
            }
            if (var6 == PathType.DAMAGE_OTHER) {
                var4 = PathType.DAMAGE_OTHER;
            }
        }
        var4 = this.a(var0, var1, var2, var3, var4);
        return var4;
    }

    public PathType a(IBlockAccess var0, int var1, int var2, int var3, EntityHumanNPC var4, int var5, int var6,
            int var7, boolean var8, boolean var9) {
        EnumSet var10 = EnumSet.noneOf(PathType.class);
        PathType var11 = PathType.BLOCKED;
        double var12 = var4.getWidth() / 2.0D;
        BlockPosition var14 = new BlockPosition(var4);
        var11 = this.a(var0, var1, var2, var3, var5, var6, var7, var8, var9, var10, var11, var14);
        if (var10.contains(PathType.FENCE))
            return PathType.FENCE;
        else {
            PathType var15 = PathType.BLOCKED;
            Iterator var17 = var10.iterator();
            while (var17.hasNext()) {
                PathType var16 = (PathType) var17.next();
                if (var4.a(var16) < 0.0F)
                    return var16;
                if (var4.a(var16) >= var4.a(var15)) {
                    var15 = var16;
                }
            }
            if (var11 == PathType.OPEN && var4.a(var15) == 0.0F)
                return PathType.OPEN;
            else
                return var15;
        }
    }

    @Override
    public PathType a(IBlockAccess var0, int var1, int var2, int var3, EntityInsentient var4, int var5, int var6,
            int var7, boolean var8, boolean var9) {
        EnumSet var10 = EnumSet.noneOf(PathType.class);
        PathType var11 = PathType.BLOCKED;
        double var12 = var4.getWidth() / 2.0D;
        BlockPosition var14 = new BlockPosition(var4);
        var11 = this.a(var0, var1, var2, var3, var5, var6, var7, var8, var9, var10, var11, var14);
        if (var10.contains(PathType.FENCE))
            return PathType.FENCE;
        else {
            PathType var15 = PathType.BLOCKED;
            Iterator var17 = var10.iterator();
            while (var17.hasNext()) {
                PathType var16 = (PathType) var17.next();
                if (var4.a(var16) < 0.0F)
                    return var16;
                if (var4.a(var16) >= var4.a(var15)) {
                    var15 = var16;
                }
            }
            if (var11 == PathType.OPEN && var4.a(var15) == 0.0F)
                return PathType.OPEN;
            else
                return var15;
        }
    }

    public PathType a(IBlockAccess var0, int var1, int var2, int var3, int var4, int var5, int var6, boolean var7,
            boolean var8, EnumSet var9, PathType var10, BlockPosition var11) {
        for (int var12 = 0; var12 < var4; ++var12) {
            for (int var13 = 0; var13 < var5; ++var13) {
                for (int var14 = 0; var14 < var6; ++var14) {
                    int var15 = var12 + var1;
                    int var16 = var13 + var2;
                    int var17 = var14 + var3;
                    PathType var18 = this.a(var0, var15, var16, var17);
                    var18 = this.a(var0, var7, var8, var11, var18);
                    if (var12 == 0 && var13 == 0 && var14 == 0) {
                        var10 = var18;
                    }
                    var9.add(var18);
                }
            }
        }
        return var10;
    }

    public PathType a(IBlockAccess var0, int var1, int var2, int var3, PathType var4) {
        if (var4 == PathType.WALKABLE) {
            PooledBlockPosition var5 = PooledBlockPosition.r();
            Throwable tt = null;
            try {
                for (int var7 = -1; var7 <= 1; ++var7) {
                    for (int var8 = -1; var8 <= 1; ++var8) {
                        if (var7 != 0 || var8 != 0) {
                            Block var9 = var0.getType(var5.d(var7 + var1, var2, var8 + var3)).getBlock();
                            if (var9 == Blocks.CACTUS) {
                                var4 = PathType.DANGER_CACTUS;
                            } else if (var9 == Blocks.FIRE) {
                                var4 = PathType.DANGER_FIRE;
                            } else if (var9 == Blocks.SWEET_BERRY_BUSH) {
                                var4 = PathType.DANGER_OTHER;
                            }
                        }
                    }
                }
            } catch (Throwable var18) {
                tt = var18;
                throw var18;
            } finally {
                if (var5 != null) {
                    if (tt != null) {
                        try {
                            var5.close();
                        } catch (Throwable var17) {
                            tt.addSuppressed(var17);
                        }
                    } else {
                        var5.close();
                    }
                }
            }
        }
        return var4;
    }

    private PathPoint a(int var0, int var1, int var2, int var3, double var4, EnumDirection var6) {
        PathPoint var7 = null;
        BlockPosition var8 = new BlockPosition(var0, var1, var2);
        double var9 = a(this.a, var8);
        if (var9 - var4 > 1.125D)
            return null;
        else {
            PathType var11 = this.a(this.b, var0, var1, var2);
            float var12 = this.b.a(var11);
            double var13 = this.b.getWidth() / 2.0D;
            if (var12 >= 0.0F) {
                var7 = this.a(var0, var1, var2);
                var7.l = var11;
                var7.k = Math.max(var7.k, var12);
            }
            if ((var11 != PathType.WALKABLE)) {
                if ((var7 == null || var7.k < 0.0F) && var3 > 0 && var11 != PathType.FENCE
                        && var11 != PathType.TRAPDOOR) {
                    var7 = this.a(var0, var1 + 1, var2, var3 - 1, var4, var6);
                    if (var7 != null && (var7.l == PathType.OPEN || var7.l == PathType.WALKABLE)
                            && this.b.getWidth() < 1.0F) {
                        double var15 = var0 - var6.getAdjacentX() + 0.5D;
                        double var17 = var2 - var6.getAdjacentZ() + 0.5D;
                        AxisAlignedBB var19 = new AxisAlignedBB(var15 - var13,
                                a(this.a, new BlockPosition(var15, var1 + 1, var17)) + 0.001D, var17 - var13,
                                var15 + var13,
                                this.b.getHeight() + a(this.a, new BlockPosition(var7.a, var7.b, var7.c)) - 0.002D,
                                var17 + var13);
                        if (!this.a.getCubes(this.b, var19)) {
                            var7 = null;
                        }
                    }
                }
                if (var11 == PathType.WATER && !this.e()) {
                    if (this.a(this.b, var0, var1 - 1, var2) != PathType.WATER)
                        return var7;
                    while (var1 > 0) {
                        --var1;
                        var11 = this.a(this.b, var0, var1, var2);
                        if (var11 != PathType.WATER)
                            return var7;
                        var7 = this.a(var0, var1, var2);
                        var7.l = var11;
                        var7.k = Math.max(var7.k, this.b.a(var11));
                    }
                }
                if (var11 == PathType.OPEN) {
                    AxisAlignedBB var15 = new AxisAlignedBB(var0 - var13 + 0.5D, var1 + 0.001D, var2 - var13 + 0.5D,
                            var0 + var13 + 0.5D, var1 + this.b.getHeight(), var2 + var13 + 0.5D);
                    if (!this.a.getCubes(this.b, var15))
                        return null;
                    if (this.b.getWidth() >= 1.0F) {
                        PathType var16 = this.a(this.b, var0, var1 - 1, var2);
                        if (var16 == PathType.BLOCKED) {
                            var7 = this.a(var0, var1, var2);
                            var7.l = PathType.WALKABLE;
                            var7.k = Math.max(var7.k, var12);
                            return var7;
                        }
                    }
                    int var16 = 0;
                    int var17 = var1;
                    while (var11 == PathType.OPEN) {
                        --var1;
                        PathPoint var18;
                        if (var1 < 0) {
                            var18 = this.a(var0, var17, var2);
                            var18.l = PathType.BLOCKED;
                            var18.k = -1.0F;
                            return var18;
                        }
                        var18 = this.a(var0, var1, var2);
                        if (var16++ >= b.bv()) {
                            var18.l = PathType.BLOCKED;
                            var18.k = -1.0F;
                            return var18;
                        }
                        var11 = this.a(this.b, var0, var1, var2);
                        var12 = this.b.a(var11);
                        if (var11 != PathType.OPEN && var12 >= 0.0F) {
                            var7 = var18;
                            var18.l = var11;
                            var18.k = Math.max(var18.k, var12);
                            break;
                        }
                        if (var12 < 0.0F) {
                            var18.l = PathType.BLOCKED;
                            var18.k = -1.0F;
                            return var18;
                        }
                    }
                }
            }
            return var7;
        }
    }

    private boolean a(PathPoint var0, PathPoint var1, PathPoint var2, PathPoint var3) {
        if (((var3 == null) || (var2 == null) || (var1 == null)) || var3.i)
            return false;
        else if (var2.b <= var0.b && var1.b <= var0.b)
            return var3.k >= 0.0F && (var2.b < var0.b || var2.k >= 0.0F) && (var1.b < var0.b || var1.k >= 0.0F);
        else
            return false;
    }

    @Override
    public int a(PathPoint[] var0, PathPoint var1) {
        int var2 = 0;
        int var3 = 0;
        PathType var4 = this.a(this.b, var1.a, var1.b + 1, var1.c);
        if (this.b.a(var4) >= 0.0F) {
            var3 = MathHelper.d(Math.max(1.0F, this.b.K));
        }
        double var5 = a(this.a, new BlockPosition(var1.a, var1.b, var1.c));
        PathPoint var7 = this.a(var1.a, var1.b, var1.c + 1, var3, var5, EnumDirection.SOUTH);
        if (var7 != null && !var7.i && var7.k >= 0.0F) {
            var0[var2++] = var7;
        }
        PathPoint var8 = this.a(var1.a - 1, var1.b, var1.c, var3, var5, EnumDirection.WEST);
        if (var8 != null && !var8.i && var8.k >= 0.0F) {
            var0[var2++] = var8;
        }
        PathPoint var9 = this.a(var1.a + 1, var1.b, var1.c, var3, var5, EnumDirection.EAST);
        if (var9 != null && !var9.i && var9.k >= 0.0F) {
            var0[var2++] = var9;
        }
        PathPoint var10 = this.a(var1.a, var1.b, var1.c - 1, var3, var5, EnumDirection.NORTH);
        if (var10 != null && !var10.i && var10.k >= 0.0F) {
            var0[var2++] = var10;
        }
        PathPoint var11 = this.a(var1.a - 1, var1.b, var1.c - 1, var3, var5, EnumDirection.NORTH);
        if (this.a(var1, var8, var10, var11)) {
            var0[var2++] = var11;
        }
        PathPoint var12 = this.a(var1.a + 1, var1.b, var1.c - 1, var3, var5, EnumDirection.NORTH);
        if (this.a(var1, var9, var10, var12)) {
            var0[var2++] = var12;
        }
        PathPoint var13 = this.a(var1.a - 1, var1.b, var1.c + 1, var3, var5, EnumDirection.SOUTH);
        if (this.a(var1, var8, var7, var13)) {
            var0[var2++] = var13;
        }
        PathPoint var14 = this.a(var1.a + 1, var1.b, var1.c + 1, var3, var5, EnumDirection.SOUTH);
        if (this.a(var1, var9, var7, var14)) {
            var0[var2++] = var14;
        }
        return var2;
    }

    @Override
    public PathPoint b() {
        int var0;
        BlockPosition var1;
        if (this.e() && this.b.isInWater()) {
            var0 = MathHelper.floor(this.b.getBoundingBox().minY);
            var1 = new MutableBlockPosition(this.b.locX, var0, this.b.locZ);
            for (IBlockData var2 = this.a.getType(var1); var2.getBlock() == Blocks.WATER
                    || var2.p() == FluidTypes.WATER.a(false); var2 = this.a.getType(var1)) {
                ++var0;
                ((MutableBlockPosition) var1).c(this.b.locX, var0, this.b.locZ);
            }
            --var0;
        } else if (this.b.onGround) {
            var0 = MathHelper.floor(this.b.getBoundingBox().minY + 0.5D);
        } else {
            for (var1 = new BlockPosition(
                    this.b); (this.a.getType(var1).isAir() || this.a.getType(var1).a(this.a, var1, PathMode.LAND))
                            && var1.getY() > 0; var1 = var1.down()) {
            }
            var0 = var1.up().getY();
        }
        var1 = new BlockPosition(this.b);
        PathType var2 = this.a(this.b, var1.getX(), var0, var1.getZ());
        if (this.b.a(var2) < 0.0F) {
            Set var3 = Sets.newHashSet();
            var3.add(new BlockPosition(this.b.getBoundingBox().minX, var0, this.b.getBoundingBox().minZ));
            var3.add(new BlockPosition(this.b.getBoundingBox().minX, var0, this.b.getBoundingBox().maxZ));
            var3.add(new BlockPosition(this.b.getBoundingBox().maxX, var0, this.b.getBoundingBox().minZ));
            var3.add(new BlockPosition(this.b.getBoundingBox().maxX, var0, this.b.getBoundingBox().maxZ));
            Iterator var5 = var3.iterator();
            while (var5.hasNext()) {
                BlockPosition var4 = (BlockPosition) var5.next();
                PathType var6 = this.a(this.b, var4);
                if (this.b.a(var6) >= 0.0F)
                    return this.a(var4.getX(), var4.getY(), var4.getZ());
            }
        }
        return this.a(var1.getX(), var0, var1.getZ());
    }

    protected PathType b(IBlockAccess var0, int var1, int var2, int var3) {
        BlockPosition var4 = new BlockPosition(var1, var2, var3);
        IBlockData var5 = var0.getType(var4);
        Block var6 = var5.getBlock();
        Material var7 = var5.getMaterial();
        if (var5.isAir())
            return PathType.OPEN;
        else if (!var6.a(TagsBlock.TRAPDOORS) && var6 != Blocks.LILY_PAD) {
            if (var6 == Blocks.FIRE)
                return PathType.DAMAGE_FIRE;
            else if (var6 == Blocks.CACTUS)
                return PathType.DAMAGE_CACTUS;
            else if (var6 == Blocks.SWEET_BERRY_BUSH)
                return PathType.DAMAGE_OTHER;
            else if (var6 instanceof BlockDoor && var7 == Material.WOOD && !(Boolean) var5.get(BlockDoor.OPEN))
                return PathType.DOOR_WOOD_CLOSED;
            else if (var6 instanceof BlockDoor && var7 == Material.ORE && !(Boolean) var5.get(BlockDoor.OPEN))
                return PathType.DOOR_IRON_CLOSED;
            else if (var6 instanceof BlockDoor && var5.get(BlockDoor.OPEN))
                return PathType.DOOR_OPEN;
            else if (var6 instanceof BlockMinecartTrackAbstract)
                return PathType.RAIL;
            else if (var6 instanceof BlockLeaves)
                return PathType.LEAVES;
            else if (!var6.a(TagsBlock.FENCES) && !var6.a(TagsBlock.WALLS)
                    && (!(var6 instanceof BlockFenceGate) || var5.get(BlockFenceGate.OPEN))) {
                Fluid var8 = var0.getFluid(var4);
                if (var8.a(TagsFluid.WATER))
                    return PathType.WATER;
                else if (var8.a(TagsFluid.LAVA))
                    return PathType.LAVA;
                else
                    return var5.a(var0, var4, PathMode.LAND) ? PathType.OPEN : PathType.BLOCKED;
            } else
                return PathType.FENCE;
        } else
            return PathType.TRAPDOOR;
    }

    public static double a(IBlockAccess var0, BlockPosition var1) {
        BlockPosition var2 = var1.down();
        VoxelShape var3 = var0.getType(var2).getCollisionShape(var0, var2);
        return var2.getY() + (var3.isEmpty() ? 0.0D : var3.c(EnumAxis.Y));
    }
}
