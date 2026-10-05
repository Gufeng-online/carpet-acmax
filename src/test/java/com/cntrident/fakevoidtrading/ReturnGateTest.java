package com.cntrident.fakevoidtrading;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReturnGateTest {
    @Test void quickStorageWaitsOnlyTheRemainderSinceArrival() {
        var gate = new ReturnGate(); gate.start(100, 2.0);
        gate.finishStorage();
        assertEquals(10, gate.remaining(130)); assertFalse(gate.ready(130));
        assertTrue(gate.ready(140));
    }
    @Test void slowStorageCannotBeInterruptedByTheTimer() {
        var gate = new ReturnGate(); gate.start(100, 2.0);
        assertEquals(0, gate.remaining(180)); assertFalse(gate.ready(180));
        gate.finishStorage(); assertTrue(gate.ready(180));
    }
    @Test void customDurationAndNewRoundRequireStorageAgain() {
        var gate = new ReturnGate(); gate.start(0, 0.125); gate.finishStorage();
        assertFalse(gate.ready(2)); assertTrue(gate.ready(3));
        gate.start(200, 3.0); assertFalse(gate.ready(260));
        gate.finishStorage(); assertTrue(gate.ready(260));
    }
    @Test void zeroCooldownStillRequiresStorageAndInvalidValuesHaveBounds() {
        var gate = new ReturnGate(); assertFalse(gate.ready(100));
        gate.start(100, 0); assertFalse(gate.ready(100));
        gate.finishStorage(); assertTrue(gate.ready(100));
        assertEquals(2.0, ReturnGate.sanitizeSeconds(Double.NaN));
        assertEquals(2.0, ReturnGate.sanitizeSeconds(Double.POSITIVE_INFINITY));
        assertEquals(0.0, ReturnGate.sanitizeSeconds(-1));
        assertEquals(60.0, ReturnGate.sanitizeSeconds(1e9));
    }
}
