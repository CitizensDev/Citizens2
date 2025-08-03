package net.citizensnpcs.api.ai;

import net.citizensnpcs.api.ai.event.CancelReason;

public abstract class AbstractPathStrategy implements PathStrategy {
    private CancelReason cancelReason;
    private final TargetType type;

    protected AbstractPathStrategy(TargetType type) {
        this.type = type;
    }

    @Override
    public void clearCancelReason() {
        cancelReason = null;
    }

    @Override
    public CancelReason getCancelReason() {
        return cancelReason;
    }

    @Override
    public TargetType getTargetType() {
        return type;
    }

    protected void setCancelReason(CancelReason reason) {
        cancelReason = reason;
    }
}