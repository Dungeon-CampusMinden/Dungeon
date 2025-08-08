package contrib.entities;

import contrib.components.*;
import contrib.hud.crafting.CraftingGUI;
import contrib.hud.elements.GUICombination;
import contrib.hud.inventory.InventoryGUI;
import contrib.item.Item;
import contrib.utils.components.draw.ChestAnimations;
import contrib.utils.components.item.ItemGenerator;
import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Direction;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.Vector2;
import core.utils.components.draw.Animation;
import core.utils.components.draw.CoreAnimations;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** A utility class for building different miscellaneous entities in the game world. */
public final class MiscFactory {

  private static final Random RANDOM = new Random();
  private static final int DEFAULT_CHEST_SIZE = 12;
  private static final int MAX_AMOUNT_OF_ITEMS_ON_RANDOM = 5;
  private static final int MIN_AMOUNT_OF_ITEMS_ON_RANDOM = 1;
  private static final SimpleIPath CATAPULT = new SimpleIPath("other/red_dot.png");
  private static final SimpleIPath MARKER_TEXTURE = new SimpleIPath("other/blue_dot.png");

  /**
   * The {@link ItemGenerator} used to generate random items for chests.
   *
   * @see ItemGenerator
   * @see ItemGenerator#defaultItemGenerator()
   */
  private static ItemGenerator randomItemGenerator = ItemGenerator.defaultItemGenerator();

  /**
   * This method is used to create a new chest entity. The chest will be filled with random items.
   *
   * <p>The Entity is not added to the game yet.
   *
   * <p>The Entity will have a {@link InteractionComponent}, {@link PositionComponent}, {@link
   * core.components.DrawComponent}, {@link contrib.components.CollideComponent} and {@link
   * contrib.components.InventoryComponent}. It will use the {@link
   * contrib.utils.components.interaction.DropItemsInteraction} on interaction.
   *
   * @return A new Entity representing the chest.
   * @throws IOException if the animation could not be loaded.
   * @see MiscFactory#generateRandomItems(int, int) generateRandomItems
   */
  public static Entity newChest() throws IOException {
    return newChest(FILL_CHEST.RANDOM);
  }

  /**
   * This method is used to create a new chest entity. The chest entity can either be empty or
   * filled with random items.
   *
   * <p>The Entity is not added to the game yet.
   *
   * <p>The Entity will have a {@link InteractionComponent}, {@link PositionComponent}, {@link
   * core.components.DrawComponent}, {@link contrib.components.CollideComponent} and {@link
   * contrib.components.InventoryComponent}. It will use the {@link
   * contrib.utils.components.interaction.DropItemsInteraction} on interaction.
   *
   * @param type The type of chest to be created. It can either be RANDOM (filled with random items)
   *     or EMPTY.
   * @return A new Entity representing the chest.
   * @throws IOException if the animation could not be loaded.
   */
  public static Entity newChest(FILL_CHEST type) throws IOException {
    return switch (type) {
      case RANDOM ->
          newChest(
              generateRandomItems(MIN_AMOUNT_OF_ITEMS_ON_RANDOM, MAX_AMOUNT_OF_ITEMS_ON_RANDOM),
              PositionComponent.ILLEGAL_POSITION);
      case EMPTY -> newChest(Set.of(), PositionComponent.ILLEGAL_POSITION);
    };
  }

  private static Set<Item> generateRandomItems(int min, int max) {
    return IntStream.range(0, RANDOM.nextInt(min, max))
        .mapToObj(i -> randomItemGenerator.generateItemData())
        .collect(Collectors.toSet());
  }

  /**
   * Sets the ItemGenerator used to generate random items for monsters upon death.
   *
   * @param randomItemGenerator The ItemGenerator to use for generating random items.
   * @see ItemGenerator
   */
  public static void randomItemGenerator(ItemGenerator randomItemGenerator) {
    MiscFactory.randomItemGenerator = randomItemGenerator;
  }

  /**
   * Gets the ItemGenerator used to generate random items for randomly filled chests.
   *
   * <p>The default ItemGenerator is {@link ItemGenerator#defaultItemGenerator()}.
   *
   * @return The current ItemGenerator used for generating random items.
   * @see ItemGenerator
   */
  public static ItemGenerator randomItemGenerator() {
    return randomItemGenerator;
  }

