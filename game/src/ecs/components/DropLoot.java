package ecs.components;

import ecs.entities.Entity;
import ecs.items.ItemData;
import tools.Point;

/** a simple implementation of dropping all items of an Entity when it is dying. */
public class DropLoot implements IOnDeathFunction {
    private record DLData(Entity e, Components dlc, ItemData i) {}

    private record Components(InventoryComponent ic, PositionComponent pc) {}

    /**
     * drops all the Items the Entity currently holds
     *
     * @param entity Entity that has died
     */
    @Override
    public void onDeath(Entity entity) {
        Components dlc = prepareComponent(entity);
        dlc.ic.getItems().stream().map(x -> new DLData(entity, dlc, x)).forEach(this::dropItem);
    }

    /**
     * For Dropping Items there is a need for having an inventory and having a position.
     *
     * @param entity which should have the PositionComponent and the InventoryComponent
     * @return a simple record with both components
     */
    private Components prepareComponent(Entity entity) {
        InventoryComponent ic =
                entity.getComponent(InventoryComponent.class)
                        .map(InventoryComponent.class::cast)
                        .orElseThrow(DropLoot::missingInventoryComponentException);
        PositionComponent pc =
                entity.getComponent(PositionComponent.class)
                        .map(PositionComponent.class::cast)
                        .orElseThrow(DropLoot::missingPositionsComponentException);

        return new Components(ic, pc);
    }

    /**
     * Default Message when PositionComponent is missing
     *
     * @return MissingComponentException with a predefined Message
     */
    private static MissingComponentException missingPositionsComponentException() {
        return new MissingComponentException(
                "Missing "
                        + PositionComponent.class.getName()
                        + " which is required for "
                        + DropLoot.class.getName());
    }

    /**
     * Default Message when InventoryComponent is missing
     *
     * @return MissingComponentException with a predefined Message
     */
    private static MissingComponentException missingInventoryComponentException() {
        return new MissingComponentException(
                "Missing "
                        + InventoryComponent.class.getName()
                        + " which is required for "
                        + DropLoot.class.getName());
    }

    /**
     * handles the drop of an Item from a Inventory
     *
     * @param d the needed Data for dropping an Item
     */
    private void dropItem(DLData d) {
        d.i.triggerDrop(d.e, new Point(d.dlc.pc.getPosition()));
        d.dlc.ic.removeItem(d.i);
    }
}
