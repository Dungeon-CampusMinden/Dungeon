package contrib.utils.multiplayer.packages.request;

import contrib.utils.multiplayer.packages.Version;
import core.Entity;

import static java.util.Objects.requireNonNull;

public class JoinSessionRequest {
//    private final Version clientVersion;
    private final Entity hero;

    public JoinSessionRequest(final Entity hero) {
//        this.clientVersion = requireNonNull(clientVersion);
        this.hero = hero;
    }

//    public Version clientVersion() {return this.clientVersion;}

    public Entity hero() { return this.hero;}
}
