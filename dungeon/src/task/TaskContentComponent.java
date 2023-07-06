package task;

import core.Component;
import core.Entity;

public class TaskContentComponent extends Component {
    /**
     * Create a new component and add it to the associated entity
     *
     * @param entity associated entity
     */
    public TaskContentComponent(Entity entity) {
        super(entity);
    }
}
