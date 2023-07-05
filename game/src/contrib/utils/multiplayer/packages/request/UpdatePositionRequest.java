package contrib.utils.multiplayer.packages.request;


import core.utils.Point;

import static java.util.Objects.requireNonNull;

public class UpdatePositionRequest {
    private final int entityGlobalID;
    private final Point position;

    public UpdatePositionRequest(final int entityGlobalID, final Point position){
        requireNonNull(position);
        this.entityGlobalID = entityGlobalID;
        this.position = position;
    }

    public int entityGlobalID() {
        return entityGlobalID;
    }

    public Point position() {
        return position;
    }
}
