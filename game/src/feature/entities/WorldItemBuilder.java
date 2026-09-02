package feature.entities;

import engine.Entity;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.utils.Point;
import engine.utils.components.draw.animation.Animation;
import feature.components.ItemComponent;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import feature.inventory.Item;

/** Class which creates all needed Components for a basic WorldItem. */
public final class WorldItemBuilder {
  private static final float DEFAULT_ITEM_PICKUP_RADIUS = 2.0f;

  /**
   * Creates an Entity which can then be added to the game.
   *
   * <p>The item will be picked up directly on interaction.
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

    droppedItem.add(
        new InteractionComponent(new Interaction(item::collect, DEFAULT_ITEM_PICKUP_RADIUS)));
    return droppedItem;
  }

  /**
   * Creates an Entity which can then be added to the game.
   *
   * <p>The item will be picked up directly on interaction.
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
