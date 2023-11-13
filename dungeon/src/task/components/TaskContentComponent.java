package task.components;

import core.Component;

import dsl.semanticanalysis.types.DSLType;
import dsl.semanticanalysis.types.DSLTypeProperty;
import dsl.semanticanalysis.types.IDSLTypeProperty;

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
@DSLType
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

    /** Create a new TaskContentComponent. */
    public TaskContentComponent() {}

    /**
     * Return the internal represented {@link TaskContent}.
     *
     * @return internal {@link TaskContent}
     */
    public TaskContent content() {
        return content;
    }

    /** Set the internal represented {@link TaskContent}. */
    public void content(TaskContent content) {
        this.content = content;
    }

    @DSLTypeProperty(name = "content", extendedType = TaskContentComponent.class)
    public static class ContentProperty
            implements IDSLTypeProperty<TaskContentComponent, TaskContent> {
        public static TaskContentComponent.ContentProperty instance =
                new TaskContentComponent.ContentProperty();

        private ContentProperty() {}

        @Override
        public void set(TaskContentComponent instance, TaskContent valueToSet) {
            instance.content(valueToSet);
        }

        @Override
        public TaskContent get(TaskContentComponent instance) {
            return instance.content();
        }
    }
}
