package net.citizensnpcs.api.ai;

public abstract class AbstractGoal implements Goal {

    @Override
    public boolean isCompatibleWith(Goal other) {
        return false;
    }

    @Override
    public void reset() {
    }

    @Override
    public void start() {
    }

    @Override
    public boolean shouldExecute() {
        return continueExecuting();
    }
}