  /**
   * Get an Entity that can be used as a chest.
   *
   * <p>It will contain the given items.
   *
   * <p>The Entity is not added to the game yet.
   *
   * <p>It will have a {@link InteractionComponent}. {@link PositionComponent}, {@link
   * core.components.DrawComponent}, {@link contrib.components.CollideComponent} and {@link
   * contrib.components.InventoryComponent}. It will use the {@link
   * contrib.utils.components.interaction.DropItemsInteraction} on interaction.
   *
   * @param item Items that should be in the chest.
   * @param position Where should the chest be placed?
   * @return A new Entity.
   * @throws IOException If the animation could not be loaded.
   */
  public static Entity newChest(final Set<Item> item, final Point position) throws IOException {
    final float defaultInteractionRadius = 1f;
    Entity chest = new Entity("chest");

    if (position == null) chest.add(new PositionComponent());
    else chest.add(new PositionComponent(position));
    InventoryComponent ic = new InventoryComponent(DEFAULT_CHEST_SIZE);
    chest.add(ic);
    item.forEach(ic::add);
    chest.add(
        new InteractionComponent(
            defaultInteractionRadius,
            true,
            (interacted, interactor) -> {
              interactor
                  .fetch(InventoryComponent.class)
                  .ifPresent(
                      whoIc -> {
                        UIComponent uiComponent =
                            new UIComponent(
                                new GUICombination(
                                    new InventoryGUI(whoIc), new InventoryGUI("Chest", ic, 6)),
                                true);
                        uiComponent.onClose(
                            () ->
                                interacted
                                    .fetch(DrawComponent.class)
                                    .ifPresent(
                                        interactedDC -> {
                                          // remove all
                                          // prior
                                          // opened
                                          // animations
                                          interactedDC.deQueueByPriority(
                                              ChestAnimations.OPEN_FULL.priority());
                                          if (ic.count() > 0) {
                                            // as long
                                            // as
                                            // there is
                                            // an
                                            // item
                                            // inside
                                            // the chest
                                            // show a
                                            // full
                                            // chest
                                            interactedDC.queueAnimation(ChestAnimations.OPEN_FULL);
                                          } else {
                                            // empty
                                            // chest
                                            // show the
                                            // empty
                                            // animation
                                            interactedDC.queueAnimation(ChestAnimations.OPEN_EMPTY);
                                          }
                                        }));
                        interactor.add(uiComponent);
                      });
              interacted
                  .fetch(DrawComponent.class)
                  .ifPresent(
                      interactedDC -> {
                        // only add opening animation when it is not
                        // finished
                        if (interactedDC
                            .animation(ChestAnimations.OPENING)
                            .map(animation -> !animation.isFinished())
                            .orElse(true)) {
                          interactedDC.queueAnimation(ChestAnimations.OPENING);
                        }
                      });
            }));
    DrawComponent dc = new DrawComponent(new SimpleIPath("objects/treasurechest"));
    var mapping = dc.animationMap();
    // set the closed chest as default idle
    mapping.put(CoreAnimations.IDLE.pathString(), mapping.get(ChestAnimations.CLOSED.pathString()));
    // opening animation should not loop
    mapping.get(ChestAnimations.OPENING.pathString()).loop(false);
    dc.animationMap(mapping);
    // reset Idle Animation
    dc.deQueueByPriority(CoreAnimations.IDLE.priority());
    dc.currentAnimation(CoreAnimations.IDLE);
    chest.add(dc);

    return chest;
  }

  /**
   * Get an Entity that can be used as a crafting cauldron.
   *
   * <p>The Entity is not added to the game yet.
   *
   * @return A new Entity.
   * @throws IOException if the animation could not be loaded.
   */
  public static Entity newCraftingCauldron() throws IOException {
    Entity cauldron = new Entity("cauldron");
    cauldron.add(new PositionComponent());
    cauldron.add(new DrawComponent(new SimpleIPath("objects/cauldron")));
    cauldron.add(new CollideComponent());
    cauldron.add(
        new InteractionComponent(
            1f,
            true,
            (entity, who) ->
                who.fetch(InventoryComponent.class)
                    .ifPresent(
                        ic -> {
                          CraftingGUI craftingGUI = new CraftingGUI(ic);
                          UIComponent component =
                              new UIComponent(
                                  new GUICombination(new InventoryGUI(ic), craftingGUI), true);
                          component.onClose(craftingGUI::cancel);
                          who.add(component);
                        })));
    return cauldron;
  }

  /**
   * Create a Entity that can be used as a marker on the floor (x marks the spot).
   *
   * @param position Positon where to spawn the marker.
   * @return The Marker-Entity.
   */
  public static Entity marker(Point position) {
    Entity marker = new Entity("marker");
    marker.add(new PositionComponent(position));
    marker.add(new DrawComponent(Animation.fromSingleImage(MARKER_TEXTURE)));
    return marker;
  }

