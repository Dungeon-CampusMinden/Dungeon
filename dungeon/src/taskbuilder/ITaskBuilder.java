package taskbuilder;

import task.Task;

import java.util.Optional;

public interface ITaskBuilder {

    /**
     * Execute a scenario builder method for the given {@link Task}.
     *
     * @param task The {@link Task} to execute a scenario builder method for.
     * @return An {@link Optional} containing the Java-Object which was instantiated from the return
     *     value of the scenario builder. The content inside the {@link Optional} will be of type
     *     HashSet<HashSet<core.Entity>>. If the execution of the scenario builder method was
     *     unsuccessful or no fitting scenario builder method for the given {@link Task} could be
     *     found, an empty {@link Optional} will be returned.
     */
    Optional<Object> buildTask(Task task);
}
