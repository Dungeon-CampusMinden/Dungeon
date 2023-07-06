package task;

import core.Component;
import core.Entity;

public final class TaskManagerComponent extends Component {
    /**
     * Create a new component and add it to the associated entity
     *
     * @param entity associated entity
     */
    public TaskManagerComponent(Entity entity) {
        super(entity);
    }
}
