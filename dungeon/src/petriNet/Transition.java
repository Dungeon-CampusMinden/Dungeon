package petriNet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Transition {

    private final Map<Place, Boolean> dependencyPlaces;
    private final Set<Place> addTokenIfTriggerd;

    private final Set<Place> addTokenOnFire;

    public Transition(
            Set<Place> dependencyPlaces, Set<Place> addTokenIfTriggerd, Set<Place> addTokenOnFire) {
        this.addTokenOnFire = addTokenOnFire;
        this.dependencyPlaces = new HashMap<>();
        for (Place place : dependencyPlaces) {
            this.dependencyPlaces.put(place, place.tokenCount() > 0);
            place.register(this);
        }
        this.addTokenIfTriggerd = addTokenIfTriggerd;
    }

    public void notify(Place place) {
        dependencyPlaces.replace(place, true);

        // if all places have a token fire
        if (dependencyPlaces.values().stream().allMatch(v -> v == true)) fire();
    }

    private void fire() {
        addTokenOnFire.forEach(Place::placeToken);
    }
}
