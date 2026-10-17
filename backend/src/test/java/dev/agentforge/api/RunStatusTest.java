package dev.agentforge.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RunStatusTest {
    @Test
    void permitsOnlyDeclaredForwardTransitions() {
        assertTrue(RunStatus.QUEUED.canTransitionTo(RunStatus.RUNNING));
        assertTrue(RunStatus.QUEUED.canTransitionTo(RunStatus.CANCELLED));
        assertTrue(RunStatus.RUNNING.canTransitionTo(RunStatus.SUCCEEDED));
        assertTrue(RunStatus.RUNNING.canTransitionTo(RunStatus.FAILED));
        assertTrue(RunStatus.RUNNING.canTransitionTo(RunStatus.CANCELLED));
        assertFalse(RunStatus.QUEUED.canTransitionTo(RunStatus.SUCCEEDED));
        assertFalse(RunStatus.SUCCEEDED.canTransitionTo(RunStatus.RUNNING));
        assertFalse(RunStatus.FAILED.canTransitionTo(RunStatus.SUCCEEDED));
        assertFalse(RunStatus.CANCELLED.canTransitionTo(RunStatus.RUNNING));
    }

    @Test
    void transitionToReturnsNextStateForValidTransition() {
        assertEquals(RunStatus.RUNNING, RunStatus.QUEUED.transitionTo(RunStatus.RUNNING));
        assertEquals(RunStatus.SUCCEEDED, RunStatus.RUNNING.transitionTo(RunStatus.SUCCEEDED));
    }

    @Test
    void transitionToRejectsIllegalTransition() {
        assertThrows(IllegalStateException.class, () -> RunStatus.QUEUED.transitionTo(RunStatus.SUCCEEDED));
        assertThrows(IllegalStateException.class, () -> RunStatus.SUCCEEDED.transitionTo(RunStatus.FAILED));
    }
}
