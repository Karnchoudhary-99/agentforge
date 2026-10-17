package dev.agentforge.api;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** Explicit lifecycle states for a bounded run. Terminal states cannot transition. */
public enum RunStatus {
    QUEUED, RUNNING, SUCCEEDED, FAILED, CANCELLED;

    private static final Map<RunStatus, Set<RunStatus>> ALLOWED = Map.of(
            QUEUED, EnumSet.of(RUNNING, CANCELLED),
            RUNNING, EnumSet.of(SUCCEEDED, FAILED, CANCELLED),
            SUCCEEDED, EnumSet.noneOf(RunStatus.class),
            FAILED, EnumSet.noneOf(RunStatus.class),
            CANCELLED, EnumSet.noneOf(RunStatus.class));

    public boolean canTransitionTo(RunStatus next) {
        return ALLOWED.get(this).contains(next);
    }

    public RunStatus transitionTo(RunStatus next) {
        if (!canTransitionTo(next)) {
            throw new IllegalStateException("Illegal run status transition: " + this + " -> " + next);
        }
        return next;
    }
}
