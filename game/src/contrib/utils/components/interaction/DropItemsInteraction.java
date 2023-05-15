package contrib.utils.components.interaction;

import contrib.components.InteractionComponent;
import contrib.components.InventoryComponent;
import contrib.utils.components.item.ItemData;

import core.Entity;
import core.components.*;
import core.utils.Point;
import core.utils.components.MissingComponentException;

import java.util.List;
import java.util.stream.IntStream;

/**
 * This class is a specific implementation of the {@link IInteraction} interface to use in the
 * {@link InteractionComponent}.
 *
 * <p>The implementation will drop all the items inside the {@link InventoryComponent} of the
 * associated entity on the floor.
 *
 * <p>This function can be used to implement the loot function for chests.
 *
 * <p>Note: The entity that will use this function needs an {@link InventoryComponent} and {@link
 * PositionComponent}. An {@link DrawComponent} is optional.
 *
 * <p>If an {@link DrawComponent} is present, after the interaction, the {@link
 * DrawComponent#idleRight} animation will be set as the current animation.
 */
public class DropItemsInteraction implements IInteraction {

    /**
     * Will drop all the items inside the {@link InventoryComponent} of the associated entity on the
     * floor.
     *
     * <p>This function can be used to implement the loot function for chests.
     *
     * <p>Note: The entity that will use this function needs an {@link InventoryComponent} and
     * {@link PositionComponent}. An {@link DrawComponent} is optional.
     *
     * <p>If an {@link DrawComponent} is present, after the interaction, the {@link
     * DrawComponent#idleRight} animation will be set as the current animation.
     *
     * @param entity associated entity
     */
    public void onInteraction(Entity entity) {
        InventoryComponent inventoryComponent =
                entity.getComponent(InventoryComponent.class)
                        .map(InventoryComponent.class::cast)
                        .orElseThrow(
                                () ->
                                        createMissingComponentException(
                                                InventoryComponent.class.getName(), entity));
        PositionComponent positionComponent =
                entity.getComponent(PositionComponent.class)
                        .map(PositionComponent.class::cast)
                        .orElseThrow(
                                () ->
                                        createMissingComponentException(
                                                PositionComponent.class.getName(), entity));
        List<ItemData> itemData = inventoryComponent.getItems();
        double count = itemData.size();

        IntStream.range(0, itemData.size())
                .forEach(
                        index ->
                                itemData.get(index)
                                        .triggerDrop(
                                                entity,
                                                calculateDropPosition(
                                                        positionComponent, index / count)));
        entity.getComponent(DrawComponent.class)
                .map(DrawComponent.class::cast)
                .ifPresent(x -> x.setCurrentAnimation(x.getIdleRight()));
    }

    /**
     * small Helper to determine the Position of the dropped item simple circle drop
     *
     * @param positionComponent The PositionComponent of the Chest
     * @param radian of the current Item
     * @return a Point in a unit Vector around the Chest
     */
    private static Point calculateDropPosition(PositionComponent positionComponent, double radian) {
        return new Point(
                (float) Math.cos(radian * Math.PI) + positionComponent.getPosition().x,
                (float) Math.sin(radian * Math.PI) + positionComponent.getPosition().y);
    }

    /**
     * Helper to create a MissingComponentException with a bit more information
     *
     * @param Component the name of the Component which is missing
     * @param e the Entity which did miss the Component
     * @return the newly created Exception
     */
    private static MissingComponentException createMissingComponentException(
            String Component, Entity e) {
        return new MissingComponentException(
                Component
                        + " missing in "
                        + DropItemsInteraction.class.getName()
                        + " in Entity "
                        + e.getClass().getName());
    }
}
