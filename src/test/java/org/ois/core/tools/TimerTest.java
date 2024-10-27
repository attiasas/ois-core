package org.ois.core.tools;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class TimerTest {
    private Timer timer;

    @BeforeMethod
    public void setUp() {
        timer = new Timer(10);
    }

    @Test
    public void testInitialState() {
        assertEquals(timer.getTarget(), 10.0f);
        assertEquals(timer.timeLeftToTarget(), 10.0f);
        assertFalse(timer.isLoop());
        assertFalse(timer.isPaused());
        // Should not be over initially
        assertFalse(timer.tic(1));
    }

    @Test
    public void testIsOverExactTarget() {
        timer.tic(10);
        assertTrue(timer.tic(0));
    }

    @Test
    public void testPauseAndResume() {
        timer.tic(3);
        assertEquals(timer.timeLeftToTarget(), 7.0f);

        timer.pause();
        timer.tic(5);
        // Should not affect time because timer is paused
        assertEquals(timer.timeLeftToTarget(), 7.0f);

        timer.resume();
        timer.tic(2);
        // Should now affect time
        assertEquals(timer.timeLeftToTarget(), 5.0f);
    }

    @Test
    public void testOnFinishListener() {
        final boolean[] listenerTriggered = {false};
        timer.setOnFinishListener(() -> listenerTriggered[0] = true);

        // Listener should trigger at the target
        assertTrue(timer.tic(10));
        assertTrue(listenerTriggered[0]);
        // After the listener, it should be over
        assertTrue(timer.tic(0));
    }

    @Test
    public void testResetWithNewTarget() {
        // Reset with a new target
        timer.reset(20);
        assertEquals(timer.getTarget(), 20.0f);
        assertEquals(timer.timeLeftToTarget(), 20.0f);
    }

    @Test
    public void testTicWithLoopingAndListener() {
        final boolean[] listenerTriggered = {false};
        timer.setLoop(true);
        timer.setOnFinishListener(() -> listenerTriggered[0] = true);
        // With loop, it's not over
        assertFalse(timer.tic(10));
        // Listener triggered at 10 seconds
        assertTrue(listenerTriggered[0]);
        assertFalse(timer.tic(0));
        // Loop again, Listener triggered again at 20 seconds
        listenerTriggered[0] = false;
        timer.tic(10);
        assertTrue(listenerTriggered[0]);
    }

    @Test
    public void testTicWithLoopingNoListener() {
        timer.setLoop(true);
        // No listener, should not report "over"
        assertFalse(timer.tic(10));
        // Still not report
        assertFalse(timer.tic(10));
    }

    @Test
    public void testTimerResumesAfterPause() {
        timer.tic(5);
        assertEquals(timer.timeLeftToTarget(), 5.0f);
        // No change while paused
        timer.pause();
        timer.tic(5);
        assertEquals(timer.timeLeftToTarget(), 5.0f);
        // Should be over after resuming
        timer.resume();
        timer.tic(5);
        assertEquals(timer.timeLeftToTarget(), 0.0f);
        assertTrue(timer.tic(0));
    }

    @Test
    public void testOnFinishListenerWithoutLooping() {
        final boolean[] listenerTriggered = {false};
        timer.setOnFinishListener(() -> listenerTriggered[0] = true);
        // Listener should trigger
        assertTrue(timer.tic(10));
        assertTrue(listenerTriggered[0]);
        assertTrue(timer.tic(0));
    }
}
