package contrib.utils.multiplayer.packages.request;

import contrib.utils.multiplayer.packages.Version;

public class InitServerRequest {
    private final Version clientVersion;

    public InitServerRequest(final Version clientVersion) {
        this.clientVersion = clientVersion;
    }

    public Version clientVersion() {return this.clientVersion;}
}
