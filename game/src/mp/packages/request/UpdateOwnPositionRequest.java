package mp.packages.request;

import tools.Point;

import static java.util.Objects.requireNonNull;

public class UpdateOwnPositionRequest{
    private int clientId;
    private Point heroPosition;

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
