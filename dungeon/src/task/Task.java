package task;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public abstract class Task {
    private static final String DEFAULT_TASK_TEXT = "No task description provided";
    private static final TaskState DEFAULT_TASK_STATE = TaskState.DEACTIVE;
    private TaskState state;
    private String taskText;
    private TaskManagerComponent managementComponent;
    private Set<TaskContent> content = new HashSet<>();

    public Task(
            TaskState state,
            String taskText,
            TaskManagerComponent component,
            Set<TaskContent> content) {
        this.state = state;
        this.taskText = taskText;
        this.managementComponent = component;
        this.content = content;
    }

    public Task(TaskState state, String taskText, TaskManagerComponent component) {
        this(state, taskText, component, new HashSet<>());
    }

    public Task(TaskState state, String taskText) {
        this(state, taskText, null);
    }

    public Task(String taskText) {
        this(DEFAULT_TASK_STATE, taskText);
    }

    public Task() {
        this(DEFAULT_TASK_TEXT);
    }

    public TaskState state() {
        return state;
    }

    public void state(TaskState state) {
        this.state = state;
    }

    public String taskText() {
        return taskText;
    }

    public void taskText(String taskText) {
        this.taskText = taskText;
    }

    public TaskManagerComponent managerComponent() {
        return managementComponent;
    }

    public void managerComponent(TaskManagerComponent component) {
        this.managementComponent = component;
    }

    public Stream<TaskContent> contentStream() {
        return content.stream();
    }

    public void content(Set<TaskContent> content) {
        this.content = content;
    }

    public void addContent(TaskContent content) {
        this.content.add(content);
    }

    public void removeContent(TaskContent content) {
        this.content.remove(content);
    }

    public boolean hasContent(TaskContent content) {
        return this.content.contains(content);
    }

    public enum TaskState {
        ACTIVE,
        DEACTIVE,
        FINISHED
    }
}
