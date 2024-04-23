package net.citizensnpcs.nms.v1_20_R4.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.Target;

public abstract class EntityNodeEvaluatorBase extends NodeEvaluator {
    protected boolean canFloat;
    protected boolean canOpenDoors;
    protected boolean canPassDoors;
    protected boolean canWalkOverFences;
    protected PathfindingContext currentContext;
    protected int entityDepth;
    protected int entityHeight;
    protected int entityWidth;
    protected LivingEntity mob;
    protected MobAI mvmt;
    protected final Int2ObjectMap nodes = new Int2ObjectOpenHashMap();

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
    public boolean canWalkOverFences() {
        return this.canWalkOverFences;
    }

    @Override
    public void done() {
        this.currentContext = null;
        this.mob = null;
        this.mvmt = null;
    }

    @Override
    protected Node getNode(BlockPos var0) {
        return this.getNode(var0.getX(), var0.getY(), var0.getZ());
    }

    @Override
    protected Node getNode(int var0, int var1, int var2) {
        return (Node) this.nodes.computeIfAbsent(Node.createHash(var0, var1, var2), (var3) -> {
            return new Node(var0, var1, var2);
        });
    }

    @Override
    public PathType getPathType(Mob var0, BlockPos var1) {
        return this.getPathType(new PathfindingContext(var0.level(), var0), var1.getX(), var1.getY(), var1.getZ());
    }

    @Override
    protected Target getTargetNodeAt(double var0, double var2, double var4) {
        return new Target(this.getNode(Mth.floor(var0), Mth.floor(var2), Mth.floor(var4)));
    }

    public void prepare(PathNavigationRegion var0, LivingEntity var1) {
        this.mob = var1;
        this.mvmt = MobAI.from(var1);
        this.currentContext = new EntityPathfindingContext(var0, var1);
        this.nodes.clear();
        this.entityWidth = Mth.floor(var1.getBbWidth() + 1.0F);
        this.entityHeight = Mth.floor(var1.getBbHeight() + 1.0F);
        this.entityDepth = Mth.floor(var1.getBbWidth() + 1.0F);
    }

    @Override
    public void prepare(PathNavigationRegion var0, Mob var1) {
        this.currentContext = new PathfindingContext(var0, var1);
        this.mob = var1;
        this.nodes.clear();
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

    @Override
    public void setCanWalkOverFences(boolean var0) {
        this.canWalkOverFences = var0;
    }
}
