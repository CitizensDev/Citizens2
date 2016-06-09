package net.citizensnpcs.util.nms;

import java.util.EnumSet;
import java.util.HashSet;

import net.citizensnpcs.npc.entity.EntityHumanNPC;
import net.minecraft.server.v1_10_R1.AxisAlignedBB;
import net.minecraft.server.v1_10_R1.Block;
import net.minecraft.server.v1_10_R1.BlockCobbleWall;
import net.minecraft.server.v1_10_R1.BlockDoor;
import net.minecraft.server.v1_10_R1.BlockFence;
import net.minecraft.server.v1_10_R1.BlockFenceGate;
import net.minecraft.server.v1_10_R1.BlockMinecartTrackAbstract;
import net.minecraft.server.v1_10_R1.BlockPosition;
import net.minecraft.server.v1_10_R1.BlockPosition.MutableBlockPosition;
import net.minecraft.server.v1_10_R1.Blocks;
import net.minecraft.server.v1_10_R1.EntityInsentient;
import net.minecraft.server.v1_10_R1.EnumDirection;
import net.minecraft.server.v1_10_R1.IBlockAccess;
import net.minecraft.server.v1_10_R1.IBlockData;
import net.minecraft.server.v1_10_R1.Material;
import net.minecraft.server.v1_10_R1.MathHelper;
import net.minecraft.server.v1_10_R1.PathPoint;
import net.minecraft.server.v1_10_R1.PathType;

public class PlayerPathfinderNormal extends PlayerPathfinderAbstract {
    private float j;

    @Override
    public void a() {
        super.a();
        this.b.a(PathType.WATER, this.j);
    }

    @Override
    public PathPoint a(double paramDouble1, double paramDouble2, double paramDouble3) {
        return a(MathHelper.floor(paramDouble1 - this.b.width / 2.0F), MathHelper.floor(paramDouble2),
                MathHelper.floor(paramDouble3 - this.b.width / 2.0F));
    }

    private PathType a(EntityHumanNPC paramEntityInsentient, BlockPosition paramBlockPosition) {
        return a(this.a, paramBlockPosition.getX(), paramBlockPosition.getY(), paramBlockPosition.getZ(),
                paramEntityInsentient, this.d, this.e, this.f, d(), c());
    }

    private PathType a(EntityHumanNPC paramEntityInsentient, int paramInt1, int paramInt2, int paramInt3) {
        return a(this.a, paramInt1, paramInt2, paramInt3, paramEntityInsentient, this.d, this.e, this.f, d(), c());
    }

    @Override
    public void a(IBlockAccess paramIBlockAccess, EntityHumanNPC paramEntityInsentient) {
        super.a(paramIBlockAccess, paramEntityInsentient);
        this.j = paramEntityInsentient.a(PathType.WATER);
    }

    @Override
    public PathType a(IBlockAccess paramIBlockAccess, int paramInt1, int paramInt2, int paramInt3) {
        PathType localPathType1 = getPathTypeBase(paramIBlockAccess, paramInt1, paramInt2, paramInt3);
        if ((localPathType1 == PathType.OPEN) && (paramInt2 >= 1)) {
            PathType localPathType2 = localPathType1;
            while (localPathType2 == PathType.OPEN && (--paramInt2 >= 1)) {
                localPathType2 = getPathTypeBase(paramIBlockAccess, paramInt1, paramInt2, paramInt3);
            }
            localPathType1 = (localPathType2 == PathType.WALKABLE) || (localPathType2 == PathType.OPEN)
                    || (localPathType2 == PathType.WATER) || (localPathType2 == PathType.LAVA) ? PathType.OPEN
                            : PathType.WALKABLE;
        }
        if (localPathType1 == PathType.WALKABLE) {
            for (int i = paramInt1 - 1; i <= paramInt1 + 1; i++) {
                for (int k = paramInt3 - 1; k <= paramInt3 + 1; k++) {
                    if ((i != paramInt1) || (k != paramInt3)) {
                        Block localBlock2 = paramIBlockAccess.getType(new BlockPosition(i, paramInt2, k)).getBlock();
                        if (localBlock2 == Blocks.CACTUS) {
                            localPathType1 = PathType.DANGER_CACTUS;
                        } else if (localBlock2 == Blocks.FIRE) {
                            localPathType1 = PathType.DANGER_FIRE;
                        }
                    }
                }
            }
        }
        return localPathType1;
    }

