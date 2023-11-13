package dslinterop.dsltypeproperties;

import contrib.components.InteractionComponent;
import contrib.components.InventoryComponent;
import contrib.components.UIComponent;
import contrib.hud.GUICombination;
import contrib.hud.inventory.InventoryGUI;
import contrib.item.Item;
import contrib.utils.components.draw.ChestAnimations;
import contrib.utils.components.interaction.DropItemsInteraction;

import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;

import dsl.semanticanalysis.types.DSLExtensionMethod;
import dsl.semanticanalysis.types.DSLTypeProperty;
import dsl.semanticanalysis.types.IDSLExtensionMethod;
import dsl.semanticanalysis.types.IDSLTypeProperty;

import task.Task;
import task.TaskContent;
import task.components.TaskComponent;
import task.components.TaskContentComponent;
import task.tasktype.Element;
import task.utils.gamecontent.QuestItem;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * This class implements {@link IDSLTypeProperty} for the {@link Entity} class, in order to access
 * the Components of an entity from a DSL-program.
 */
public class EntityExtension {
    // private ctor, because this class should not be instantiated
    private EntityExtension() {}

    @DSLTypeProperty(name = "velocity_component", extendedType = Entity.class)
    public static class VelocityComponentProperty
            implements IDSLTypeProperty<Entity, VelocityComponent> {
        public static EntityExtension.VelocityComponentProperty instance =
                new EntityExtension.VelocityComponentProperty();

        private VelocityComponentProperty() {}

        @Override
        public void set(Entity instance, VelocityComponent valueToSet) {
            instance.removeComponent(VelocityComponent.class);
            instance.addComponent(valueToSet);
        }

        @Override
        public VelocityComponent get(Entity instance) {
            var optionalComponent = instance.fetch(VelocityComponent.class);
            return optionalComponent.orElse(null);
        }
    }

    @DSLTypeProperty(name = "inventory_component", extendedType = Entity.class)
    public static class InventoryComponentProperty
            implements IDSLTypeProperty<Entity, InventoryComponent> {
        public static EntityExtension.InventoryComponentProperty instance =
                new EntityExtension.InventoryComponentProperty();

        private InventoryComponentProperty() {}

        @Override
        public void set(Entity instance, InventoryComponent valueToSet) {
            instance.removeComponent(InventoryComponent.class);
            instance.addComponent(valueToSet);
        }

        @Override
        public InventoryComponent get(Entity instance) {
            var optionalComponent = instance.fetch(InventoryComponent.class);
            return optionalComponent.orElse(null);
        }
    }

    @DSLTypeProperty(name = "position_component", extendedType = Entity.class)
    public static class PositionComponentProperty
            implements IDSLTypeProperty<Entity, PositionComponent> {
        public static PositionComponentProperty instance = new PositionComponentProperty();

        private PositionComponentProperty() {}

        @Override
        public void set(Entity instance, PositionComponent valueToSet) {
            instance.removeComponent(PositionComponent.class);
            instance.addComponent(valueToSet);
        }

        @Override
        public PositionComponent get(Entity instance) {
            var optionalComponent = instance.fetch(PositionComponent.class);
            return optionalComponent.orElse(null);
        }
    }

    @DSLTypeProperty(name = "task_content_component", extendedType = Entity.class)
    public static class TaskContentComponentProperty
            implements IDSLTypeProperty<Entity, TaskContentComponent> {
        public static TaskContentComponentProperty instance = new TaskContentComponentProperty();

        private TaskContentComponentProperty() {}

        @Override
        public void set(Entity instance, TaskContentComponent valueToSet) {
            instance.removeComponent(TaskContentComponent.class);
            instance.addComponent(valueToSet);
        }

        @Override
        public TaskContentComponent get(Entity instance) {
            var optionalComponent = instance.fetch(TaskContentComponent.class);
            return optionalComponent.orElse(null);
        }
    }

    @DSLTypeProperty(name = "draw_component", extendedType = Entity.class)
    public static class DrawComponentProperty implements IDSLTypeProperty<Entity, DrawComponent> {
        public static DrawComponentProperty instance = new DrawComponentProperty();

