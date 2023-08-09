package contrib.utils.multiplayer.network.packages.request;

import contrib.utils.multiplayer.network.packages.response.JoinSessionResponse;

import core.Entity;

/**
 * Used to request joining multiplayer session.
 *
 * <p>According response would be {@link JoinSessionResponse}
 */
public class JoinSessionRequest {
    private final Entity hero;

    /**
     * Create a new instance.
     *
     * @param hero Playable hero instance of the client who wants to join.
     */
    public JoinSessionRequest(final Entity hero) {
        this.hero = hero;
    }

    /**
     * @return Playable hero.
     */
    public Entity hero() {
        return this.hero;
    }
}
