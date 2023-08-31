package petriNet;

import task.Task;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a Place in the Petri Net.
 *
 * <p>Stores an integer value as tokens. Add a token to the Place by calling {@link #placeToken()}.
 *
 * <p>Places are observed by {@link Transition}. If a new token is added to a Place, it will notify
 * all observer transitions. Use {@link #register(Transition)} to register a Transition as an
 * observer.
 *
 * <p>A Place can change the {@link task.Task.TaskState} of a {@link Task} to {@link
 * task.Task.TaskState#ACTIVE} if a Task is given in the constructor. This will only happen if the
 * state of the {@link Task} is {@link task.Task.TaskState#INACTIVE}.
 */
public class Place {
    private int tokenCount = 0;
    private final Set<Transition> observer = new HashSet<>();

    private final Task activate;

    /**
     * Create a new Place that will activate a {@link Task} if a Token is placed in, and the given
     * Task is in {@link task.Task.TaskState#INACTIVE}.
     *
     * @param activate Task to activate if a token is placed in this place.
     */
    public Place(Task activate) {
        this.activate = activate;
    }

    /** Create a new Place. */
    public Place() {
        this(null);
    }

    /**
     * Increase the token count of this place.
     *
     * <p>This will invoke {@link Transition#notify(Place)} for all observers.
     *
     * <p>If this Place has a Task to activate, this function will also activate it, if the state of
     * the task is {@link task.Task.TaskState#INACTIVE}
     */
    public void placeToken() {
        tokenCount++;
        observer.forEach(transition -> transition.notify(this));

        if (activate != null && activate.state() == Task.TaskState.INACTIVE)
            activate.state(Task.TaskState.ACTIVE);
    }

    /**
     * Retrieve the number of tokens in this place.
     *
     * @return The number of tokens in this place.
     */
    public int tokenCount() {
        return tokenCount;
    }

    /**
     * Register a {@link Transition} as an observer.
     *
     * <p>Observers are notified by {@link Transition#notify(Place)} when a token is added to this
     * place.
     *
     * @param observer The Transition serving as the observer.
     */
    public void register(Transition observer) {
        this.observer.add(observer);
    }
}
