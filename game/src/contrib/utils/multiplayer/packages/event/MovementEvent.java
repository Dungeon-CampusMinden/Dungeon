package contrib.utils.multiplayer.packages.event;

import static java.util.Objects.requireNonNull;

import core.utils.Point;

/**
 * Used to inform server about movement of an entity, so that server can synchronize over all
 * clients.
 */
public class MovementEvent {
    private final int entityGlobalID;
    private final Point position;
    private final float xVelocity;
    private final float yVelocity;

    /**
     * Create a new instance.
     *
     * @param entityGlobalID Global ID of entity that has been moved.
     * @param newPosition New position of the entity calculated by the client. Needs for validation
     *     on server side.
     * @param xVelocity X velocity the entity has on movement.
     * @param yVelocity Y velocity the entity has on movement.
     */
    public MovementEvent(
            final int entityGlobalID,
            final Point newPosition,
            final float xVelocity,
            final float yVelocity) {
        requireNonNull(newPosition);
        this.entityGlobalID = entityGlobalID;
        this.position = newPosition;
        this.xVelocity = xVelocity;
        this.yVelocity = yVelocity;
    }

    /**
     * @return Global ID of the moved entity.
     */
    public int entityGlobalID() {
        return entityGlobalID;
    }

    /**
     * @return New position of the moved entity.
     */
    public Point position() {
        return position;
    }

    /**
     * @return X velocity on movement.
     */
    public float xVelocity() {
        return xVelocity;
    }

    /**
     * @return > velocity on movement.
     */
    public float yVelocity() {
        return yVelocity;
    }
}