    @Override
    public PathType a(IBlockAccess paramIBlockAccess, int paramInt1, int paramInt2, int paramInt3,
            EntityHumanNPC paramEntityInsentient, int paramInt4, int paramInt5, int paramInt6, boolean paramBoolean1,
            boolean paramBoolean2) {
        EnumSet<PathType> localEnumSet = EnumSet.noneOf(PathType.class);
        Object localObject1 = PathType.BLOCKED;

        double d = paramEntityInsentient.width / 2.0D;
        BlockPosition localBlockPosition = new BlockPosition(paramEntityInsentient);
        for (int i = paramInt1; i < paramInt1 + paramInt4; i++) {
            for (int k = paramInt2; k < paramInt2 + paramInt5; k++) {
                for (int m = paramInt3; m < paramInt3 + paramInt6; m++) {
                    PathType localPathType2 = a(paramIBlockAccess, i, k, m);
                    if ((localPathType2 == PathType.DOOR_WOOD_CLOSED) && (paramBoolean1) && (paramBoolean2)) {
                        localPathType2 = PathType.WALKABLE;
                    }
                    if ((localPathType2 == PathType.DOOR_OPEN) && (!paramBoolean2)) {
                        localPathType2 = PathType.BLOCKED;
                    }
                    if ((localPathType2 == PathType.RAIL)
                            && (!(paramIBlockAccess.getType(localBlockPosition)
                                    .getBlock() instanceof BlockMinecartTrackAbstract))
                            && (!(paramIBlockAccess.getType(localBlockPosition.down())
                                    .getBlock() instanceof BlockMinecartTrackAbstract))) {
                        localPathType2 = PathType.FENCE;
                    }
                    if ((i == paramInt1) && (k == paramInt2) && (m == paramInt3)) {
                        localObject1 = localPathType2;
                    }
                    if ((k > paramInt2) && (localPathType2 != PathType.OPEN)) {
                        AxisAlignedBB localAxisAlignedBB = new AxisAlignedBB(i - d + 0.5D, paramInt2 + 0.001D,
                                m - d + 0.5D, i + d + 0.5D, paramInt2 + paramEntityInsentient.length, m + d + 0.5D);
                        if (!paramEntityInsentient.world.b(localAxisAlignedBB)) {
                            localPathType2 = PathType.OPEN;
                        }
                    }
                    localEnumSet.add(localPathType2);
                }
            }
        }
        if (localEnumSet.contains(PathType.FENCE)) {
            return PathType.FENCE;
        }
        Object localObject2 = PathType.BLOCKED;
        for (PathType localPathType1 : localEnumSet) {
            if (paramEntityInsentient.a(localPathType1) < 0.0F) {
                return localPathType1;
            }
            if (paramEntityInsentient.a(localPathType1) >= paramEntityInsentient.a((PathType) localObject2)) {
                localObject2 = localPathType1;
            }
        }
        if ((localObject1 == PathType.OPEN) && (paramEntityInsentient.a((PathType) localObject2) == 0.0F)) {
            return PathType.OPEN;
        }
        return (PathType) localObject2;
    }

