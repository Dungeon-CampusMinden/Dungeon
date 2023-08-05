package task;

import core.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Marks an entity as a representation of one or more {@link TaskContent}.
 *
 * <p>Stores a collection of {@link TaskContent}s that are represented by the associated entity.
 *
 * <p>Using this component, a connection can be made between the in-game entity and the elements
 * from the task description.
 *
 * <p>The collection can be queried as a stream using {@link #stream()}
 */
public final class TaskContentComponent implements Component {

    private final Set<TaskContent> content;

    /**
     * Create a new TaskContentComponent and add it to the associated entity.
     *
     * @param content Collection of {@link TaskContent} that this Component represent
     */
    public TaskContentComponent(final Set<TaskContent> content) {
        this.content = new HashSet<>(content);
    }

    /**
     * Create a new TaskContentComponent and add it to the associated entity.
     *
     * @param content Single {@link TaskContent} that this Component represent
     */
    public TaskContentComponent(final TaskContent content) {
        this.content = new HashSet<>();
        this.content.add(content);
    }

    /**
     * Return the internal Set of represented {@link TaskContent}s as Stream.
     *
     * @return internal Set as stream
     */
    public Stream<TaskContent> stream() {
        return content.stream();
    }
}
