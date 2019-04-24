package net.citizensnpcs.nms.v1_14_R1.util;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.citizensnpcs.nms.v1_14_R1.entity.EntityHumanNPC;
import net.minecraft.server.v1_14_R1.EntityInsentient;
import net.minecraft.server.v1_14_R1.IBlockAccess;
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

    public PathEntity a(IBlockAccess var0, EntityHumanNPC var1, double var2, double var4, double var6, float var8) {
        this.a.a();
        this.e.a(var0, var1);
        PathPoint var9 = this.e.b();
        PathPoint var10 = this.e.a(var2, var4, var6);
        PathEntity var11 = this.a(var9, var10, var8);
        this.e.a();
        return var11;
    }

    @Override
    public PathEntity a(IBlockAccess var0, EntityInsentient var1, double var2, double var4, double var6, float var8) {
        this.a.a();
        this.e.a(var0, var1);
        PathPoint var9 = this.e.b();
        PathPoint var10 = this.e.a(var2, var4, var6);
        PathEntity var11 = this.a(var9, var10, var8);
        this.e.a();
        return var11;
    }

    private PathEntity a(PathPoint var0) {
        List<PathPoint> var1 = Lists.newArrayList();
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
        var0.f = var0.c(var1);
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
            if (var5.equals(var1)) {
                var3 = var1;
                break;
            }

            if (var5.c(var1) < var3.c(var1)) {
                var3 = var5;
            }

            var5.i = true;
            int var6 = this.e.a(this.c, var5, var1, var2);

            for (int var7 = 0; var7 < var6; ++var7) {
                PathPoint var8 = this.c[var7];
                float var9 = var5.c(var8);
                var8.j = var5.j + var9;
                var8.k = var9 + var8.l;
                float var10 = var5.e + var8.k;
                if (var8.j < var2 && (!var8.c() || var10 < var8.e)) {
                    var8.h = var5;
                    var8.e = var10;
                    var8.f = var8.c(var1) + var8.l;
                    if (var8.c()) {
                        this.a.a(var8, var8.e + var8.f);
                    } else {
                        var8.g = var8.e + var8.f;
                        this.a.a(var8);
                    }
                }
            }
        }

        if (var3 == var0) {
            return null;
        } else {
            PathEntity var5 = this.a(var3);
            return var5;
        }
    }

}
