package task.components;

import core.Component;

import semanticanalysis.types.DSLTypeProperty;
import semanticanalysis.types.IDSLTypeProperty;
import task.Task;
import task.TaskContent;

/**
 * Marks an entity as a representation of one or more {@link TaskContent}.
 *
 * <p>Stores a {@link TaskContent}s that is represented by the associated entity.
 *
 * <p>Using this component, a connection can be made between the in-game entity and the elements
 * from the task description.
 *
 * <p>The collection can be queried as a stream using {@link #content()}
 */
public final class TaskContentComponent implements Component {

    private TaskContent content;

    /**
     * Create a new TaskContentComponent and add it to the associated entity.
     *
     * @param content Single {@link TaskContent} that this Component represent
     */
    public TaskContentComponent(final TaskContent content) {
        this.content = content;
    }

    /**
     * Create a new TaskContentComponent.
     */
    public TaskContentComponent() { }

    /**
     * Return the internal Set of represented {@link TaskContent}s as Stream.
     *
     * @return internal Set as stream
     */
    public TaskContent content() {
        return content;
    }
}
