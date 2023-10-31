package dsltypeproperties;

import semanticanalysis.types.DSLTypeProperty;
import semanticanalysis.types.IDSLTypeProperty;

import task.QuestItem;
import task.components.TaskContentComponent;

/**
 * Class, which stores dsl-extensions (e.g. {@link IDSLTypeProperty} or {@link
 * semanticanalysis.types.IDSLExtensionMethod}) for {@link QuestItem}s
 */
public class QuestItemExtension {
    // private ctor, because this class should not be instantiated
    private QuestItemExtension() {}

    /**
     * {@link IDSLTypeProperty} extension to access the {@link TaskContentComponent} of a {@link
     * QuestItem} instance. Not settable.
     */
    @DSLTypeProperty(
            name = "task_content_component",
            extendedType = QuestItem.class,
            isSettable = false)
    public static class TaskContentComponentProperty
            implements IDSLTypeProperty<QuestItem, TaskContentComponent> {
        public static TaskContentComponentProperty instance = new TaskContentComponentProperty();

        private TaskContentComponentProperty() {}

        @Override
        public void set(QuestItem instance, TaskContentComponent valueToSet) {}

        @Override
        public TaskContentComponent get(QuestItem instance) {
            return instance.taskContentComponent();
        }
    }
}
