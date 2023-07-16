package contrib.utils.multiplayer.network.packages.response;

import static java.util.Objects.requireNonNull;

import contrib.utils.multiplayer.network.packages.GameState;
import contrib.utils.multiplayer.network.packages.request.JoinSessionRequest;

import core.utils.Point;

/** Response of {@link JoinSessionRequest} */
public class JoinSessionResponse {

    private final boolean isSucceed;
    private final int heroGlobalID;
    private final GameState gameState;
    private final Point initialPosition;

    /**
     * Create new instance.
     *
     * @param isSucceed State, whether server accepted join request or not.
     * @param heroGlobalID From server assigned global ID for playable hero.
     * @param gameState Game state when session joined.
     * @param initialPosition From server assigned start position of playable hero. Has to be set
     *     locally.
     */
    public JoinSessionResponse(
            final boolean isSucceed,
            final int heroGlobalID,
            final GameState gameState,
            final Point initialPosition) {

        this.isSucceed = isSucceed;
        this.heroGlobalID = heroGlobalID;
        this.gameState = requireNonNull(gameState);
        this.initialPosition = requireNonNull(initialPosition);
    }

    /**
     * @return State, whether server accepted join request or not
     */
    public boolean isSucceed() {
        return isSucceed;
    }

    /**
     * @return From server assigned global ID for playable hero.
     */
    public Integer heroGlobalID() {
        return this.heroGlobalID;
    }

    /**
     * @return Game state when session joined.
     */
    public GameState gameState() {
        return this.gameState;
    }

    /**
     * @return From server assigned start position of playable hero.
     */
    public Point initialPosition() {
        return this.initialPosition;
    }
}