    @Override
    public PathType a(IBlockAccess paramIBlockAccess, int paramInt1, int paramInt2, int paramInt3,
            EntityInsentient paramEntityInsentient, int paramInt4, int paramInt5, int paramInt6, boolean paramBoolean1,
            boolean paramBoolean2) {
        EnumSet<PathType> localEnumSet = EnumSet.noneOf(PathType.class);
        Object localObject1 = PathType.BLOCKED;

        double d = paramEntityInsentient.width / 2.0D;
        BlockPosition localBlockPosition = new BlockPosition(paramEntityInsentient);
        for (int i = paramInt1; i < paramInt1 + paramInt4; i++) {
            for (int k = paramInt2; k < paramInt2 + paramInt5; k++) {
                for (int m = paramInt3; m < paramInt3 + paramInt6; m++) {
                    PathType localPathType2 = a(paramIBlockAccess, i, k, m);
                    if ((localPathType2 == PathType.DOOR_WOOD_CLOSED) && (paramBoolean1) && (paramBoolean2)) {
                        localPathType2 = PathType.WALKABLE;
                    }
                    if ((localPathType2 == PathType.DOOR_OPEN) && (!paramBoolean2)) {
                        localPathType2 = PathType.BLOCKED;
                    }
                    if ((localPathType2 == PathType.RAIL)
                            && (!(paramIBlockAccess.getType(localBlockPosition)
                                    .getBlock() instanceof BlockMinecartTrackAbstract))
                            && (!(paramIBlockAccess.getType(localBlockPosition.down())
                                    .getBlock() instanceof BlockMinecartTrackAbstract))) {
                        localPathType2 = PathType.FENCE;
                    }
                    if ((i == paramInt1) && (k == paramInt2) && (m == paramInt3)) {
                        localObject1 = localPathType2;
                    }
                    if ((k > paramInt2) && (localPathType2 != PathType.OPEN)) {
                        AxisAlignedBB localAxisAlignedBB = new AxisAlignedBB(i - d + 0.5D, paramInt2 + 0.001D,
                                m - d + 0.5D, i + d + 0.5D, paramInt2 + paramEntityInsentient.length, m + d + 0.5D);
                        if (!paramEntityInsentient.world.b(localAxisAlignedBB)) {
                            localPathType2 = PathType.OPEN;
                        }
                    }
                    localEnumSet.add(localPathType2);
                }
            }
        }
        if (localEnumSet.contains(PathType.FENCE)) {
            return PathType.FENCE;
        }
        Object localObject2 = PathType.BLOCKED;
        for (PathType localPathType1 : localEnumSet) {
            if (paramEntityInsentient.a(localPathType1) < 0.0F) {
                return localPathType1;
            }
            if (paramEntityInsentient.a(localPathType1) >= paramEntityInsentient.a((PathType) localObject2)) {
                localObject2 = localPathType1;
            }
        }
        if ((localObject1 == PathType.OPEN) && (paramEntityInsentient.a((PathType) localObject2) == 0.0F)) {
            return PathType.OPEN;
        }
        return (PathType) localObject2;
    }

