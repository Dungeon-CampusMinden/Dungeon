package contrib.utils.multiplayer.packages.request;

import contrib.utils.multiplayer.packages.Version;
import contrib.utils.multiplayer.packages.response.JoinSessionResponse;
import core.Entity;

import static java.util.Objects.requireNonNull;

/**
 * Used to request joining multiplayer session.
 * <p>According response would be {@link JoinSessionResponse}
 */
public class JoinSessionRequest {
    private final Version clientVersion;
    private final Entity hero;

    /**
     * Create a new instance.
     *
     * @param hero Playable hero instance of the client who wants to join.
     * @param clientVersion Version of client who wants to join. Has to be same as server version.
     */
    public JoinSessionRequest(final Entity hero, final Version clientVersion) {
        this.clientVersion = requireNonNull(clientVersion);
        this.hero = hero;
    }

    /**
     * @return Client version.
     */
    public Version clientVersion() {return this.clientVersion;}

    /**
     * @return Playable hero.
     */
    public Entity hero() { return this.hero;}
}
