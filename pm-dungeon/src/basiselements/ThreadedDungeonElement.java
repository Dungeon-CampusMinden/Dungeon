package basiselements;

import tools.Point;

/**
 * A thread based DungeonElement, which will have all its logic be executed in an own thread
 *
 * @author Maxim Fruendt
 */
public abstract class ThreadedDungeonElement implements Runnable {

    /** Current position in the level */
    protected Point position;
    /** Fake element that belongs to this threaded element. Used for rendering */
    protected ThreadedFakeDungeonElement fakeElement;

    /** Flag if this thread should be running */
    private volatile boolean running = true;
    /** Flag if this thread is paused */
    private volatile boolean paused = false;
    /** Lock for pausing this thread */
    private final Object pauseLock = new Object();

    /**
     * Creates a new element of which the logic will be executed in a thread
     *
     * @param spawnPosition Position where this element will spawn
     */
    public ThreadedDungeonElement(Point spawnPosition) {
        position = spawnPosition;
    }

    /**
     * Set the fake element of this element, which must be placed in the main thread
     *
     * @param fakeElement Fake element used to visualize this threaded element
     */
    public void setFakeElement(ThreadedFakeDungeonElement fakeElement) {
        this.fakeElement = fakeElement;
    }

    /**
     * Get the fake element of this threaded element
     *
     * @return Fake element
     */
    public ThreadedFakeDungeonElement getFakeElement() {
        return fakeElement;
    }

    /** Function that updates the logic of the threaded element */
    protected abstract void update();

    /**
     * Get the texture path of this threaded element
     *
     * @return Path of the active texture
     */
    public abstract String getTexturePath();

    /**
     * Get the position in the level
     *
     * @return Position of this threaded element
     */
    public Point getPosition() {
        return position;
    }

    /** Pauses the execution of logic */
    public void pause() {
        paused = true;
    }

    /** Resumes the execution of logic */
    public void resume() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll();
        }
    }

    /** Stops this runnable */
    public void stop() {
        running = false;
        resume();
    }

    /** Run this runnable */
    @Override
    public void run() {
        // Check if we should pause
        synchronized (pauseLock) {
            if (!running) {
                // Leave if this should no longer run
                return;
            }
            // If we should pause, wait
            if (paused) {
                try {
                    pauseLock.wait();
                } catch (InterruptedException ignored) {
                    return;
                }
                if (!running) {
                    // Leave if this should no longer run
                    return;
                }
            }
        }
        // Update logic of this threaded element
        update();
        // Update the sprite of this threaded element
        if (fakeElement != null) {
            fakeElement.setPosition(position);
            fakeElement.updateTexture(getTexturePath());
        }
    }
}