    private PathPoint a(int paramInt1, int paramInt2, int paramInt3, int paramInt4, double paramDouble,
            EnumDirection paramEnumDirection) {
        PathPoint localPathPoint = null;

        BlockPosition localBlockPosition1 = new BlockPosition(paramInt1, paramInt2, paramInt3);
        BlockPosition localBlockPosition2 = localBlockPosition1.down();
        double d1 = paramInt2 - (1.0D - this.a.getType(localBlockPosition2).c(this.a, localBlockPosition2).e);
        if (d1 - paramDouble > 1.0D) {
            return null;
        }
        PathType localPathType = a(this.b, paramInt1, paramInt2, paramInt3);
        float f = this.b.a(localPathType);
        double d2 = this.b.width / 2.0D;
        if (f >= 0.0F) {
            localPathPoint = a(paramInt1, paramInt2, paramInt3);
            localPathPoint.m = localPathType;
            localPathPoint.l = Math.max(localPathPoint.l, f);
        }
        if (localPathType == PathType.WALKABLE) {
            return localPathPoint;
        }
        if ((localPathPoint == null) && (paramInt4 > 0) && (localPathType != PathType.FENCE)
                && (localPathType != PathType.TRAPDOOR)) {
            localPathPoint = a(paramInt1, paramInt2 + 1, paramInt3, paramInt4 - 1, paramDouble, paramEnumDirection);
            if ((localPathPoint != null)
                    && ((localPathPoint.m == PathType.OPEN) || (localPathPoint.m == PathType.WALKABLE))) {
                double d3 = paramInt1 - paramEnumDirection.getAdjacentX() + 0.5D;
                double d4 = paramInt3 - paramEnumDirection.getAdjacentZ() + 0.5D;

                AxisAlignedBB localAxisAlignedBB1 = new AxisAlignedBB(d3 - d2, paramInt2 + 0.001D, d4 - d2, d3 + d2,
                        paramInt2 + this.b.length, d4 + d2);
                AxisAlignedBB localAxisAlignedBB2 = this.a.getType(localBlockPosition1).c(this.a, localBlockPosition1);

                AxisAlignedBB localAxisAlignedBB3 = localAxisAlignedBB1.a(0.0D, localAxisAlignedBB2.e - 0.002D, 0.0D);
                if (this.b.world.b(localAxisAlignedBB3)) {
                    localPathPoint = null;
                }
            }
        }
        if (localPathType == PathType.OPEN) {
            AxisAlignedBB localAxisAlignedBB4 = new AxisAlignedBB(paramInt1 - d2 + 0.5D, paramInt2 + 0.001D,
                    paramInt3 - d2 + 0.5D, paramInt1 + d2 + 0.5D, paramInt2 + this.b.length, paramInt3 + d2 + 0.5D);
            if (this.b.world.b(localAxisAlignedBB4)) {
                return null;
            }
            int i = 0;
            while ((paramInt2 > 0) && (localPathType == PathType.OPEN)) {
                paramInt2--;
                if (i++ >= this.b.aY()) {
                    return null;
                }
                localPathType = a(this.b, paramInt1, paramInt2, paramInt3);
                f = this.b.a(localPathType);
                if ((localPathType != PathType.OPEN) && (f >= 0.0F)) {
                    localPathPoint = a(paramInt1, paramInt2, paramInt3);
                    localPathPoint.m = localPathType;
                    localPathPoint.l = Math.max(localPathPoint.l, f);
                } else if (f < 0.0F) {
                    return null;
                }
            }
        }
        return localPathPoint;
    }

