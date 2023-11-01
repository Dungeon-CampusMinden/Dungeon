package taskbuilder;

import task.Task;

import java.util.Optional;

public interface ITaskBuilder {

    Optional<Object> buildTask(Task task);
}
