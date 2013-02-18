package net.citizensnpcs.api.ai.event;

import javax.annotation.Nullable;

public interface NavigatorCallback {
    void onCompletion(@Nullable CancelReason cancelReason);
}
