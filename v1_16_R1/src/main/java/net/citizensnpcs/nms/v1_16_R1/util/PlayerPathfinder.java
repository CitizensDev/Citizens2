package net.citizensnpcs.nms.v1_16_R1.util;

import java.util.Comparator;
import java.util.Iterator;
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
import net.citizensnpcs.nms.v1_16_R1.entity.EntityHumanNPC;
import net.minecraft.server.v1_16_R1.BlockPosition;
import net.minecraft.server.v1_16_R1.ChunkCache;
import net.minecraft.server.v1_16_R1.EntityInsentient;
import net.minecraft.server.v1_16_R1.Path;
import net.minecraft.server.v1_16_R1.PathDestination;
import net.minecraft.server.v1_16_R1.PathEntity;
import net.minecraft.server.v1_16_R1.PathPoint;
import net.minecraft.server.v1_16_R1.Pathfinder;

public class PlayerPathfinder extends Pathfinder {
    private final PathPoint[] a = new PathPoint[32];
    private final int b;
    private final PlayerPathfinderNormal c;
    private final Path d = new Path();

    public PlayerPathfinder(PlayerPathfinderNormal var0, int var1) {
        super(var0, var1);
        this.c = var0;
        this.b = var1;
    }

    public PathEntity a(ChunkCache var0, EntityHumanNPC var1, Set<BlockPosition> var2, float var3, int var4,
            float var5) {
        this.d.a();
        this.c.a(var0, var1);
        PathPoint var6 = this.c.b();
        Map var7 = var2.stream().collect(Collectors.toMap((var0x) -> {
            return this.c.a((double) var0x.getX(), (double) var0x.getY(), (double) var0x.getZ());
        }, Function.identity()));
        PathEntity var8 = this.a(var6, var7, var3, var4, var5);
        this.c.a();
        return var8;
    }

    @Override
    public PathEntity a(ChunkCache var0, EntityInsentient var1, Set<BlockPosition> var2, float var3, int var4,
            float var5) {
        this.d.a();
        this.c.a(var0, var1);
        PathPoint var6 = this.c.b();
        Map var7 = var2.stream().collect(Collectors.toMap((var0x) -> {
            return this.c.a((double) var0x.getX(), (double) var0x.getY(), (double) var0x.getZ());
        }, Function.identity()));
        PathEntity var8 = this.a(var6, var7, var3, var4, var5);
        this.c.a();
        return var8;
    }

    private PathEntity a(PathPoint var0, BlockPosition var1, boolean var2) {
        List var3 = Lists.newArrayList();
        PathPoint var4 = var0;
        var3.add(0, var0);

        while (var4.h != null) {
            var4 = var4.h;
            var3.add(0, var4);
        }

        return new PathEntity(var3, var1, var2);
    }

    private PathEntity a(PathPoint var0, Map var1, float var2, int var3, float var4) {
        Set var5 = var1.keySet();
        var0.e = 0.0F;
        var0.f = this.a(var0, var5);
        var0.g = var0.f;
        this.d.a();
        this.d.a(var0);
        Set var6 = ImmutableSet.of();
        int var7 = 0;
        Set<PathDestination> var8 = Sets.newHashSetWithExpectedSize(var5.size());
        int var9 = (int) (this.b * var4);

        while (!this.d.e()) {
            ++var7;
            if (var7 >= var9) {
                break;
            }

            PathPoint var10 = this.d.c();
            var10.i = true;
            Iterator var12 = var5.iterator();

            while (var12.hasNext()) {
                PathDestination var122 = (PathDestination) var12.next();
                if (var10.c(var122) <= var3) {
                    var122.e();
                    var8.add(var122);
                }
            }

            if (!var8.isEmpty()) {
                break;
            }

            if (var10.a(var0) < var2) {
                int var11 = this.c.a(this.a, var10);

                for (int var12i = 0; var12i < var11; ++var12i) {
                    PathPoint var13 = this.a[var12i];
                    float var14 = var10.a(var13);
                    var13.j = var10.j + var14;
                    float var15 = var10.e + var14 + var13.k;
                    if (var13.j < var2 && (!var13.c() || var15 < var13.e)) {
                        var13.h = var10;
                        var13.e = var15;
                        var13.f = this.a(var13, var5) * 1.5F;
                        if (var13.c()) {
                            this.d.a(var13, var13.e + var13.f);
                        } else {
                            var13.g = var13.e + var13.f;
                            this.d.a(var13);
                        }
                    }
                }
            }
        }
        Optional var10 = !var8.isEmpty() ? var8.stream().map((var1x) -> {
            return this.a(var1x.d(), (BlockPosition) var1.get(var1x), true);
        }).min(Comparator.comparingInt(PathEntity::e)) : getFallbackDestinations(var1, var5).findFirst();
        if (!var10.isPresent()) {
            return null;
        } else {
            PathEntity var11 = (PathEntity) var10.get();
            return var11;
        }
    }

    private float a(PathPoint var0, Set var1) {
        float var2 = Float.MAX_VALUE;

        float var5;
        for (Iterator var4 = var1.iterator(); var4.hasNext(); var2 = Math.min(var5, var2)) {
            PathDestination var44 = (PathDestination) var4.next();
            var5 = var0.a(var44);
            var44.a(var5, var0);
        }

        return var2;
    }

    public Stream<PathEntity> getFallbackDestinations(Map<PathDestination, BlockPosition> var1,
            Set<PathDestination> var5) {
        if (Setting.DISABLE_MC_NAVIGATION_FALLBACK.asBoolean()) {
            return Stream.empty();
        }
        return var5.stream().map((var1x) -> {
            return this.a(var1x.d(), var1.get(var1x), false);
        }).sorted(Comparator.comparingDouble(PathEntity::n).thenComparingInt(PathEntity::e));
    }

}
