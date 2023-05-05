package mp.packages.response;

import level.elements.ILevel;
import tools.Point;

import java.util.HashMap;

public class JoinSessionResponse {

    private final ILevel level;
    private final Integer clientId;
    private final HashMap<Integer, Point> heroPositionByClientId;

    public JoinSessionResponse(
        final ILevel level,
        final Integer clientId,
        final HashMap<Integer, Point> heroPositionByClientId) {

        this.level = level;
        this.clientId = clientId;
        this.heroPositionByClientId = heroPositionByClientId;
    }

    public ILevel getLevel() {
        return level;
    }

    public HashMap<Integer, Point> getHeroPositionByClientId() {
        return this.heroPositionByClientId;
    }

    public Integer getClientId() { return this.clientId; }
}
