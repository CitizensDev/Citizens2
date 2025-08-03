package net.citizensnpcs.nms.v1_12_R1.util;

import java.util.EnumSet;
import java.util.HashSet;

import com.google.common.collect.Sets;

import net.citizensnpcs.nms.v1_12_R1.entity.EntityHumanNPC;
import net.minecraft.server.v1_12_R1.AxisAlignedBB;
import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.BlockCobbleWall;
import net.minecraft.server.v1_12_R1.BlockDoor;
import net.minecraft.server.v1_12_R1.BlockFence;
import net.minecraft.server.v1_12_R1.BlockFenceGate;
import net.minecraft.server.v1_12_R1.BlockMinecartTrackAbstract;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.BlockPosition.MutableBlockPosition;
import net.minecraft.server.v1_12_R1.Blocks;
import net.minecraft.server.v1_12_R1.EntityInsentient;
import net.minecraft.server.v1_12_R1.EnumDirection;
import net.minecraft.server.v1_12_R1.IBlockAccess;
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.Material;
import net.minecraft.server.v1_12_R1.MathHelper;
import net.minecraft.server.v1_12_R1.PathPoint;
import net.minecraft.server.v1_12_R1.PathType;

public class PlayerPathfinderNormal extends PlayerPathfinderAbstract {
    protected float j;

    @Override
    public void a() {
        this.b.a(PathType.WATER, this.j);
        this.a = null;
        this.b = null;
    }

    @Override
    public PathPoint a(double paramDouble1, double paramDouble2, double paramDouble3) {
        return a(MathHelper.floor(paramDouble1), MathHelper.floor(paramDouble2), MathHelper.floor(paramDouble3));
    }

    @Override
    public void a(IBlockAccess paramIBlockAccess, EntityHumanNPC paramEntityInsentient) {
        super.a(paramIBlockAccess, paramEntityInsentient);
        this.j = paramEntityInsentient.a(PathType.WATER);
    }

    @Override
    public PathType a(IBlockAccess paramIBlockAccess, int paramInt1, int paramInt2, int paramInt3) {
        PathType localPathType1 = pb(paramIBlockAccess, paramInt1, paramInt2, paramInt3);
        if (localPathType1 == PathType.OPEN && paramInt2 >= 1) {
            Block localBlock = paramIBlockAccess.getType(new BlockPosition(paramInt1, paramInt2 - 1, paramInt3))
                    .getBlock();
            PathType localPathType2 = pb(paramIBlockAccess, paramInt1, paramInt2 - 1, paramInt3);
            localPathType1 = localPathType2 == PathType.WALKABLE || localPathType2 == PathType.OPEN
                    || localPathType2 == PathType.WATER || localPathType2 == PathType.LAVA ? PathType.OPEN
                            : PathType.WALKABLE;
            if (localPathType2 == PathType.DAMAGE_FIRE || localBlock == Blocks.df) {
                localPathType1 = PathType.DAMAGE_FIRE;
            }
            if (localPathType2 == PathType.DAMAGE_CACTUS) {
                localPathType1 = PathType.DAMAGE_CACTUS;
            }
        }
        localPathType1 = a(paramIBlockAccess, paramInt1, paramInt2, paramInt3, localPathType1);
        return localPathType1;
    }

    public PathType a(IBlockAccess paramIBlockAccess, int paramInt1, int paramInt2, int paramInt3,
            EntityHumanNPC paramEntityInsentient, int paramInt4, int paramInt5, int paramInt6, boolean paramBoolean1,
            boolean paramBoolean2) {
        EnumSet<PathType> localEnumSet = EnumSet.noneOf(PathType.class);
        PathType localPathType1 = PathType.BLOCKED;
        BlockPosition localBlockPosition = new BlockPosition(paramEntityInsentient);
        localPathType1 = a(paramIBlockAccess, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6,
                paramBoolean1, paramBoolean2, localEnumSet, localPathType1, localBlockPosition);
        if (localEnumSet.contains(PathType.FENCE))
            return PathType.FENCE;
        Object localObject = PathType.BLOCKED;
        for (PathType localPathType2 : localEnumSet) {
            if (paramEntityInsentient.a(localPathType2) < 0.0F)
                return localPathType2;
            if (paramEntityInsentient.a(localPathType2) >= paramEntityInsentient.a((PathType) localObject)) {
                localObject = localPathType2;
            }
        }
        if (localPathType1 == PathType.OPEN && paramEntityInsentient.a((PathType) localObject) == 0.0F)
            return PathType.OPEN;
        return (PathType) localObject;
    }

