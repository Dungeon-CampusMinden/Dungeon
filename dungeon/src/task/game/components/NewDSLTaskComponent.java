package task.game.components;

import core.Component;
import core.Entity;
import core.level.elements.tile.DoorTile;
import dsl.annotation.DSLCallback;
import dsl.annotation.DSLContextMember;
import dsl.annotation.DSLType;
import dsl.annotation.DSLTypeProperty;
import dsl.semanticanalysis.typesystem.extension.IDSLExtensionProperty;
import newdsl.tasks.Task;

import java.util.function.Consumer;

@DSLType
public final class NewDSLTaskComponent implements Component {

    private static final Consumer<Entity> EMPTY_ON_ACTIVATE = (taskmanager) -> {
    };

    public static final Consumer<Entity> DOOR_OPENER =
        entity ->
            entity
                .fetch(DoorComponent.class)
                .ifPresent(component -> component.doors().forEach(DoorTile::open));

    @DSLCallback
    private Consumer<Entity> onActivate;
    private Task task;
    private Entity my_entity;

    public NewDSLTaskComponent(@DSLContextMember(name = "entity") final Entity entity) {
        this.my_entity = entity;
        onActivate = DOOR_OPENER;
    }

    public NewDSLTaskComponent(Task task, final Entity entity) {
        this.task = task;
        entity.add(this);
        task.setManagementEntity(entity);
        onActivate = DOOR_OPENER;
    }

    public Task task() {
        return task;
    }

    public void onActivate(Consumer<Entity> callback) {
        this.onActivate = callback;
    }

    public void activate(Entity taskManager) {
        onActivate.accept(taskManager);
    }

    @DSLTypeProperty(name = "task", extendedType = NewDSLTaskComponent.class)
    public static class TaskProperty implements IDSLExtensionProperty<NewDSLTaskComponent, Task> {
        public static NewDSLTaskComponent.TaskProperty instance = new NewDSLTaskComponent.TaskProperty();

        private TaskProperty() {
        }

        @Override
        public void set(NewDSLTaskComponent instance, Task valueToSet) {
            instance.task = valueToSet;
            instance.task.setManagementEntity(instance.my_entity);
        }

        @Override
        public Task get(NewDSLTaskComponent instance) {
            return instance.task();
        }
    }
}
