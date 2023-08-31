package petriNet;

import java.util.HashSet;
import java.util.Set;

public class Place {
    private int tokenCount = 0;
    private Set<Transition> observer = new HashSet<>();

    public void placeToken() {
        tokenCount++;
        observer.forEach(Transition::notify);
    }

    public int tokenCount() {
        return tokenCount;
    }

    public void register(Transition observer) {
        this.observer.add(observer);
    }
}
