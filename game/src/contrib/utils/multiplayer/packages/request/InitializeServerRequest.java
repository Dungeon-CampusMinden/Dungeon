package contrib.utils.multiplayer.packages.request;

import contrib.utils.multiplayer.packages.Version;
import contrib.utils.multiplayer.packages.response.InitializeServerResponse;

/**
 * Used to request server to initialize for hosting multiplayer session.
 *
 * <p>According response would be {@link InitializeServerResponse}
 */
public class InitializeServerRequest {
    private final Version clientVersion;

    /**
     * Creates a new instance.
     *
     * @param clientVersion Client version that want to host multiplayer session. Has to be same as
     *     server version.
     */
    public InitializeServerRequest(final Version clientVersion) {
        this.clientVersion = clientVersion;
    }

    /**
     * @return Client version.
     */
    public Version clientVersion() {
        return this.clientVersion;
    }
}
