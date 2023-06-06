package core.components;

import core.Component;
import core.Entity;

/** The CameraComponent marks an entity as the point of focus for the camera. */
public class CameraComponent extends Component {

    /**
     * Create a new CameraComponent and add it to the associated entity.
     *
     * @param entity entity which should be the focus point
     */
    public CameraComponent(Entity entity) {
        super(entity);
    }
}