    @Override
    public PathType a(IBlockAccess paramIBlockAccess, int paramInt1, int paramInt2, int paramInt3,
            EntityInsentient paramEntityInsentient, int paramInt4, int paramInt5, int paramInt6, boolean paramBoolean1,
            boolean paramBoolean2) {
        EnumSet<PathType> localEnumSet = EnumSet.noneOf(PathType.class);
        PathType localPathType1 = PathType.BLOCKED;
        BlockPosition localBlockPosition = new BlockPosition(paramEntityInsentient);
        localPathType1 = a(paramIBlockAccess, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6,
                paramBoolean1, paramBoolean2, localEnumSet, localPathType1, localBlockPosition);
        if (localEnumSet.contains(PathType.FENCE))
            return PathType.FENCE;
        Object localObject = PathType.BLOCKED;
        for (PathType localPathType2 : localEnumSet) {
            if (paramEntityInsentient.a(localPathType2) < 0.0F)
                return localPathType2;
            if (paramEntityInsentient.a(localPathType2) >= paramEntityInsentient.a((PathType) localObject)) {
                localObject = localPathType2;
            }
        }
        if (localPathType1 == PathType.OPEN && paramEntityInsentient.a((PathType) localObject) == 0.0F)
            return PathType.OPEN;
        return (PathType) localObject;
    }

    public PathType a(IBlockAccess paramIBlockAccess, int paramInt1, int paramInt2, int paramInt3, int paramInt4,
            int paramInt5, int paramInt6, boolean paramBoolean1, boolean paramBoolean2, EnumSet<PathType> paramEnumSet,
            PathType paramPathType, BlockPosition paramBlockPosition) {
        for (int i = 0; i < paramInt4; i++) {
            for (int k = 0; k < paramInt5; k++) {
                for (int m = 0; m < paramInt6; m++) {
                    int n = i + paramInt1;
                    int i1 = k + paramInt2;
                    int i2 = m + paramInt3;
                    PathType localPathType = a(paramIBlockAccess, n, i1, i2);
                    if (localPathType == PathType.DOOR_WOOD_CLOSED && paramBoolean1 && paramBoolean2) {
                        localPathType = PathType.WALKABLE;
                    }
                    if (localPathType == PathType.DOOR_OPEN && !paramBoolean2) {
                        localPathType = PathType.BLOCKED;
                    }
                    if (localPathType == PathType.RAIL
                            && !(paramIBlockAccess.getType(paramBlockPosition)
                                    .getBlock() instanceof BlockMinecartTrackAbstract)
                            && !(paramIBlockAccess.getType(paramBlockPosition.down())
                                    .getBlock() instanceof BlockMinecartTrackAbstract)) {
                        localPathType = PathType.FENCE;
                    }
                    if (i == 0 && k == 0 && m == 0) {
                        paramPathType = localPathType;
                    }
                    paramEnumSet.add(localPathType);
                }
            }
        }
        return paramPathType;
    }

    public PathType a(IBlockAccess paramIBlockAccess, int paramInt1, int paramInt2, int paramInt3,
            PathType paramPathType) {
        BlockPosition.PooledBlockPosition localPooledBlockPosition = BlockPosition.PooledBlockPosition.s();
        if (paramPathType == PathType.WALKABLE) {
            for (int i = -1; i <= 1; i++) {
                for (int k = -1; k <= 1; k++) {
                    if (i != 0 || k != 0) {
                        Block localBlock = paramIBlockAccess
                                .getType(localPooledBlockPosition.f(i + paramInt1, paramInt2, k + paramInt3))
                                .getBlock();
                        if (localBlock == Blocks.CACTUS) {
                            paramPathType = PathType.DANGER_CACTUS;
                        } else if (localBlock == Blocks.FIRE) {
                            paramPathType = PathType.DANGER_FIRE;
                        }
                    }
                }
            }
        }
        localPooledBlockPosition.t();
        return paramPathType;
    }

