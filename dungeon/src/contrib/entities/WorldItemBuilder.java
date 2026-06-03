package contrib.entities;

import contrib.components.ItemComponent;
import contrib.hud.DialogUtils;
import contrib.item.Item;
import contrib.modules.interaction.IInteractable;
import contrib.modules.interaction.Interaction;
import contrib.modules.interaction.InteractionComponent;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.components.draw.animation.Animation;

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
    DrawComponent drawComponent = new DrawComponent(item.worldAnimation());
    droppedItem.add(drawComponent);
    droppedItem.add(new ItemComponent(item));
    applyMaxOneTileScale(droppedItem, drawComponent);

    droppedItem.add(
        new InteractionComponent(() -> new Interaction(item::collect, DEFAULT_ITEM_PICKUP_RADIUS)));
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
    DrawComponent drawComponent = new DrawComponent(item.worldAnimation());
    droppedItem.add(drawComponent);
    droppedItem.add(new ItemComponent(item));
    applyMaxOneTileScale(droppedItem, drawComponent);

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

  /**
   * Scales the given entity's {@link PositionComponent} so that the largest world dimension of the
   * provided {@link DrawComponent} fits exactly into a 1x1 tile while keeping the texture's aspect
   * ratio intact.
   *
   * <p>The default behavior of {@link Animation} sizes the smallest sprite dimension to one tile,
   * which causes thin/tall textures (e.g. paper) to appear larger than one tile. This method
   * counteracts that by applying a uniform scale based on the largest dimension.
   *
   * @param entity the entity that owns the {@link PositionComponent}
   * @param drawComponent the {@link DrawComponent} whose world size is used for the calculation
   */
  private static void applyMaxOneTileScale(final Entity entity, final DrawComponent drawComponent) {
    float maxDim = Math.max(drawComponent.getWidth(), drawComponent.getHeight());
    if (maxDim <= 0) return;
    entity.fetch(PositionComponent.class).ifPresent(pc -> pc.scale(1f / maxDim));
  }
}
