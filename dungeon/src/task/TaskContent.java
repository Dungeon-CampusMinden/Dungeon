package task;

public abstract class TaskContent {
    private final Task task;

    public TaskContent(Task task) {
        this.task = task;
    }

    public Task task() {
        return task;
    }
}
