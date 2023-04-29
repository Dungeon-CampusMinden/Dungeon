package mp.packages.event;

import tools.Point;

import java.util.HashMap;

import static java.util.Objects.requireNonNull;

public class HeroPositionsChangedEvent {
    private HashMap<Integer, Point> heroPositionByClientId = new HashMap<Integer, Point>();

    public HeroPositionsChangedEvent(final HashMap<Integer, Point> heroPositionByClientId){
        requireNonNull(heroPositionByClientId);
        this.heroPositionByClientId = heroPositionByClientId;
    }

    public HashMap<Integer, Point> getHeroPositionByClientId() {
        return heroPositionByClientId;
    }
}
