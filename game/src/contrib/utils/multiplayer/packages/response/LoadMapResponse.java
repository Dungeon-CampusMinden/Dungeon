package contrib.utils.multiplayer.packages.response;


import contrib.utils.multiplayer.packages.GameState;
import contrib.utils.multiplayer.packages.request.LoadMapRequest;

import static java.util.Objects.requireNonNull;

/**
 * Response of {@link LoadMapRequest}
 */
public class LoadMapResponse {

    private final boolean isSucceed;
    private final GameState gameState;

    /**
     * Create new instance.
     *
     * @param isSucceed State whether game state was set up or not.
     * @param gameState Game
     */
    public LoadMapResponse(final boolean isSucceed, final GameState gameState) {
        this.isSucceed = isSucceed;
        this.gameState = requireNonNull(gameState);
    }

    public boolean isSucceed() {
        return isSucceed;
    }

    public GameState gameState(){return gameState;}
}
