package net.citizensnpcs.nms.v1_14_R1.util;

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
import net.citizensnpcs.nms.v1_14_R1.entity.EntityHumanNPC;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.EntityInsentient;
import net.minecraft.server.v1_14_R1.IWorldReader;
import net.minecraft.server.v1_14_R1.Path;
import net.minecraft.server.v1_14_R1.PathDestination;
import net.minecraft.server.v1_14_R1.PathEntity;
import net.minecraft.server.v1_14_R1.PathPoint;
import net.minecraft.server.v1_14_R1.Pathfinder;

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

    public PathEntity a(IWorldReader var0, EntityHumanNPC var1, Set<BlockPosition> var2, float var3, int var4) {
        this.a.a();
        this.e.a(var0, var1);
        PathPoint var5 = this.e.b();
        Map var6 = var2.stream()
                .collect(Collectors.toMap(
                        var0x -> this.e.a((double) var0x.getX(), (double) var0x.getY(), (double) var0x.getZ()),
                        Function.identity()));
        PathEntity var7 = this.a(var5, var6, var3, var4);
        this.e.a();
        return var7;
    }

    @Override
    public PathEntity a(IWorldReader var0, EntityInsentient var1, Set<BlockPosition> var2, float var3, int var4) {
        this.a.a();
        this.e.a(var0, var1);
        PathPoint var5 = this.e.b();
        Map var6 = var2.stream()
                .collect(Collectors.toMap(
                        var0x -> this.e.a((double) var0x.getX(), (double) var0x.getY(), (double) var0x.getZ()),
                        Function.identity()));
        PathEntity var7 = this.a(var5, var6, var3, var4);
        this.e.a();
        return var7;
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

    private PathEntity a(PathPoint var0, Map var1, float var2, int var3) {
        Set<PathDestination> var4 = var1.keySet();
        var0.e = 0.0F;
        var0.f = this.a(var0, var4);
        var0.g = var0.f;
        this.a.a();
        this.b.clear();
        this.a.a(var0);
        int var5 = 0;
        while (!this.a.e()) {
            ++var5;
            if (var5 >= this.d) {
                break;
            }
            PathPoint var6 = this.a.c();
            var6.i = true;
            var4.stream().filter(var2x -> (var6.c(var2x) <= var3)).forEach(PathDestination::e);
            if (var4.stream().anyMatch(PathDestination::f)) {
                break;
            }
            if (var6.a(var0) < var2) {
                int var7 = this.e.a(this.c, var6);
                for (int var8 = 0; var8 < var7; ++var8) {
                    PathPoint var9 = this.c[var8];
                    float var10 = var6.a(var9);
                    var9.j = var6.j + var10;
                    float var11 = var6.e + var10 + var9.k;
                    if (var9.j < var2 && (!var9.c() || var11 < var9.e)) {
                        var9.h = var6;
                        var9.e = var11;
                        var9.f = this.a(var9, var4) * 1.5F;
                        if (var9.c()) {
                            this.a.a(var9, var9.e + var9.f);
                        } else {
                            var9.g = var9.e + var9.f;
                            this.a.a(var9);
                        }
                    }
                }
            }
        }
        Stream var6;
        if (var4.stream().anyMatch(PathDestination::f)) {
            var6 = var4.stream().filter(PathDestination::f)
                    .map(var1x -> this.a(var1x.d(), (BlockPosition) var1.get(var1x), true))
                    .sorted(Comparator.comparingInt(PathEntity::e));
        } else {
            var6 = getFallbackDestinations(var1, var4);
        }
        Optional var7 = var6.findFirst();
        if (!var7.isPresent())
            return null;
        else {
            PathEntity var8 = (PathEntity) var7.get();
            return var8;
        }
    }

    private float a(PathPoint var0, Set var1) {
        float var2 = Float.MAX_VALUE;
        float var5;
        for (Iterator var4 = var1.iterator(); var4.hasNext(); var2 = Math.min(var5, var2)) {
            PathDestination var3 = (PathDestination) var4.next();
            var5 = var0.a(var3);
            var3.a(var5, var0);
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
