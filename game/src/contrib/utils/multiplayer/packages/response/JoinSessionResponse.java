package contrib.utils.multiplayer.packages.response;


import contrib.utils.multiplayer.packages.GameState;
import core.utils.Point;

import static java.util.Objects.requireNonNull;

public class JoinSessionResponse {

    private final boolean isSucceed;
    private final int heroGlobalID;
    private final GameState gameState;
    private final Point initialPosition;

    public JoinSessionResponse(
        final boolean isSucceed,
        final int heroGlobalID,
        final GameState gameState,
        final Point initialPosition) {

        this.isSucceed = isSucceed;
        this.heroGlobalID = requireNonNull(heroGlobalID);
        this.gameState = requireNonNull(gameState);
        this.initialPosition = requireNonNull(initialPosition);
    }

    public boolean isSucceed() {
        return isSucceed;
    }

    public Integer heroGlobalID() { return this.heroGlobalID; }

    public GameState gameState() { return this.gameState; }

    public Point initialPosition() { return this.initialPosition; }
}
