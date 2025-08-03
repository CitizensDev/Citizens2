package net.citizensnpcs.nms.v1_16_R3.util;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.citizensnpcs.Settings.Setting;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.ChunkCache;
import net.minecraft.server.v1_16_R3.EntityInsentient;
import net.minecraft.server.v1_16_R3.EntityLiving;
import net.minecraft.server.v1_16_R3.Path;
import net.minecraft.server.v1_16_R3.PathDestination;
import net.minecraft.server.v1_16_R3.PathEntity;
import net.minecraft.server.v1_16_R3.PathPoint;
import net.minecraft.server.v1_16_R3.Pathfinder;

public class EntityPathfinder extends Pathfinder {
    private final PathPoint[] a = new PathPoint[32];
    private final int b;
    private final EntityPathfinderNormal c;
    private final Path d = new Path();

    public EntityPathfinder(EntityPathfinderNormal var0, int var1) {
        super(var0, var1);
        this.c = var0;
        this.b = var1;
    }

    @Override
    public PathEntity a(ChunkCache var0, EntityInsentient var1, Set<BlockPosition> var2, float var3, int var4,
            float var5) {
        this.d.a();
        this.c.a(var0, var1);
        PathPoint var6 = this.c.b();
        Map var7 = var2.stream()
                .collect(Collectors.toMap(
                        var0x -> this.c.a((double) var0x.getX(), (double) var0x.getY(), (double) var0x.getZ()),
                        Function.identity()));
        PathEntity var8 = this.a(var6, var7, var3, var4, var5);
        this.c.a();
        return var8;
    }

    public PathEntity a(ChunkCache var0, EntityLiving var1, Set<BlockPosition> var2, float var3, int var4, float var5) {
        this.d.a();
        this.c.a(var0, var1);
        PathPoint var6 = this.c.b();
        Map var7 = var2.stream()
                .collect(Collectors.toMap(
                        var0x -> this.c.a((double) var0x.getX(), (double) var0x.getY(), (double) var0x.getZ()),
                        Function.identity()));
        PathEntity var8 = this.a(var6, var7, var3, var4, var5);
        this.c.a();
        return var8;
    }

    private PathEntity a(PathPoint var0, BlockPosition var1, boolean var2) {
        List<PathPoint> var3 = Lists.newArrayList();
        PathPoint var4 = var0;
        var3.add(0, var4);
        while (var4.h != null) {
            var4 = var4.h;
            var3.add(0, var4);
        }
        return new PathEntity(var3, var1, var2);
    }

    private PathEntity a(PathPoint var0, Map var1, float var2, int var3, float var4) {
        Set<PathDestination> var5 = var1.keySet();
        var0.e = 0.0F;
        var0.f = a(var0, var5);
        var0.g = var0.f;
        this.d.a();
        this.d.a(var0);
        ImmutableSet immutableSet = ImmutableSet.of();
        int var7 = 0;
        Set<PathDestination> var8 = Sets.newHashSetWithExpectedSize(var5.size());
        int var9 = (int) (this.b * var4);
        while (!this.d.e() && ++var7 < var9) {
            PathPoint pathPoint = this.d.c();
            pathPoint.i = true;
            for (PathDestination pathDestination : var5) {
                if (pathPoint.c(pathDestination) <= var3) {
                    pathDestination.e();
                    var8.add(pathDestination);
                }
            }
            if (!var8.isEmpty()) {
                break;
            }
            if (pathPoint.a(var0) >= var2) {
                continue;
            }
            int i = this.c.a(this.a, pathPoint);
            for (int var12 = 0; var12 < i; var12++) {
                PathPoint var13 = this.a[var12];
                float var14 = pathPoint.a(var13);
                pathPoint.j += var14;
                float var15 = pathPoint.e + var14 + var13.k;
                if (var13.j < var2 && (!var13.c() || var15 < var13.e)) {
                    var13.h = pathPoint;
                    var13.e = var15;
                    var13.f = a(var13, var5) * 1.5F;
                    if (var13.c()) {
                        this.d.a(var13, var13.e + var13.f);
                    } else {
                        var13.g = var13.e + var13.f;
                        this.d.a(var13);
                    }
                }
            }
        }
        Optional var10 = !var8.isEmpty()
                ? var8.stream().map(var1x -> this.a(var1x.d(), (BlockPosition) var1.get(var1x), true)).min(
                        Comparator.comparingInt(PathEntity::e))
                : getFallbackDestinations(var1, var5).findFirst();
        if (!var10.isPresent())
            return null;
        else {
            PathEntity var11 = (PathEntity) var10.get();
            return var11;
        }
    }

    private float a(PathPoint var0, Set<PathDestination> var1) {
        float var2 = Float.MAX_VALUE;
        for (PathDestination var4 : var1) {
            float var5 = var0.a(var4);
            var4.a(var5, var0);
            var2 = Math.min(var5, var2);
        }
        return var2;
    }

    public Stream<PathEntity> getFallbackDestinations(Map<PathDestination, BlockPosition> var1,
            Set<PathDestination> var5) {
        if (Setting.DISABLE_MC_NAVIGATION_FALLBACK.asBoolean())
            return Stream.empty();
        return var5.stream().map(var1x -> this.a(var1x.d(), var1.get(var1x), false))
                .sorted(Comparator.comparingDouble(PathEntity::n).thenComparingInt(PathEntity::e));
    }
}