    @Override
    public int a(PathPoint[] paramArrayOfPathPoint, PathPoint paramPathPoint1, PathPoint paramPathPoint2,
            float paramFloat) {
        int i = 0;

        int k = 0;
        PathType localPathType = a(this.b, paramPathPoint1.a, paramPathPoint1.b + 1, paramPathPoint1.c);
        if (this.b.a(localPathType) >= 0.0F) {
            k = 1;
        }
        BlockPosition localBlockPosition = new BlockPosition(paramPathPoint1.a, paramPathPoint1.b, paramPathPoint1.c)
                .down();
        double d = paramPathPoint1.b - (1.0D - this.a.getType(localBlockPosition).c(this.a, localBlockPosition).e);

        PathPoint localPathPoint1 = a(paramPathPoint1.a, paramPathPoint1.b, paramPathPoint1.c + 1, k, d,
                EnumDirection.SOUTH);
        PathPoint localPathPoint2 = a(paramPathPoint1.a - 1, paramPathPoint1.b, paramPathPoint1.c, k, d,
                EnumDirection.WEST);
        PathPoint localPathPoint3 = a(paramPathPoint1.a + 1, paramPathPoint1.b, paramPathPoint1.c, k, d,
                EnumDirection.EAST);
        PathPoint localPathPoint4 = a(paramPathPoint1.a, paramPathPoint1.b, paramPathPoint1.c - 1, k, d,
                EnumDirection.NORTH);
        if ((localPathPoint1 != null) && (!localPathPoint1.i) && (localPathPoint1.a(paramPathPoint2) < paramFloat)) {
            paramArrayOfPathPoint[(i++)] = localPathPoint1;
        }
        if ((localPathPoint2 != null) && (!localPathPoint2.i) && (localPathPoint2.a(paramPathPoint2) < paramFloat)) {
            paramArrayOfPathPoint[(i++)] = localPathPoint2;
        }
        if ((localPathPoint3 != null) && (!localPathPoint3.i) && (localPathPoint3.a(paramPathPoint2) < paramFloat)) {
            paramArrayOfPathPoint[(i++)] = localPathPoint3;
        }
        if ((localPathPoint4 != null) && (!localPathPoint4.i) && (localPathPoint4.a(paramPathPoint2) < paramFloat)) {
            paramArrayOfPathPoint[(i++)] = localPathPoint4;
        }
        int m = (localPathPoint4 == null) || (localPathPoint4.m == PathType.OPEN) || (localPathPoint4.l != 0.0F) ? 1
                : 0;
        int n = (localPathPoint1 == null) || (localPathPoint1.m == PathType.OPEN) || (localPathPoint1.l != 0.0F) ? 1
                : 0;
        int i1 = (localPathPoint3 == null) || (localPathPoint3.m == PathType.OPEN) || (localPathPoint3.l != 0.0F) ? 1
                : 0;
        int i2 = (localPathPoint2 == null) || (localPathPoint2.m == PathType.OPEN) || (localPathPoint2.l != 0.0F) ? 1
                : 0;
        PathPoint localPathPoint5;
        if ((m != 0) && (i2 != 0)) {
            localPathPoint5 = a(paramPathPoint1.a - 1, paramPathPoint1.b, paramPathPoint1.c - 1, k, d,
                    EnumDirection.NORTH);
            if ((localPathPoint5 != null) && (!localPathPoint5.i)
                    && (localPathPoint5.a(paramPathPoint2) < paramFloat)) {
                paramArrayOfPathPoint[(i++)] = localPathPoint5;
            }
        }
        if ((m != 0) && (i1 != 0)) {
            localPathPoint5 = a(paramPathPoint1.a + 1, paramPathPoint1.b, paramPathPoint1.c - 1, k, d,
                    EnumDirection.NORTH);
            if ((localPathPoint5 != null) && (!localPathPoint5.i)
                    && (localPathPoint5.a(paramPathPoint2) < paramFloat)) {
                paramArrayOfPathPoint[(i++)] = localPathPoint5;
            }
        }
        if ((n != 0) && (i2 != 0)) {
            localPathPoint5 = a(paramPathPoint1.a - 1, paramPathPoint1.b, paramPathPoint1.c + 1, k, d,
                    EnumDirection.SOUTH);
            if ((localPathPoint5 != null) && (!localPathPoint5.i)
                    && (localPathPoint5.a(paramPathPoint2) < paramFloat)) {
                paramArrayOfPathPoint[(i++)] = localPathPoint5;
            }
        }
        if ((n != 0) && (i1 != 0)) {
            localPathPoint5 = a(paramPathPoint1.a + 1, paramPathPoint1.b, paramPathPoint1.c + 1, k, d,
                    EnumDirection.SOUTH);
            if ((localPathPoint5 != null) && (!localPathPoint5.i)
                    && (localPathPoint5.a(paramPathPoint2) < paramFloat)) {
                paramArrayOfPathPoint[(i++)] = localPathPoint5;
            }
        }
        return i;
    }