        private DrawComponentProperty() {}

        @Override
        public void set(Entity instance, DrawComponent valueToSet) {
            instance.removeComponent(DrawComponent.class);
            instance.addComponent(valueToSet);
        }

        @Override
        public DrawComponent get(Entity instance) {
            var optionalComponent = instance.fetch(DrawComponent.class);
            return optionalComponent.orElse(null);
        }
    }

    @DSLTypeProperty(name = "interaction_component", extendedType = Entity.class)
    public static class InteractionComponentProperty
            implements IDSLTypeProperty<Entity, InteractionComponent> {
        public static InteractionComponentProperty instance = new InteractionComponentProperty();

        private InteractionComponentProperty() {}

        @Override
        public void set(Entity instance, InteractionComponent valueToSet) {
            instance.removeComponent(InteractionComponent.class);
            instance.addComponent(valueToSet);
        }

        @Override
        public InteractionComponent get(Entity instance) {
            var optionalComponent = instance.fetch(InteractionComponent.class);
            return optionalComponent.orElse(null);
        }
    }

    @DSLTypeProperty(name = "task_component", extendedType = Entity.class)
    public static class TaskComponentProperty implements IDSLTypeProperty<Entity, TaskComponent> {
        public static TaskComponentProperty instance = new TaskComponentProperty();

        private TaskComponentProperty() {}

        @Override
        public void set(Entity instance, TaskComponent valueToSet) {
            instance.removeComponent(TaskComponent.class);
            instance.addComponent(valueToSet);

            // if the task component references a Task, the manager entity should
            // be updated to the instance entity
            Task task = valueToSet.task();
            if (task != null) {
                task.managerEntity(instance);
            }
        }

        @Override
        public TaskComponent get(Entity instance) {
            var optionalComponent = instance.fetch(TaskComponent.class);
            return optionalComponent.orElse(null);
        }
    }

    @DSLExtensionMethod(name = "open", extendedType = InventoryComponent.class)
    public static class OpenInventoryMethod
            implements IDSLExtensionMethod<InventoryComponent, Void> {
        public static EntityExtension.OpenInventoryMethod instance =
                new EntityExtension.OpenInventoryMethod();

        @Override
        public Void call(InventoryComponent instance, List<Object> params) {
            // interacted = chest
            // interactor = andere entity (spieler)
            Entity chest = Game.find(instance).get();
            Entity other = (Entity) params.get(0);
            InventoryComponent otherIc = other.fetch(InventoryComponent.class).get();

            var optionalTcc = chest.fetch(TaskContentComponent.class);
            InventoryGUI inventory;
            if (optionalTcc.isEmpty()) {
                inventory = new InventoryGUI(instance);
            } else {
                TaskContent content = optionalTcc.get().content();
                Task task = content.task();
                inventory =
                        new InventoryGUI(content + " (Quest: '" + task.taskName() + "')", instance);
            }

            UIComponent uiComponent =
                    new UIComponent(new GUICombination(new InventoryGUI(otherIc), inventory), true);
            uiComponent.onClose(
                    () ->
                            chest.fetch(DrawComponent.class)
                                    .ifPresent(
                                            interactedDC -> {
                                                // remove all prior
                                                // opened animations
                                                interactedDC.deQueueByPriority(
                                                        ChestAnimations.OPEN_FULL.priority());
                                                if (instance.count() > 0) {
                                                    // aslong as
                                                    // there is an
                                                    // item inside
                                                    // the chest
                                                    // show a full
                                                    // chest
                                                    interactedDC.queueAnimation(
                                                            ChestAnimations.OPEN_FULL);
                                                } else {
                                                    // empty chest
                                                    // show the
                                                    // empty
                                                    // animation
                                                    interactedDC.queueAnimation(
                                                            ChestAnimations.OPEN_EMPTY);
                                                }
                                            }));
            other.addComponent(uiComponent);
            chest.fetch(DrawComponent.class)
                    .ifPresent(
                            interactedDC -> {
                                // only add opening animation when it is not
                                // finished
                                if (interactedDC
                                        .getAnimation(ChestAnimations.OPENING)
                                        .map(animation -> !animation.isFinished())
                                        .orElse(true)) {
                                    interactedDC.queueAnimation(ChestAnimations.OPENING);
                                }
                            });

            return null;
        }

        @Override
        public List<Type> getParameterTypes() {
            var arr = new Type[] {core.Entity.class};
            return Arrays.stream(arr).toList();
        }
    }