  /**
   * Creates a catapult entity at the specified spawn point that can launch other entities to a
   * given target location.
   *
   * <p>When another entity collides with this catapult:
   *
   * <ul>
   *   <li>The colliding entity must have a {@link CatapultableComponent} to be eligible for launch.
   *   <li>The entity’s current velocity is reset to zero.
   *   <li>The {@link CatapultableComponent}'s deactivate callback is invoked to disable any
   *       controls or AI.
   *   <li>The entity is temporarily turned into a projectile that travels from the spawn point to
   *       the target location.
   *   <li>Once the projectile reaches the target, the entity is restored to its original state.
   * </ul>
   *
   * @param spawnPoint the position where the catapult entity is created
   * @param location the target location to which entities will be launched
   * @param speed the speed at which the entity travels toward the target
   * @return the catapult entity that initiates the launch on collision
   */
  public static Entity catapult(Point spawnPoint, Point location, float speed) {
    Entity catapult = new Entity();
    catapult.add(new PositionComponent(spawnPoint));
    catapult.add(new DrawComponent(Animation.fromSingleImage(CATAPULT)));
    TriConsumer<Entity, Entity, Direction> action =
        (you, other, direction) -> {
          if (!other.isPresent(CatapultableComponent.class)) return;
          other
              .fetch(VelocityComponent.class)
              .ifPresent(
                  vc -> {
                    vc.currentVelocity(Vector2.ZERO);
                    vc.clearForces();
                  });
          other.fetch(CatapultableComponent.class).ifPresent(cc -> cc.deactivate().accept(other));
          catapultFlyEntity(other, spawnPoint, location, speed);
        };
    catapult.add(new CollideComponent(action, CollideComponent.DEFAULT_COLLIDER));
    return catapult;
  }

  /**
   * Makes the entity a temporary projectile and flies from the start to the goal location.
   *
   * <p>This entity handles the visual and logical aspects of the catapult animation. While the
   * original entity is made invisible and immobilized, this projectile simulates the flight:
   *
   * <ul>
   *   <li>Attaches a {@link VelocityComponent} with a termination callback that resets the entity
   *       when the flight ends.
   *   <li>Adds a {@link CollideComponent} that resets the entity once the projectile reaches it
   *       endpoint.
   * </ul>
   *
   * @param other the entity that is being catapulted
   * @param start the starting position of the catapult flight (usually the catapult location)
   * @param goal the target location to which the entity is being catapulted
   * @param speed the flight speed of the projectile
   */
  private static void catapultFlyEntity(Entity other, Point start, Point goal, float speed) {
    Vector2 forceToApply = SkillTools.calculateDirection(start, goal).scale(speed);
    VelocityComponent entityVc = other.fetch(VelocityComponent.class).orElse(null);
    other.remove(VelocityComponent.class);
    VelocityComponent vc =
        new VelocityComponent(
            speed,
            entity -> {
              resetCatapultedEntity(entity, entityVc);
            },
            true);
    other.add(vc);

    other.add(
        new ProjectileComponent(
            start, goal, forceToApply, entity -> resetCatapultedEntity(entity, entityVc)));
    other.add(new PositionComponent(start));
    other.add(new FlyComponent());
  }

  /**
   * Reverts all temporary changes made to an entity after the catapult process is complete.
   *
   * <p>This method should be called when the projectile-entity has reached its destination. It:
   *
   * <ul>
   *   <li>Calls the reactivation callback of the {@link CatapultableComponent} to restore entity
   *       behavior.
   *   <li>Restores the original {@link VelocityComponent}.
   *   <li>Removes the {@link ProjectileComponent} and {@link FlyComponent}.
   * </ul>
   *
   * @param other the original entity that was catapulted
   * @param entityVc VelocityComponent restore to the entity
   */
  private static void resetCatapultedEntity(Entity other, VelocityComponent entityVc) {
    other.fetch(CatapultableComponent.class).ifPresent(cc -> cc.reactivate().accept(other));
    other.remove(ProjectileComponent.class);
    other.remove(FlyComponent.class);
    if (entityVc != null) {
      other.add(entityVc);
    }
  }

  /**
   * Enum representing the types of chest fillings. The chest can either be filled with random items
   * (RANDOM) or be empty (EMPTY).
   *
   * @see MiscFactory#newChest(FILL_CHEST)
   */
  public enum FILL_CHEST {
    /** Represents a chest filled with random items. */
    RANDOM,
    /** Represents an empty chest. */
    EMPTY,
  }
}
