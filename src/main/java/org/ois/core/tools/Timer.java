package org.ois.core.tools;

/**
 * A versatile timer utility class that tracks the passage of time relative to a target duration.
 * The timer supports both looping and non-looping modes, pausing, and listeners for when the timer is over.
 */
public class Timer {
    /** The target duration (in seconds) that the timer is counting toward. */
    public float target;

    /** The amount of time (in seconds) that has elapsed since the timer started. */
    public float elapsed;

    /** If true, the timer will reset and continue looping after reaching the target time. */
    public boolean loop;

    /** If true, the timer is paused and does not increment time. */
    private boolean paused;

    /** Listener that triggers when the timer reaches its target. */
    private Runnable onFinishListener;

    /**
     * Constructs a new Timer with the default target duration of {@link Float#MAX_VALUE}.
     */
    public Timer() {
        this(Float.MAX_VALUE);
    }

    /**
     * Constructs a new Timer with the specified target duration.
     *
     * @param target The target time (in seconds) for the timer.
     */
    public Timer(float target) {
        this.target = target;
        this.paused = false;
        this.onFinishListener = null;
    }

    /**
     * Returns the amount of time remaining until the timer reaches the target time.
     *
     * @return The time left (in seconds) before reaching the target.
     */
    public float timeLeftToTarget() {
        return target - elapsed;
    }

    /**
     * Resets the timer's elapsed time to 0.
     */
    public void reset() {
        this.elapsed = 0;
    }

    /**
     * Resets the timer's elapsed time and allows setting a new target time.
     *
     * @param newTarget The new target time (in seconds) for the timer.
     */
    public void reset(float newTarget) {
        this.target = newTarget;
        reset();
    }

    /**
     * Pauses the timer, preventing it from advancing.
     */
    public void pause() {
        this.paused = true;
    }

    /**
     * Checks if the timer is currently paused.
     *
     * @return true if the timer is paused, false otherwise.
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Resumes the timer if it is paused.
     */
    public void resume() {
        this.paused = false;
    }

    /**
     * Gets the target time of the timer.
     *
     * @return The target time (in seconds).
     */
    public float getTarget() {
        return target;
    }

    /**
     * Sets a new target time for the timer.
     *
     * @param target The new target time (in seconds).
     */
    public void setTarget(float target) {
        this.target = target;
    }

    /**
     * Checks if the timer is set to loop after reaching the target time.
     *
     * @return true if the timer loops, false otherwise.
     */
    public boolean isLoop() {
        return loop;
    }

    /**
     * Enables or disables looping behavior for the timer.
     *
     * @param loop true to enable looping, false to disable.
     */
    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    /**
     * Registers a listener that will be triggered when the timer reaches its target.
     *
     * @param listener A {@link Runnable} that will be executed when the timer is over.
     */
    public void setOnFinishListener(Runnable listener) {
        this.onFinishListener = listener;
    }

    /**
     * Advances the timer by the specified amount of time.
     * <p>
     * This method increases the timer's {@code elapsed} time by the given {@code deltaTime}.
     * If the timer is paused, no time will be added, and the method will return {@code false}.
     * </p>
     * <p>
     * If the timer has exceeded the target time, the behavior depends on whether the timer is in looping mode:
     * </p>
     * <ul>
     *     <li>If {@code loop} is enabled, the timer will reset the elapsed time to continue running and
     *     trigger any registered {@code onFinishListener}.</li>
     *     <li>If {@code loop} is disabled, the timer will stop and return {@code true} to indicate that it
     *     has finished.</li>
     * </ul>
     * <p>
     * If the timer reaches or exceeds the target time and an {@code onFinishListener} is registered, it will be
     * executed exactly once when the target time is reached or exceeded. If looping is enabled, the listener
     * will trigger at each interval where the timer surpasses the target time.
     * </p>
     *
     * <p>
     * Special Cases:
     * <ul>
     *     <li>If the timer is paused, the elapsed time will not increase, and the method returns {@code false}.</li>
     *     <li>If the elapsed time surpasses the target time and looping is disabled, this method will return
     *     {@code true}, indicating that the timer has finished.</li>
     * </ul>
     * </p>
     *
     * @param deltaTime The amount of time (in seconds) to add to the timer's elapsed time. This value is typically
     *                  the time that has passed since the last call to {@code tic()}.
     * @return {@code true} if the timer is over and not looping after the advancement, {@code false} otherwise.
     * <p>
     * Returns {@code false} if the timer is paused, or if the timer continues running (either because the target
     * time hasn't been reached yet, or because looping is enabled).
     * </p>
     */
    public boolean tic(float deltaTime) {
        if (paused) {
            // Don't advance time if paused
            return false;
        }

        if (!loop && elapsed >= target) {
            return true;
        }

        elapsed += deltaTime;

        if (elapsed >= target) {
            if (onFinishListener != null) {
                onFinishListener.run();
            }
            if (loop) {
                elapsed -= target;
            } else {
                return true;
            }
        }

        return false;
    }
}
