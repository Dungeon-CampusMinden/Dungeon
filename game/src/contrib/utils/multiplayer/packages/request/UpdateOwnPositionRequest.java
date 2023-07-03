package contrib.utils.multiplayer.packages.request;


import core.utils.Point;

import static java.util.Objects.requireNonNull;

public class UpdateOwnPositionRequest{
    private final int clientId;
    private final Point heroPosition;

    public UpdateOwnPositionRequest(final int clientId, final Point heroPosition){
        requireNonNull(heroPosition);
        this.clientId = clientId;
        this.heroPosition = heroPosition;
    }

    public int getClientId() {
        return clientId;
    }

    public Point getHeroPosition() {
        return heroPosition;
    }
}
