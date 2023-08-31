package petriNet;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a Place in the Petri Net.
 *
 * <p>Stores an integer value as tokens. Add a token to the Place by calling {@link #placeToken()}.
 *
 * <p>Places are observed by {@link Transition}. If a new token is added to a Place, it will notify
 * all observer transitions. Use {@link #register(Transition)} to register a Transition as an
 * observer, and {@link #deregister(Transition)} to deregister an observer.
 */
public class Place {
    private int tokenCount = 0;
    private final Set<Transition> observer = new HashSet<>();

    /**
     * Increase the token count of this place.
     *
     * <p>This will invoke {@link Transition#notify(Place)} for all observers.
     */
    public void placeToken() {
        tokenCount++;
        observer.forEach(transition -> transition.notify(this));
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

    /**
     * Remove the given Transition as an observer.
     *
     * @param observer Transition to remove as an observer.
     */
    public void deregister(Transition observer) {
        this.observer.remove(observer);
    }
}
