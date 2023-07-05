package contrib.utils.multiplayer.packages.response;


import contrib.utils.multiplayer.packages.GameState;
import core.Entity;
import core.level.elements.ILevel;
import core.utils.Point;

import java.util.HashMap;

import static java.util.Objects.requireNonNull;

public class LoadMapResponse {

    private final boolean isSucceed;
    private final GameState gameState;

    public LoadMapResponse(final boolean isSucceed, final GameState gameState) {
        this.isSucceed = isSucceed;
        this.gameState = requireNonNull(gameState);
    }

    public boolean isSucceed() {
        return isSucceed;
    }

    public GameState gameState(){return gameState;}
}
