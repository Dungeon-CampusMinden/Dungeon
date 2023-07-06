package task;

import core.Component;
import core.Entity;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public final class TaskContentComponent extends Component {

    private final Set<TaskContent> content;

    public TaskContentComponent(final Entity entity, final Set<TaskContent> content) {
        super(entity);
        this.content = new HashSet<>(content);
    }

    public TaskContentComponent(final Entity entity) {
        this(entity, new HashSet<>());
    }

    public Stream<TaskContent> stream() {
        return content.stream();
    }

    public boolean contains(TaskContent content) {
        return this.content.contains(content);
    }

    public void add(TaskContent content) {
        this.content.add(content);
    }

    public void remove(TaskContent content) {
        this.content.remove(content);
    }
}
