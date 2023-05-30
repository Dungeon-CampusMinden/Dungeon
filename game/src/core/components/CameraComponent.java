package core.components;

import core.Component;
import core.Entity;

public class CameraComponent extends Component {

    /**
     * Create a new component and add it to the associated entity
     *
     * @param entity associated entity
     */
    public CameraComponent(Entity entity) {
        super(entity);
    }
}