    @Override
    public int a(PathPoint[] paramArrayOfPathPoint, PathPoint paramPathPoint1, PathPoint paramPathPoint2,
            float paramFloat) {
        int i = 0;
        int k = 0;
        PathType localPathType = pa(this.b, paramPathPoint1.a, paramPathPoint1.b + 1, paramPathPoint1.c);
        if (this.b.a(localPathType) >= 0.0F) {
            k = MathHelper.d(Math.max(1.0F, this.b.P));
        }
        BlockPosition localBlockPosition = new BlockPosition(paramPathPoint1.a, paramPathPoint1.b, paramPathPoint1.c)
                .down();
        double d = paramPathPoint1.b - (1.0D - this.a.getType(localBlockPosition).e(this.a, localBlockPosition).e);
        PathPoint localPathPoint1 = pa(paramPathPoint1.a, paramPathPoint1.b, paramPathPoint1.c + 1, k, d,
                EnumDirection.SOUTH);
        PathPoint localPathPoint2 = pa(paramPathPoint1.a - 1, paramPathPoint1.b, paramPathPoint1.c, k, d,
                EnumDirection.WEST);
        PathPoint localPathPoint3 = pa(paramPathPoint1.a + 1, paramPathPoint1.b, paramPathPoint1.c, k, d,
                EnumDirection.EAST);
        PathPoint localPathPoint4 = pa(paramPathPoint1.a, paramPathPoint1.b, paramPathPoint1.c - 1, k, d,
                EnumDirection.NORTH);
        if (localPathPoint1 != null && !localPathPoint1.i && localPathPoint1.a(paramPathPoint2) < paramFloat) {
            paramArrayOfPathPoint[i++] = localPathPoint1;
        }
        if (localPathPoint2 != null && !localPathPoint2.i && localPathPoint2.a(paramPathPoint2) < paramFloat) {
            paramArrayOfPathPoint[i++] = localPathPoint2;
        }
        if (localPathPoint3 != null && !localPathPoint3.i && localPathPoint3.a(paramPathPoint2) < paramFloat) {
            paramArrayOfPathPoint[i++] = localPathPoint3;
        }
        if (localPathPoint4 != null && !localPathPoint4.i && localPathPoint4.a(paramPathPoint2) < paramFloat) {
            paramArrayOfPathPoint[i++] = localPathPoint4;
        }
        int m = localPathPoint4 == null || localPathPoint4.m == PathType.OPEN || localPathPoint4.l != 0.0F ? 1 : 0;
        int n = localPathPoint1 == null || localPathPoint1.m == PathType.OPEN || localPathPoint1.l != 0.0F ? 1 : 0;
        int i1 = localPathPoint3 == null || localPathPoint3.m == PathType.OPEN || localPathPoint3.l != 0.0F ? 1 : 0;
        int i2 = localPathPoint2 == null || localPathPoint2.m == PathType.OPEN || localPathPoint2.l != 0.0F ? 1 : 0;
        PathPoint localPathPoint5;
        if (m != 0 && i2 != 0) {
            localPathPoint5 = pa(paramPathPoint1.a - 1, paramPathPoint1.b, paramPathPoint1.c - 1, k, d,
                    EnumDirection.NORTH);
            if (localPathPoint5 != null && !localPathPoint5.i && localPathPoint5.a(paramPathPoint2) < paramFloat) {
                paramArrayOfPathPoint[i++] = localPathPoint5;
            }
        }
        if (m != 0 && i1 != 0) {
            localPathPoint5 = pa(paramPathPoint1.a + 1, paramPathPoint1.b, paramPathPoint1.c - 1, k, d,
                    EnumDirection.NORTH);
            if (localPathPoint5 != null && !localPathPoint5.i && localPathPoint5.a(paramPathPoint2) < paramFloat) {
                paramArrayOfPathPoint[i++] = localPathPoint5;
            }
        }
        if (n != 0 && i2 != 0) {
            localPathPoint5 = pa(paramPathPoint1.a - 1, paramPathPoint1.b, paramPathPoint1.c + 1, k, d,
                    EnumDirection.SOUTH);
            if (localPathPoint5 != null && !localPathPoint5.i && localPathPoint5.a(paramPathPoint2) < paramFloat) {
                paramArrayOfPathPoint[i++] = localPathPoint5;
            }
        }
        if (n != 0 && i1 != 0) {
            localPathPoint5 = pa(paramPathPoint1.a + 1, paramPathPoint1.b, paramPathPoint1.c + 1, k, d,
                    EnumDirection.SOUTH);
            if (localPathPoint5 != null && !localPathPoint5.i && localPathPoint5.a(paramPathPoint2) < paramFloat) {
                paramArrayOfPathPoint[i++] = localPathPoint5;
            }
        }
        return i;
    }

