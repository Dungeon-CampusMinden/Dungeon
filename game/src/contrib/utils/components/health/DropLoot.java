package contrib.utils.components.health;

import contrib.components.InventoryComponent;
import contrib.item.Item;

import core.Entity;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.components.MissingComponentException;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

/** A simple implementation of dropping all items of an Entity when it is dying. */
public final class DropLoot implements Consumer<Entity> {

    /**
     * Drops all the items the entity currently holds.
     *
     * @param entity Entity that has died.
     */
    @Override
    public void accept(final Entity entity) {
        Components dlc = prepareComponent(entity);
        Arrays.stream(dlc.ic.items())
                .filter(Objects::nonNull)
                .map(x -> new DLData(entity, dlc, x))
                .forEach(this::dropItem);
    }

    /**
     * For Dropping Items there is a need for having an inventory and having a position.
     *
     * @param entity Entity which should have the PositionComponent and InventoryComponent.
     * @return A simple record with both components.
     */
    private Components prepareComponent(Entity entity) {
        InventoryComponent ic =
                entity.fetch(InventoryComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                entity, InventoryComponent.class));
        PositionComponent pc =
                entity.fetch(PositionComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                entity, PositionComponent.class));

        return new Components(ic, pc);
    }

    /**
     * Handles the drop of an item from an inventory.
     *
     * @param d The needed Data for dropping an Item.
     */
    private void dropItem(DLData d) {
        d.i.drop(d.e, new Point(d.dlc.pc.position()));
        d.dlc.ic.remove(d.i);
    }

    private record DLData(Entity e, Components dlc, Item i) {}

    private record Components(InventoryComponent ic, PositionComponent pc) {}
}
