package controller;

import basiselements.DungeonElement;
import basiselements.ThreadedDungeonElement;
import basiselements.ThreadedFakeDungeonElement;
import graphic.Painter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import tools.Constants;

/**
 * A class to manage <code>DungeonElement</code>s.
 *
 * <p>On each <code>DungeonElement</code> the update and draw method be are called.
 */
public class EntityController extends AbstractController<DungeonElement> {
    Painter painter;
    /** List of registered threaded characters */
    protected final HashSet<ThreadedDungeonElement> threadedDungeonElements;
    /** Scheduler used to run threaded characters */
    protected final ScheduledExecutorService scheduler;
    /** Time in milliseconds between two frames */
    protected final long MS_PER_FRAME = (long) (1f / (float) Constants.FRAME_RATE * 1000f);
    /** Initial delay before a threaded character thread is started */
    protected final long THREAD_START_DELAY_MS = 0;
    /** List of started dungeon element threads */
    protected final List<Future<?>> threadedDungeonElementThreads;
    /** Flag if this is currently paused */
    protected boolean isPaused = false;

    public EntityController(Painter painter) {
        super();
        threadedDungeonElements = new HashSet<>();
        threadedDungeonElementThreads = new ArrayList<>();
        scheduler = new ScheduledThreadPoolExecutor(Constants.CORE_POOL_SIZE);
        this.painter = painter;
    }

    /** Pause the execution of threaded entity, which are registered in this controller. */
    public void pause() {
        threadedDungeonElements.forEach(ThreadedDungeonElement::pause);
        threadedDungeonElementThreads.forEach((e) -> e.cancel(true));
        isPaused = true;
    }

    /** Resume the execution of threaded entity, which are registered in this controller. */
    public void resume() {
        for (ThreadedDungeonElement element : threadedDungeonElements) {
            threadedDungeonElementThreads.add(
                    scheduler.scheduleAtFixedRate(
                            element, THREAD_START_DELAY_MS, MS_PER_FRAME, TimeUnit.MILLISECONDS));
            element.resume();
        }
        isPaused = true;
    }

    /** Stop the execution of threaded entity, which are registered in this controller. */
    public void stop() {
        threadedDungeonElements.forEach(ThreadedDungeonElement::stop);
        scheduler.shutdown();
    }

    /**
     * Adds a threaded dungeon element with default layer (20) to this controller, if it is not
     * already added.
     *
     * @param e Element to add.
     * @return true, if this was successful.
     */
    public boolean add(ThreadedDungeonElement e) {
        if (!threadedDungeonElements.add(e)) {
            return false;
        }
        ThreadedFakeDungeonElement fakeElement =
                new ThreadedFakeDungeonElement(e.getTexturePath(), e.getPosition());
        e.setFakeElement(fakeElement);
        if (!add(fakeElement)) {
            return false;
        }
        if (!isPaused) {
            threadedDungeonElementThreads.add(
                    scheduler.scheduleAtFixedRate(
                            e, THREAD_START_DELAY_MS, MS_PER_FRAME, TimeUnit.MILLISECONDS));
        }
        return true;
    }

    /**
     * Removes the threaded dungeon element from this controller, if it is in this controller.
     *
     * @param e Element to remove.
     * @return true, if this was successful.
     */
    public boolean remove(ThreadedDungeonElement e) {
        assert e != null;
        e.stop();
        if (!remove(e.getFakeElement())) {
            return false;
        }
        e.setFakeElement(null);
        return threadedDungeonElements.remove(e);
    }

    /**
     * Updates all elements that are registered at this controller, removes deletable elements and
     * calls the update and draw method for every registered element.
     */
    @Override
    public void process(DungeonElement e) {
        e.update();
        e.draw(painter);
    }
}
