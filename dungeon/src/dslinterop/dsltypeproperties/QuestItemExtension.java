package dslinterop.dsltypeproperties;

import dsl.annotation.DSLTypeProperty;
import dsl.semanticanalysis.typesystem.extension.IDSLExtensionMethod;
import dsl.semanticanalysis.typesystem.extension.IDSLExtensionProperty;
import task.game.components.TaskContentComponent;
import task.game.content.QuestItem;

/**
 * Class, which stores dsl-extensions (e.g. {@link IDSLExtensionProperty} or {@link
 * IDSLExtensionMethod}) for {@link QuestItem}s
 */
public class QuestItemExtension {
  // private ctor, because this class should not be instantiated
  private QuestItemExtension() {}

  /**
   * {@link IDSLExtensionProperty} extension to access the {@link TaskContentComponent} of a {@link
   * QuestItem} instance. Not settable.
   */
  @DSLTypeProperty(
      name = "task_content_component",
      extendedType = QuestItem.class,
      isSettable = false)
  public static class TaskContentComponentProperty
      implements IDSLExtensionProperty<QuestItem, TaskContentComponent> {
    /** WTF? . */
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
