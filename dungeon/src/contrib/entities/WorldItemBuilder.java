package contrib.entities;

import contrib.components.ItemComponent;
import contrib.hud.DialogUtils;
import contrib.item.Item;
import contrib.modules.interaction.IInteractable;
import contrib.modules.interaction.ISimpleIInteractable;
import contrib.modules.interaction.Interaction;
import contrib.modules.interaction.InteractionComponent;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;

/** Class which creates all needed Components for a basic WorldItem. */
public final class WorldItemBuilder {
  private static final float DEFAULT_ITEM_PICKUP_RADIUS = 2.0f;

  /**
   * Creates an Entity which then can be added to the game.
   *
   * @param item the Item that is stored in the entity
   * @return the newly created Entity
   */
  public static Entity buildWorldItemSimpleInteraction(final Item item) {
    Entity droppedItem = new Entity("worldItem_" + item.displayName());
    droppedItem.add(new PositionComponent(PositionComponent.ILLEGAL_POSITION));
    droppedItem.add(new DrawComponent(item.worldAnimation()));
    droppedItem.add(new ItemComponent(item));

    droppedItem.add(
        new InteractionComponent(
            (ISimpleIInteractable)
                () -> new Interaction(item::collect, DEFAULT_ITEM_PICKUP_RADIUS)));
    return droppedItem;
  }

  /**
   * Creates an Entity which then can be added to the game.
   *
   * @param item the Data which should be given to the world Item
   * @param position the position where the item should be placed
   * @return the newly created Entity
   */
  public static Entity buildWorldItemSimpleInteraction(final Item item, final Point position) {
    Entity droppedItem = buildWorldItemSimpleInteraction(item);
    droppedItem.fetch(PositionComponent.class).ifPresent(pc -> pc.position(position));
    return droppedItem;
  }

  /**
   * Creates an Entity which can then be added to the game.
   *
   * <p>This entity will have a more detailed Interaction Component than the entity created with
   * {@link #buildWorldItemSimpleInteraction(Item)}.
   *
   * <p>On interaction, the entity can be picked up or looked at. When looked at, the description of
   * the item will be shown in a dialog pop-up.
   *
   * <p>Other interactions use the default implementation and will show some funny texts.
   *
   * @param item the Item stored in the entity
   * @return the newly created Entity
   */
  public static Entity buildWorldItem(final Item item) {
    Entity droppedItem = new Entity("worldItem_" + item.displayName());
    droppedItem.add(new PositionComponent(PositionComponent.ILLEGAL_POSITION));
    droppedItem.add(new DrawComponent(item.worldAnimation()));
    droppedItem.add(new ItemComponent(item));

    droppedItem.add(new InteractionComponent(detailedItemInteraction(item)));
    return droppedItem;
  }

  /**
   * Creates an Entity which can then be added to the game.
   *
   * <p>This entity will have a more detailed Interaction Component than the entity created with
   * {@link #buildWorldItemSimpleInteraction(Item)}.
   *
   * <p>On interaction, the entity can be picked up or looked at. When looked at, the description of
   * the item will be shown in a dialog pop-up.
   *
   * <p>Other interactions use the default implementation and will show some funny texts.
   *
   * @param item the Item stored in the entity
   * @param position the position where the item should be placed
   * @return the newly created Entity
   */
  public static Entity buildWorldItem(final Item item, final Point position) {
    Entity droppedItem = buildWorldItem(item);
    droppedItem.fetch(PositionComponent.class).ifPresent(pc -> pc.position(position));
    return droppedItem;
  }

  private static IInteractable detailedItemInteraction(final Item item) {
    return new IInteractable() {
      @Override
      public Interaction look() {
        return new Interaction(
            (entity, who) -> DialogUtils.showTextPopup(item.description(), item.displayName()),
            LOOK_LABEL);
      }

      @Override
      public Interaction take() {
        return new Interaction(item::collect, DEFAULT_ITEM_PICKUP_RADIUS, TAKE_LABEL);
      }
    };
  }
}
