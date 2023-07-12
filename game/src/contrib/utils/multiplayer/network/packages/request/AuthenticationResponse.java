package contrib.utils.multiplayer.network.packages.request;

import contrib.utils.multiplayer.network.packages.Version;
import contrib.utils.multiplayer.network.packages.response.AuthenticationRequest;

/**
 * Used to request server to initialize for hosting multiplayer session.
 *
 * <p>According response would be {@link AuthenticationRequest}
 */
public class AuthenticationResponse {
    private final Version clientVersion;

    /**
     * Creates a new instance.
     *
     * @param clientVersion Client version that want to host multiplayer session. Has to be same as
     *     server version.
     */
    public AuthenticationResponse(final Version clientVersion) {
        this.clientVersion = clientVersion;
    }

    /**
     * @return Client version.
     */
    public Version clientVersion() {
        return this.clientVersion;
    }
}
