package contrib.utils.multiplayer.packages.request;


import core.utils.Point;

import static java.util.Objects.requireNonNull;

public class UpdatePositionRequest {
    private final int entityGlobalID;
    private final Point position;
    private final float xVelocity;
    private final float yVelocity;

    public UpdatePositionRequest(
        final int entityGlobalID,
        final Point position,
        final float xVelocity,
        final float yVelocity){
        requireNonNull(position);
        this.entityGlobalID = entityGlobalID;
        this.position = position;
        this.xVelocity = xVelocity;
        this.yVelocity = yVelocity;
    }

    public int entityGlobalID() {
        return entityGlobalID;
    }

    public Point position() {
        return position;
    }

    public float xVelocity() {return xVelocity; }

    public float yVelocity() {return yVelocity; }
}
