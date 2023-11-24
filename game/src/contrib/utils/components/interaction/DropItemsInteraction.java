package contrib.utils.components.interaction;

import com.badlogic.gdx.utils.Null;

import contrib.components.InteractionComponent;
import contrib.components.InventoryComponent;
import contrib.item.Item;
import contrib.utils.level.NoTileFoundException;

import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.utils.Coordinate;
import core.level.utils.LevelUtils;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.CoreAnimations;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
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
 * CoreAnimations#IDLE_RIGHT} animation will be set as the current animation.
 */
public class DropItemsInteraction implements BiConsumer<Entity, Entity> {

    /**
     * small Helper to determine the Position of the dropped item simple circle drop
     *
     * @param positionComponent The PositionComponent of the Chest
     * @param radian of the current Item
     * @return a Point in a unit Vector around the Chest
     */
    private static Point calculateDropPosition(PositionComponent positionComponent, double radian) {
        return new Point(
                (float) Math.cos(radian * Math.PI) + positionComponent.position().x,
                (float) Math.sin(radian * Math.PI) + positionComponent.position().y);
    }

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
     * CoreAnimations#IDLE_RIGHT} animation will be set as the current animation.
     *
     * @param entity associated entity
     * @param who The entity that triggered the interaction (could be null)
     */
    public void accept(final Entity entity, final @Null Entity who) {
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
        Item[] itemData = inventoryComponent.items();
        double count = itemData.length;
        // used for calculation of drop position
        AtomicInteger index = new AtomicInteger();
        Arrays.stream(itemData)
                .forEach(
                        item -> {
                            if (item != null) {
                                if (!item.drop(
                                        entity,
                                        calculateDropPosition(
                                                positionComponent,
                                                index.getAndIncrement() / count))) {
                                    Coordinate randomTile =
                                            LevelUtils.randomAccessibleTileCoordinateInRange(
                                                            positionComponent.position(), 1)
                                                    .orElseThrow(
                                                            () ->
                                                                    new NoTileFoundException(
                                                                            "No Tile was found for "
                                                                                    + entity));

                                    item.drop(entity, randomTile.toPoint());
                                }
                            }
                        });

        entity.fetch(DrawComponent.class)
                .ifPresent(
                        x ->
                                x.getAnimation(CoreAnimations.IDLE_RIGHT)
                                        .ifPresent(
                                                y ->
                                                        x.queueAnimation(
                                                                y.duration(),
                                                                CoreAnimations.IDLE_RIGHT)));
    }
}
