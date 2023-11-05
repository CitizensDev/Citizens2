package net.citizensnpcs.nms.v1_18_R2.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;

public abstract class EntityNodeEvaluatorBase extends NodeEvaluator {
    protected final Int2ObjectMap<Node> c = new Int2ObjectOpenHashMap<>();
    protected boolean canFloat;
    protected boolean canOpenDoors;
    protected boolean canPassDoors;
    protected int entityDepth;
    protected int entityHeight;
    protected int entityWidth;
    protected PathNavigationRegion level;
    protected LivingEntity mob;
    protected MobAI mvmt;

    @Override
    public boolean canFloat() {
        return this.canFloat;
    }

    @Override
    public boolean canOpenDoors() {
        return this.canOpenDoors;
    }

    @Override
    public boolean canPassDoors() {
        return this.canPassDoors;
    }

    @Override
    public void done() {
        this.level = null;
        this.mob = null;
    }

    @Override
    protected Node getNode(BlockPos var0) {
        return getNode(var0.getX(), var0.getY(), var0.getZ());
    }

    @Override
    protected Node getNode(int var0, int var1, int var2) {
        return this.c.computeIfAbsent(Node.createHash(var0, var1, var2), var3 -> new Node(var0, var1, var2));
    }

    public void prepare(PathNavigationRegion var0, LivingEntity var1) {
        this.level = var0;
        this.mob = var1;
        this.mvmt = MobAI.from(var1);
        this.c.clear();
        this.entityWidth = Mth.floor(var1.getBbWidth() + 1.0F);
        this.entityHeight = Mth.floor(var1.getBbHeight() + 1.0F);
        this.entityDepth = Mth.floor(var1.getBbWidth() + 1.0F);
    }

    @Override
    public void prepare(PathNavigationRegion var0, Mob var1) {
        this.level = var0;
        this.c.clear();
        this.entityWidth = Mth.floor(var1.getBbWidth() + 1.0F);
        this.entityHeight = Mth.floor(var1.getBbHeight() + 1.0F);
        this.entityDepth = Mth.floor(var1.getBbWidth() + 1.0F);
    }

    @Override
    public void setCanFloat(boolean var0) {
        this.canFloat = var0;
    }

    @Override
    public void setCanOpenDoors(boolean var0) {
        this.canOpenDoors = var0;
    }

    @Override
    public void setCanPassDoors(boolean var0) {
        this.canPassDoors = var0;
    }
}