    @Override
    public PathPoint b() {
        int i;
        BlockPosition localObject1;
        if (e() && this.b.isInWater()) {
            i = (int) this.b.getBoundingBox().b;
            localObject1 = new BlockPosition.MutableBlockPosition(MathHelper.floor(this.b.locX), i,
                    MathHelper.floor(this.b.locZ));
            Block localObject2 = this.a.getType(localObject1).getBlock();
            while (localObject2 == Blocks.FLOWING_WATER || localObject2 == Blocks.WATER) {
                i++;
                ((MutableBlockPosition) localObject1).c(MathHelper.floor(this.b.locX), i,
                        MathHelper.floor(this.b.locZ));
                localObject2 = this.a.getType(localObject1).getBlock();
            }
        } else if (this.b.onGround) {
            i = MathHelper.floor(this.b.getBoundingBox().b + 0.5D);
        } else {
            localObject1 = new BlockPosition(this.b);
            while ((this.a.getType(localObject1).getMaterial() == Material.AIR
                    || this.a.getType(localObject1).getBlock().b(this.a, localObject1)) && localObject1.getY() > 0) {
                localObject1 = localObject1.down();
            }
            i = localObject1.up().getY();
        }
        localObject1 = new BlockPosition(this.b);
        Object localObject2 = pa(this.b, localObject1.getX(), i, localObject1.getZ());
        if (this.b.a((PathType) localObject2) < 0.0F) {
            HashSet<BlockPosition> localHashSet = Sets.newHashSet();
            localHashSet.add(new BlockPosition(this.b.getBoundingBox().a, i, this.b.getBoundingBox().c));
            localHashSet.add(new BlockPosition(this.b.getBoundingBox().a, i, this.b.getBoundingBox().f));
            localHashSet.add(new BlockPosition(this.b.getBoundingBox().d, i, this.b.getBoundingBox().c));
            localHashSet.add(new BlockPosition(this.b.getBoundingBox().d, i, this.b.getBoundingBox().f));
            for (BlockPosition localBlockPosition : localHashSet) {
                PathType localPathType = pa(this.b, localBlockPosition);
                if (this.b.a(localPathType) >= 0.0F)
                    return a(localBlockPosition.getX(), localBlockPosition.getY(), localBlockPosition.getZ());
            }
        }
        return a(localObject1.getX(), i, localObject1.getZ());
    }

    private PathType pa(EntityHumanNPC paramEntityInsentient, BlockPosition paramBlockPosition) {
        return pa(paramEntityInsentient, paramBlockPosition.getX(), paramBlockPosition.getY(),
                paramBlockPosition.getZ());
    }

    private PathType pa(EntityHumanNPC paramEntityInsentient, int paramInt1, int paramInt2, int paramInt3) {
        return a(this.a, paramInt1, paramInt2, paramInt3, paramEntityInsentient, this.d, this.e, this.f, d(), c());
    }

