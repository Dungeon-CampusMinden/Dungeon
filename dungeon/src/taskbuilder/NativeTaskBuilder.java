package taskbuilder;

import task.Task;

import java.util.Optional;

public class NativeTaskBuilder implements ITaskBuilder {
    @Override
    public Optional<Object> buildTask(Task task) {
        return Optional.empty();
    }
}
