package net.citizensnpcs.nms.v1_15_R1.util;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.nms.v1_15_R1.entity.EntityHumanNPC;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.ChunkCache;
import net.minecraft.server.v1_15_R1.EntityInsentient;
import net.minecraft.server.v1_15_R1.Path;
import net.minecraft.server.v1_15_R1.PathDestination;
import net.minecraft.server.v1_15_R1.PathEntity;
import net.minecraft.server.v1_15_R1.PathPoint;
import net.minecraft.server.v1_15_R1.Pathfinder;

public class PlayerPathfinder extends Pathfinder {
    private final Path a = new Path();
    private final Set b = Sets.newHashSet();
    private final PathPoint[] c = new PathPoint[32];
    private final int d;
    private final PlayerPathfinderNormal e;

    public PlayerPathfinder(PlayerPathfinderNormal var0, int var1) {
        super(var0, var1);
        this.e = var0;
        this.d = var1;
    }

    public PathEntity a(ChunkCache var0, EntityHumanNPC var1, Set<BlockPosition> var2, float var3, int var4,
            float var5) {
        this.a.a();
        this.e.a(var0, var1);
        PathPoint var6 = this.e.b();
        Map<PathDestination, BlockPosition> var7 = var2.stream()
                .collect(Collectors.toMap(
                        var0x -> this.e.a((double) var0x.getX(), (double) var0x.getY(), (double) var0x.getZ()),
                        Function.identity()));
        PathEntity var8 = this.a(var6, var7, var3, var4, var5);
        this.e.a();
        return var8;
    }

    @Override
    public PathEntity a(ChunkCache var0, EntityInsentient var1, Set<BlockPosition> var2, float var3, int var4,
            float var5) {
        this.a.a();
        this.e.a(var0, var1);
        PathPoint var6 = this.e.b();
        Map<PathDestination, BlockPosition> var7 = var2.stream()
                .collect(Collectors.toMap(
                        var0x -> this.e.a((double) var0x.getX(), (double) var0x.getY(), (double) var0x.getZ()),
                        Function.identity()));
        PathEntity var8 = this.a(var6, var7, var3, var4, var5);
        this.e.a();
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

    private PathEntity a(PathPoint var0, Map<PathDestination, BlockPosition> var1, float var2, int var3, float var4) {
        Set<PathDestination> var5 = var1.keySet();
        var0.e = 0.0F;
        var0.f = this.a(var0, var5);
        var0.g = var0.f;
        this.a.a();
        this.b.clear();
        this.a.a(var0);
        int var6 = 0;
        int var7 = (int) (this.d * var4);
        while (!this.a.e()) {
            ++var6;
            if (var6 >= var7) {
                break;
            }
            PathPoint var8 = this.a.c();
            var8.i = true;
            var5.stream().filter(var2x -> (var8.c(var2x) <= var3)).forEach(PathDestination::e);
            if (var5.stream().anyMatch(PathDestination::f)) {
                break;
            }
            if (var8.a(var0) < var2) {
                int var9 = this.e.a(this.c, var8);
                for (int var10 = 0; var10 < var9; ++var10) {
                    PathPoint var11 = this.c[var10];
                    float var12 = var8.a(var11);
                    var11.j = var8.j + var12;
                    float var13 = var8.e + var12 + var11.k;
                    if (var11.j < var2 && (!var11.c() || var13 < var11.e)) {
                        var11.h = var8;
                        var11.e = var13;
                        var11.f = this.a(var11, var5) * 1.5F;
                        if (var11.c()) {
                            this.a.a(var11, var11.e + var11.f);
                        } else {
                            var11.g = var11.e + var11.f;
                            this.a.a(var11);
                        }
                    }
                }
            }
        }
        Stream var8;
        if (var5.stream().anyMatch(PathDestination::f)) {
            var8 = var5.stream().filter(PathDestination::f).map(var1x -> this.a(var1x.d(), var1.get(var1x), true))
                    .sorted(Comparator.comparingInt(PathEntity::e));
        } else {
            var8 = getFallbackDestinations(var1, var5);
        }
        Optional var9 = var8.findFirst();
        if (!var9.isPresent())
            return null;
        else {
            PathEntity var10 = (PathEntity) var9.get();
            return var10;
        }
    }

    private float a(PathPoint var0, Set var1) {
        float var2 = Float.MAX_VALUE;
        float var5;
        for (Iterator var4 = var1.iterator(); var4.hasNext(); var2 = Math.min(var5, var2)) {
            PathDestination var6 = (PathDestination) var4.next();
            var5 = var0.a(var6);
            var6.a(var5, var0);
        }
        return var2;
    }

    public Stream<PathEntity> getFallbackDestinations(Map<PathDestination, BlockPosition> var1,
            Set<PathDestination> var5) {
        if (Setting.DISABLE_MC_NAVIGATION_FALLBACK.asBoolean())
            return Stream.empty();
        return var5.stream().map(var1x -> this.a(var1x.d(), var1.get(var1x), false))
                .sorted(Comparator.comparingDouble(PathEntity::l).thenComparingInt(PathEntity::e));
    }
}