    @DSLExtensionMethod(name = "mark_as_task_container", extendedType = Entity.class)
    public static class AddNamedTaskContentMethod implements IDSLExtensionMethod<Entity, Void> {
        public static EntityExtension.AddNamedTaskContentMethod instance =
                new EntityExtension.AddNamedTaskContentMethod();

        @Override
        public Void call(Entity instance, List<Object> params) {
            var optionalTcc = instance.fetch(TaskContentComponent.class);
            TaskContentComponent tcc;

            if (optionalTcc.isEmpty()) {
                tcc = new TaskContentComponent();
            } else {
                tcc = optionalTcc.get();
            }

            Task task = (Task) params.get(0);
            String name = (String) params.get(1);
            Element<String> content = new Element<>(task, name);
            tcc.content(content);
            instance.addComponent(tcc);

            task.addContent(content);
            task.addContainer(content);

            return null;
        }

        @Override
        public List<Type> getParameterTypes() {
            var arr = new Type[] {Task.class, String.class};
            return Arrays.stream(arr).toList();
        }
    }

    @DSLExtensionMethod(name = "mark_as_task_container_with_element", extendedType = Entity.class)
    public static class AddTaskContentMethod implements IDSLExtensionMethod<Entity, Void> {
        public static EntityExtension.AddTaskContentMethod instance =
                new EntityExtension.AddTaskContentMethod();

        @Override
        public Void call(Entity instance, List<Object> params) {
            var optionalTcc = instance.fetch(TaskContentComponent.class);
            TaskContentComponent tcc;

            if (optionalTcc.isEmpty()) {
                tcc = new TaskContentComponent();
            } else {
                tcc = optionalTcc.get();
            }

            Task task = (Task) params.get(0);
            Element<String> content = (Element<String>) params.get(1);
            tcc.content(content);
            instance.addComponent(tcc);

            task.addContent(content);
            task.addContainer(content);

            return null;
        }

        @Override
        public List<Type> getParameterTypes() {
            var arr = new Type[] {Task.class, Element.class};
            return Arrays.stream(arr).toList();
        }
    }

    @DSLExtensionMethod(name = "add_item", extendedType = InventoryComponent.class)
    public static class AddItemToInventoryMethod
            implements IDSLExtensionMethod<InventoryComponent, Void> {
        public static EntityExtension.AddItemToInventoryMethod instance =
                new EntityExtension.AddItemToInventoryMethod();

        @Override
        public Void call(InventoryComponent instance, List<Object> params) {
            Item item = (Item) params.get(0);
            instance.add(item);
            return null;
        }

        @Override
        public List<Type> getParameterTypes() {
            var arr = new Type[] {QuestItem.class};
            return Arrays.stream(arr).toList();
        }
    }

    @DSLExtensionMethod(name = "drop_items", extendedType = InventoryComponent.class)
    public static class DropItemsMethod implements IDSLExtensionMethod<InventoryComponent, Void> {
        public static EntityExtension.DropItemsMethod instance =
                new EntityExtension.DropItemsMethod();

        @Override
        public Void call(InventoryComponent instance, List<Object> params) {
            Optional<Entity> optionalEntity = Game.find(instance);
            optionalEntity.ifPresent(entity -> new DropItemsInteraction().accept(entity, null));
            return null;
        }

        @Override
        public List<Type> getParameterTypes() {
            var arr = new Type[] {};
            return Arrays.stream(arr).toList();
        }
    }
}
