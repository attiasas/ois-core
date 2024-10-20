package org.ois.core.tools;

public class Timer {
    public float target;
    public float elapsed;
    public boolean loop;

    public Timer() {
        this(Float.MAX_VALUE);
    }

    public Timer(float target) {
        this.target = target;
    }

    public float timeLeftToTarget() {
        return target - elapsed;
    }

    public boolean isOver() {
        return elapsed >= target;
    }

    public void reset() {
        this.elapsed = 0;
    }

    public float getTarget() {
        return target;
    }

    public void setTarget(float target) {
        this.target = target;
    }

    public boolean isLoop() {
        return loop;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    /**
     * Advance the timer progress.
     * @param deltaTime - the time passed since the last 'tic'
     * @return true if after the tic the timer isOver
     */
    public boolean tic(float deltaTime) {
        if (isOver() && !loop) {
            return true;
        }
        elapsed += deltaTime;
        if (isOver() && loop) {
            elapsed -= target;
            return true;
        }
        return false;
    }
}
