package net.citizensnpcs.nms.v1_14_R1.util;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.citizensnpcs.nms.v1_14_R1.entity.EntityHumanNPC;
import net.minecraft.server.v1_14_R1.EntityInsentient;
import net.minecraft.server.v1_14_R1.IWorldReader;
import net.minecraft.server.v1_14_R1.Path;
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

    public PathEntity a(IWorldReader var0, EntityHumanNPC var1, double var2, double var4, double var6, float var8) {
        this.a.a();
        this.e.a(var0, var1);
        PathPoint var9 = this.e.b();
        PathPoint var10 = this.e.a(var2, var4, var6);
        PathEntity var11 = this.a(var9, var10, var8);
        this.e.a();
        return var11;
    }

    @Override
    public PathEntity a(IWorldReader var0, EntityInsentient var1, double var2, double var4, double var6, float var8) {
        this.a.a();
        this.e.a(var0, var1);
        PathPoint var9 = this.e.b();
        PathPoint var10 = this.e.a(var2, var4, var6);
        PathEntity var11 = this.a(var9, var10, var8);
        this.e.a();
        return var11;
    }

    private PathEntity a(PathPoint var0) {
        List var1 = Lists.newArrayList();
        PathPoint var2 = var0;
        var1.add(0, var0);

        while (var2.h != null) {
            var2 = var2.h;
            var1.add(0, var2);
        }

        return new PathEntity(var1);
    }

    private PathEntity a(PathPoint var0, PathPoint var1, float var2) {
        var0.e = 0.0F;
        var0.f = var0.a(var1);
        var0.g = var0.f;
        this.a.a();
        this.b.clear();
        this.a.a(var0);
        PathPoint var3 = var0;
        int var4 = 0;

        while (!this.a.e()) {
            ++var4;
            if (var4 >= this.d) {
                break;
            }

            PathPoint var5 = this.a.c();
            var5.i = true;
            if (var5.equals(var1)) {
                var3 = var1;
                break;
            }

            if (var5.a(var1) < var3.a(var1)) {
                var3 = var5;
            }

            if (var5.a(var1) < var2) {
                int var6 = this.e.a(this.c, var5);

                for (int var7 = 0; var7 < var6; ++var7) {
                    PathPoint var8 = this.c[var7];
                    float var9 = var5.a(var8);
                    var8.j = var5.j + var9;
                    float var10 = var5.e + var9 + var8.k;
                    if (var8.j < var2 && (!var8.c() || var10 < var8.e)) {
                        var8.h = var5;
                        var8.e = var10;
                        var8.f = var8.a(var1) * 1.5F + var8.k;
                        if (var8.c()) {
                            this.a.a(var8, var8.e + var8.f);
                        } else {
                            var8.g = var8.e + var8.f;
                            this.a.a(var8);
                        }
                    }
                }
            }
        }

        if (var3.equals(var0)) {
            return null;
        } else {
            PathEntity var5 = this.a(var3);
            return var5;
        }
    }
}