    @Override
    public PathPoint b() {
        int i;
        BlockPosition localObject1;
        if ((e()) && (this.b.isInWater())) {
            i = (int) this.b.getBoundingBox().b;
            localObject1 = new BlockPosition.MutableBlockPosition(MathHelper.floor(this.b.locX), i,
                    MathHelper.floor(this.b.locZ));
            Block localObject2 = this.a.getType(localObject1).getBlock();
            while ((localObject2 == Blocks.FLOWING_WATER) || (localObject2 == Blocks.WATER)) {
                i++;
                ((MutableBlockPosition) localObject1).c(MathHelper.floor(this.b.locX), i,
                        MathHelper.floor(this.b.locZ));
                localObject2 = this.a.getType(localObject1).getBlock();
            }
        } else if (!this.b.onGround) {
            localObject1 = new BlockPosition(this.b);
            while (((this.a.getType(localObject1).getMaterial() == Material.AIR)
                    || (this.a.getType(localObject1).getBlock().b(this.a, localObject1)))
                    && (localObject1.getY() > 0)) {
                localObject1 = localObject1.down();
            }
            i = localObject1.up().getY();
        } else {
            i = MathHelper.floor(this.b.getBoundingBox().b + 0.5D);
        }
        localObject1 = new BlockPosition(this.b);
        Object localObject2 = a(this.b, localObject1.getX(), i, localObject1.getZ());
        if (this.b.a((PathType) localObject2) < 0.0F) {
            HashSet<BlockPosition> localHashSet = new HashSet<BlockPosition>();
            localHashSet.add(new BlockPosition(this.b.getBoundingBox().a, i, this.b.getBoundingBox().c));
            localHashSet.add(new BlockPosition(this.b.getBoundingBox().a, i, this.b.getBoundingBox().f));
            localHashSet.add(new BlockPosition(this.b.getBoundingBox().d, i, this.b.getBoundingBox().c));
            localHashSet.add(new BlockPosition(this.b.getBoundingBox().d, i, this.b.getBoundingBox().f));
            for (BlockPosition localBlockPosition : localHashSet) {
                PathType localPathType = a(this.b, localBlockPosition);
                if (this.b.a(localPathType) >= 0.0F) {
                    return a(localBlockPosition.getX(), localBlockPosition.getY(), localBlockPosition.getZ());
                }
            }
        }
        return a(localObject1.getX(), i, localObject1.getZ());
    }

    public PathType getPathTypeBase(IBlockAccess paramIBlockAccess, int paramInt1, int paramInt2, int paramInt3) {
        BlockPosition localBlockPosition = new BlockPosition(paramInt1, paramInt2, paramInt3);
        IBlockData localIBlockData = paramIBlockAccess.getType(localBlockPosition);
        Block localBlock1 = localIBlockData.getBlock();
        Material localMaterial = localIBlockData.getMaterial();

        PathType localPathType1 = PathType.BLOCKED;
        if ((localBlock1 == Blocks.TRAPDOOR) || (localBlock1 == Blocks.IRON_TRAPDOOR)
                || (localBlock1 == Blocks.WATERLILY)) {
            return PathType.TRAPDOOR;
        }
        if (localBlock1 == Blocks.FIRE) {
            return PathType.DAMAGE_FIRE;
        }
        if (localBlock1 == Blocks.CACTUS) {
            return PathType.DAMAGE_CACTUS;
        }
        if (((localBlock1 instanceof BlockDoor)) && (localMaterial == Material.WOOD)
                && (!localIBlockData.get(BlockDoor.OPEN).booleanValue())) {
            return PathType.DOOR_WOOD_CLOSED;
        }
        if (((localBlock1 instanceof BlockDoor)) && (localMaterial == Material.ORE)
                && (!localIBlockData.get(BlockDoor.OPEN).booleanValue())) {
            return PathType.DOOR_IRON_CLOSED;
        }
        if (((localBlock1 instanceof BlockDoor)) && (localIBlockData.get(BlockDoor.OPEN).booleanValue())) {
            return PathType.DOOR_OPEN;
        }
        if ((localBlock1 instanceof BlockMinecartTrackAbstract)) {
            return PathType.RAIL;
        }
        if (((localBlock1 instanceof BlockFence)) || ((localBlock1 instanceof BlockCobbleWall))
                || (((localBlock1 instanceof BlockFenceGate))
                        && (!localIBlockData.get(BlockFenceGate.OPEN).booleanValue()))) {
            return PathType.FENCE;
        }
        if (localMaterial == Material.AIR) {
            localPathType1 = PathType.OPEN;
        } else {
            if (localMaterial == Material.WATER) {
                return PathType.WATER;
            }
            if (localMaterial == Material.LAVA) {
                return PathType.LAVA;
            }
        }
        if ((localBlock1.b(paramIBlockAccess, localBlockPosition)) && (localPathType1 == PathType.BLOCKED)) {
            localPathType1 = PathType.OPEN;
        }
        return localPathType1;
    }
}
