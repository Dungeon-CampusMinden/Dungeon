package contrib.utils.components.interaction;

import contrib.components.InteractionComponent;
import contrib.components.InventoryComponent;
import contrib.utils.components.item.ItemData;

import core.Entity;
import core.components.*;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.CoreAnimations;
import core.utils.position.Point;
import core.utils.position.Position;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * This class is a specific implementation of the {@link Consumer<Entity>} interface to use in the
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
 * core.utils.components.draw.CoreAnimations#IDLE_RIGHT} animation will be set as the current
 * animation.
 */
public class DropItemsInteraction implements Consumer<Entity> {

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
     * core.utils.components.draw.CoreAnimations#IDLE_RIGHT} animation will be set as the current
     * animation.
     *
     * @param entity associated entity
     */
    public void accept(final Entity entity) {
        InventoryComponent inventoryComponent =
                entity.fetch(InventoryComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                entity, InventoryComponent.class));
        PositionComponent positionComponent =
                entity.fetch(PositionComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                entity, PositionComponent.class));
        Set<ItemData> itemData = inventoryComponent.items();
        double count = itemData.size();
        // used for calculation of drop position
        AtomicInteger index = new AtomicInteger();
        itemData.forEach(
                item ->
                        item.triggerDrop(
                                entity,
                                calculateDropPosition(
                                        positionComponent, index.getAndIncrement() / count)));

        entity.fetch(DrawComponent.class)
                .ifPresent(x -> x.currentAnimation(CoreAnimations.IDLE_RIGHT));
    }

    /**
     * small Helper to determine the Position of the dropped item simple circle drop
     *
     * @param positionComponent The PositionComponent of the Chest
     * @param radian of the current Item
     * @return a Position in a unit Vector around the Chest
     */
    private static Position calculateDropPosition(
            PositionComponent positionComponent, double radian) {
        return new Point(
                (float) Math.cos(radian * Math.PI) + positionComponent.position().point().x,
                (float) Math.sin(radian * Math.PI) + positionComponent.position().point().y);
    }
}
