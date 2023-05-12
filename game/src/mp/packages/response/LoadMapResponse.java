package mp.packages.response;

import level.elements.ILevel;
import tools.Point;

import java.util.HashMap;

import static java.util.Objects.requireNonNull;

public class LoadMapResponse {

    private final boolean isSucceed;
    private final HashMap<Integer, Point> heroPositionByClientId;
    private final ILevel level;

    public LoadMapResponse(final boolean isSucceed, final ILevel level, final HashMap<Integer, Point> heroPositionByClientId) {
        requireNonNull(heroPositionByClientId);
        this.isSucceed = isSucceed;
        this.level = level;
        this.heroPositionByClientId = heroPositionByClientId;
    }

    public boolean getIsSucceed() {
        return isSucceed;
    }

    public HashMap<Integer, Point> getHeroPositionByClientId() {
        return heroPositionByClientId;
    }

    public ILevel getLevel(){return level;}
}
