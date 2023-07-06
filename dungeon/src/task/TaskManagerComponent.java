package task;

import core.Component;
import core.Entity;

public final class TaskManagerComponent extends Component {

    private final Task task;

    public TaskManagerComponent(Entity entity, Task task) {
        super(entity);
        this.task = task;
    }

    public Task task() {
        return task;
    }
}
