package task;

import core.Component;
import core.Entity;

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
 * <p>The collection can be manipulated using {@link #add(TaskContent)} and {@link
 * #remove(TaskContent)}.
 *
 * <p>The collection can be queried as a stream using {@link #stream()}, and {@link
 * #contains(TaskContent)} can be used to check if the given element is stored in the collection.
 */
public final class TaskContentComponent extends Component {

    private final Set<TaskContent> content;

    /**
     * Create a new TaskContentComponent and add it to the associated entity.
     *
     * @param entity associated entity
     * @param content Collection of {@link TaskContent} that this Component represent
     */
    public TaskContentComponent(final Entity entity, final Set<TaskContent> content) {
        super(entity);
        this.content = new HashSet<>(content);
    }

    /**
     * Create a new TaskContentComponent and add it to the associated entity.
     *
     * @param entity associated entity
     * @param content Single {@link TaskContent} that this Component represent
     */
    public TaskContentComponent(final Entity entity, final TaskContent content) {
        super(entity);
        this.content = new HashSet<>();
        add(content);
    }

    /**
     * Create a new TaskContentComponent with an empty Collection of represented {@link
     * TaskContent}s and add it to the associated entity.
     *
     * @param entity associated entity
     */
    public TaskContentComponent(final Entity entity) {
        this(entity, new HashSet<>());
    }

    /**
     * Return the internal Set of represented {@link TaskContent}s as Stream.
     *
     * @return internal Set as stream
     */
    public Stream<TaskContent> stream() {
        return content.stream();
    }

    /**
     * Checks if the internal Set of represented {@link TaskContent}s contains the given element.
     *
     * @param content TaskContent to check for
     * @return true if the element is in the internal set, false if not
     */
    public boolean contains(final TaskContent content) {
        return this.content.contains(content);
    }

    /**
     * Add given Element to the internal set of represented {@link TaskContent}s.
     *
     * @param content element to add to the internal set
     */
    public void add(final TaskContent content) {
        this.content.add(content);
    }

    /**
     * Remove given Element from the internal set of represented {@link TaskContent}s.
     *
     * @param content element to remove from the internal set
     */
    public void remove(final TaskContent content) {
        this.content.remove(content);
    }
}
