package petriNet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents a Transition in a Petri net.
 *
 * <p>A Transition observes a Set of {@link Place}s. If each place has at least one token, the
 * Transition will fire.
 *
 * <p>If a Transition fires, it will place a Token in each {@link Place} defined in the constructor.
 */
public class Transition {

    private final Map<Place, Boolean> dependencyPlaces;
    private final Set<Place> addTokenOnFire;

    /**
     * Creates a new Transition.
     *
     * @param dependencyPlaces This Transition observes each of these {@link Place}s and will fire
     *     if each Place has at least one token. These are all Places that have an outgoing arc to
     *     this transition.
     * @param addTokenOnFire If the Transition fires, for each of these {@link Place}s, {@link
     *     Place#placeToken()} will be called. These are all Places that have an incoming arc from
     *     this transition.
     */
    public Transition(Set<Place> dependencyPlaces, Set<Place> addTokenOnFire) {
        this.addTokenOnFire = addTokenOnFire;
        this.dependencyPlaces = new HashMap<>();
        for (Place place : dependencyPlaces) {
            this.dependencyPlaces.put(place, place.tokenCount() > 0);
            place.register(this);
        }
    }

    /**
     * Notify a Transition that the given Place has increased or decreased its token count.
     *
     * @param place Place that has increased its token count.
     */
    public void notify(Place place) {
        if (place.tokenCount() > 0) {
            dependencyPlaces.replace(place, true);
            // if all places have a token fire
            if (dependencyPlaces.values().stream().allMatch(v -> v)) fire();
        } else dependencyPlaces.replace(place, false);
    }

    private void fire() {
        dependencyPlaces.keySet().forEach(Place::removeToken);
        addTokenOnFire.forEach(Place::placeToken);
    }
}
