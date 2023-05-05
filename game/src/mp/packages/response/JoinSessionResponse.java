package mp.packages.response;

import level.elements.ILevel;
import tools.Point;

import java.util.HashMap;

import static java.util.Objects.requireNonNull;

public class JoinSessionResponse {

    private final boolean isSucceed;
    private final ILevel level;
    private final Integer clientId;
    private final HashMap<Integer, Point> heroPositionByClientId;

    public JoinSessionResponse(
        final boolean isSucceed,
        final ILevel level,
        final Integer clientId,
        final HashMap<Integer, Point> heroPositionByClientId) {

        this.isSucceed = isSucceed;
        this.level = requireNonNull(level);
        this.clientId = requireNonNull(clientId);
        this.heroPositionByClientId = requireNonNull(heroPositionByClientId);
    }

    public boolean getIsSucceed() {
        return isSucceed;
    }

    public ILevel getLevel() {
        return level;
    }

    public HashMap<Integer, Point> getHeroPositionByClientId() {
        return this.heroPositionByClientId;
    }

    public Integer getClientId() { return this.clientId; }
}