    private PathPoint pa(int paramInt1, int paramInt2, int paramInt3, int paramInt4, double paramDouble,
            EnumDirection paramEnumDirection) {
        PathPoint localPathPoint = null;
        BlockPosition localBlockPosition1 = new BlockPosition(paramInt1, paramInt2, paramInt3);
        BlockPosition localBlockPosition2 = localBlockPosition1.down();
        double d1 = paramInt2 - (1.0D - this.a.getType(localBlockPosition2).e(this.a, localBlockPosition2).e);
        if (d1 - paramDouble > 1.125D)
            return null;
        PathType localPathType1 = pa(this.b, paramInt1, paramInt2, paramInt3);
        float f = this.b.a(localPathType1);
        double d2 = this.b.width / 2.0D;
        if (f >= 0.0F) {
            localPathPoint = a(paramInt1, paramInt2, paramInt3);
            localPathPoint.m = localPathType1;
            localPathPoint.l = Math.max(localPathPoint.l, f);
        }
        if (localPathType1 == PathType.WALKABLE)
            return localPathPoint;
        if (localPathPoint == null && paramInt4 > 0 && localPathType1 != PathType.FENCE
                && localPathType1 != PathType.TRAPDOOR) {
            localPathPoint = pa(paramInt1, paramInt2 + 1, paramInt3, paramInt4 - 1, paramDouble, paramEnumDirection);
            if (localPathPoint != null && (localPathPoint.m == PathType.OPEN || localPathPoint.m == PathType.WALKABLE)
                    && this.b.width < 1.0F) {
                double d3 = paramInt1 - paramEnumDirection.getAdjacentX() + 0.5D;
                double d4 = paramInt3 - paramEnumDirection.getAdjacentZ() + 0.5D;
                AxisAlignedBB localAxisAlignedBB1 = new AxisAlignedBB(d3 - d2, paramInt2 + 0.001D, d4 - d2, d3 + d2,
                        paramInt2 + this.b.length, d4 + d2);
                AxisAlignedBB localAxisAlignedBB2 = this.a.getType(localBlockPosition1).e(this.a, localBlockPosition1);
                AxisAlignedBB localAxisAlignedBB3 = localAxisAlignedBB1.b(0.0D, localAxisAlignedBB2.e - 0.002D, 0.0D);
                if (this.b.world.a(localAxisAlignedBB3)) {
                    localPathPoint = null;
                }
            }
        }
        if (localPathType1 == PathType.OPEN) {
            AxisAlignedBB localAxisAlignedBB4 = new AxisAlignedBB(paramInt1 - d2 + 0.5D, paramInt2 + 0.001D,
                    paramInt3 - d2 + 0.5D, paramInt1 + d2 + 0.5D, paramInt2 + this.b.length, paramInt3 + d2 + 0.5D);
            if (this.b.world.a(localAxisAlignedBB4))
                return null;
            if (this.b.width >= 1.0F) {
                PathType localPathType2 = pa(this.b, paramInt1, paramInt2 - 1, paramInt3);
                if (localPathType2 == PathType.BLOCKED) {
                    localPathPoint = a(paramInt1, paramInt2, paramInt3);
                    localPathPoint.m = PathType.WALKABLE;
                    localPathPoint.l = Math.max(localPathPoint.l, f);
                    return localPathPoint;
                }
            }
            int i = 0;
            while (paramInt2 > 0 && localPathType1 == PathType.OPEN) {
                paramInt2--;
                if (i++ >= b.bg())
                    return null;
                localPathType1 = pa(this.b, paramInt1, paramInt2, paramInt3);
                f = this.b.a(localPathType1);
                if (localPathType1 != PathType.OPEN && f >= 0.0F) {
                    localPathPoint = a(paramInt1, paramInt2, paramInt3);
                    localPathPoint.m = localPathType1;
                    localPathPoint.l = Math.max(localPathPoint.l, f);
                } else if (f < 0.0F)
                    return null;
            }
        }
        return localPathPoint;
    }

    protected PathType pb(IBlockAccess paramIBlockAccess, int paramInt1, int paramInt2, int paramInt3) {
        BlockPosition localBlockPosition = new BlockPosition(paramInt1, paramInt2, paramInt3);
        IBlockData localIBlockData = paramIBlockAccess.getType(localBlockPosition);
        Block localBlock = localIBlockData.getBlock();
        Material localMaterial = localIBlockData.getMaterial();
        if (localMaterial == Material.AIR)
            return PathType.OPEN;
        if (localBlock == Blocks.TRAPDOOR || localBlock == Blocks.IRON_TRAPDOOR || localBlock == Blocks.WATERLILY)
            return PathType.TRAPDOOR;
        if (localBlock == Blocks.FIRE)
            return PathType.DAMAGE_FIRE;
        if (localBlock == Blocks.CACTUS)
            return PathType.DAMAGE_CACTUS;
        if (localBlock instanceof BlockDoor && localMaterial == Material.WOOD
                && !localIBlockData.get(BlockDoor.OPEN).booleanValue())
            return PathType.DOOR_WOOD_CLOSED;
        if (localBlock instanceof BlockDoor && localMaterial == Material.ORE
                && !localIBlockData.get(BlockDoor.OPEN).booleanValue())
            return PathType.DOOR_IRON_CLOSED;
        if (localBlock instanceof BlockDoor && localIBlockData.get(BlockDoor.OPEN).booleanValue())
            return PathType.DOOR_OPEN;
        if (localBlock instanceof BlockMinecartTrackAbstract)
            return PathType.RAIL;
        if (localBlock instanceof BlockFence || localBlock instanceof BlockCobbleWall
                || localBlock instanceof BlockFenceGate && !localIBlockData.get(BlockFenceGate.OPEN).booleanValue())
            return PathType.FENCE;
        if (localMaterial == Material.WATER)
            return PathType.WATER;
        if (localMaterial == Material.LAVA)
            return PathType.LAVA;
        if (localBlock.b(paramIBlockAccess, localBlockPosition))
            return PathType.OPEN;
        return PathType.BLOCKED;
    }
}
