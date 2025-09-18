package contrib.entities;

import contrib.components.*;
import contrib.hud.DialogUtils;
import contrib.hud.crafting.CraftingGUI;
import contrib.hud.dialogs.OkDialog;
import contrib.hud.dialogs.YesNoDialog;
import contrib.hud.elements.GUICombination;
import contrib.hud.inventory.InventoryGUI;
import contrib.item.Item;
import contrib.item.concreteItem.*;
import contrib.item.concreteItem.ItemBigKey;
import contrib.item.concreteItem.ItemKey;
import contrib.utils.components.item.ItemGenerator;
import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.elements.tile.DoorTile;
import core.utils.*;
import core.utils.Direction;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.Vector2;
import core.utils.components.draw.DepthLayer;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.animation.AnimationConfig;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.*;
import java.util.Arrays;
import java.util.Map;
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
  private static final SimpleIPath HEART_TEXTURE =
      new SimpleIPath("items/pickups/heart_pickup.png");
  private static final SimpleIPath FAIRY_TEXTURE =
      new SimpleIPath("items/pickups/fairy_pickup.png");
  private static final SimpleIPath DOOR_BLOCKER_TEXTURE = new SimpleIPath("other/chain_lock.png");

  private static final SimpleIPath CRATE_TEXTURE = new SimpleIPath("objects/crate/basic.png");
  private static final SimpleIPath BOOK_TEXTURE = new SimpleIPath("items/book/red_book.png");
  private static final SimpleIPath SPELL_BOOK_TEXTURE =
      new SimpleIPath("items/book/spell_book.png");
  private static final SimpleIPath STONE_TEXTURES = new SimpleIPath("objects/stone");
  private static final SimpleIPath VASE_TEXTURES = new SimpleIPath("objects/vase");

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
   * @see MiscFactory#generateRandomItems(int, int) generateRandomItems
   */
  public static Entity newChest() {
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
   */
  public static Entity newChest(FILL_CHEST type) {
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
   */
  public static Entity newChest(final Set<Item> item, final Point position) {
    final float defaultInteractionRadius = 1f;
    Entity chest = new Entity("chest");

    if (position == null) chest.add(new PositionComponent());
    else chest.add(new PositionComponent(position));
    InventoryComponent ic = new InventoryComponent(DEFAULT_CHEST_SIZE);
    chest.add(ic);
    item.forEach(ic::add);

    chest.add(new CollideComponent(Vector2.ZERO, Vector2.ONE));

    Map<String, Animation> animationMap =
        Animation.loadAnimationSpritesheet(new SimpleIPath("objects/treasurechest"));
    State stClosed = State.fromMap(animationMap, "closed");
    State stOpening = State.fromMap(animationMap, "opening");
    State stOpen = FillState.fromMap(animationMap, "open");
    StateMachine sm = new StateMachine(Arrays.asList(stClosed, stOpening, stOpen), stClosed);
    sm.addTransition(stClosed, "open", stOpening);
    // Automatically transition to open state when opening animation is finished playing
    sm.addEpsilonTransition(stOpening, State::isAnimationFinished, stOpen, () -> ic.count() == 0);
    DrawComponent dc = new DrawComponent(sm);
    chest.add(dc);

    chest.add(
        new InteractionComponent(
            defaultInteractionRadius,
            true,
            (interacted, interactor) ->
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
                                            // only add opening animation when it is not finished.
                                            // If
                                            // we close the GUI before the opening
                                            // animation finishes, the epsilon transition will
                                            // handle
                                            // setting the data correctly
                                            if (!interactedDC
                                                .stateMachine()
                                                .getCurrentStateName()
                                                .equals("opening")) {
                                              interactedDC.sendSignal("open", ic.count() == 0);
                                            }
                                          }));
                          interactor.add(uiComponent);
                        })));

    return chest;
  }

  /**
   * Creates a new locked chest entity that requires a key item to be opened.
   *
   * <p>Uses {@link MiscFactory#newChest(Set, Point)} and adds a locking mechanism to it.
   *
   * @param items Items that should be stored in the chest.
   * @param position The position where the chest should be placed.
   * @param requiredKeyType The type of key item required to open the chest.
   * @return A new locked chest entity.
   */
  public static Entity newLockedChest(
      final Set<Item> items, final Point position, final Class<? extends Item> requiredKeyType) {
    if (!ItemKey.class.equals(requiredKeyType) && !ItemBigKey.class.equals(requiredKeyType)) {
      throw new IllegalArgumentException(
          "LockedChest entity could not be created: Only ItemKey.class or ItemBigKey.class are allowed as requiredKeyType");
    }

    String reqKeyName =
        requiredKeyType.equals(ItemKey.class) ? "silbernen Schlüssel" : "großen goldenen Schlüssel";
    final float defaultInteractionRadius = 1f;
    Entity lockedChest = newChest(items, position);

    lockedChest
        .fetch(InteractionComponent.class)
        .ifPresent(
            oldIC -> {
              InteractionComponent wrapperIC =
                  new InteractionComponent(
                      defaultInteractionRadius,
                      true,
                      (interacted, interactor) -> {
                        InventoryComponent invComp =
                            interactor.fetch(InventoryComponent.class).orElse(null);
                        if (invComp == null) {
                          return;
                        }

                        if (!invComp.hasItem(requiredKeyType)) {
                          DialogUtils.showTextPopup(
                              "Du brauchst einen "
                                  + reqKeyName
                                  + " um diese Schatzkiste zu öffnen!",
                              "Fehlender Schlüssel.");
                          return;
                        }

                        YesNoDialog.showYesNoDialog(
                            "Willst du deinen "
                                + reqKeyName
                                + " verwenden, um die Schatzkiste zu öffnen?",
                            "Verschlossene Schatzkiste.",
                            () -> {
                              invComp.itemOfClass(requiredKeyType).ifPresent(invComp::remove);
                              oldIC.triggerInteraction(interacted, interactor);
                              interacted.remove(InteractionComponent.class);
                              interacted.add(oldIC);
                            },
                            () -> {
                              // "No" - do nothing
                            });
                      });

              lockedChest.remove(InteractionComponent.class);
              lockedChest.add(wrapperIC);
            });

    return lockedChest;
  }

  /**
   * Get an Entity that can be used as a crafting cauldron.
   *
   * <p>The Entity is not added to the game yet.
   *
   * @param position position of the crafting cauldron.
   * @return A new Entity.
   */
  public static Entity newCraftingCauldron(Point position) {
    Entity cauldron = new Entity("cauldron");
    cauldron.add(new PositionComponent(position));
    DrawComponent dc = new DrawComponent(new SimpleIPath("objects/cauldron"));
    dc.depth(DepthLayer.Player.depth());
    cauldron.add(dc);
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
    cauldron.add(new CollideComponent(Vector2.ZERO, Vector2.ONE));
    return cauldron;
  }

  /**
   * Get an Entity that can be used as a crafting cauldron.
   *
   * <p>The Entity is not added to the game yet.
   *
   * <p>The Entity is placed at the {@link PositionComponent#ILLEGAL_POSITION}. >.
   *
   * @return A new Entity.
   */
  public static Entity newCraftingCauldron() {
    return newCraftingCauldron(PositionComponent.ILLEGAL_POSITION);
  }

  /**
   * Creates an Entity that can be used as a marker on the floor (x marks the spot).
   *
   * @param position Position where to spawn the marker.
   * @return The Marker-Entity.
   */
  public static Entity marker(Point position) {
    Entity marker = new Entity("marker");
    marker.add(new PositionComponent(position));
    marker.add(new DrawComponent(new Animation(MARKER_TEXTURE)));
    return marker;
  }

  /**
   * Creates a crate entity at the given position with the specified mass and texture.
   *
   * <p>The crate can be pushed around by other entities. It has a default movement speed of {@code
   * 10} . The crate includes:
   *
   * <ul>
   *   <li>{@link PositionComponent} – sets the initial position
   *   <li>{@link VelocityComponent} – configured with speed {@code 10} and the given mass
   *   <li>{@link DrawComponent} – renders the crate using the given texture
   *   <li>{@link CollideComponent} – enables movement and collisions
   * </ul>
   *
   * @param position The starting position of the crate.
   * @param mass The mass of the crate.
   * @param texture The texture to render for the crate.
   * @return The created crate entity.
   */
  public static Entity crate(Point position, float mass, SimpleIPath texture) {
    Entity crate = new Entity("crate");
    crate.add(new PositionComponent(position));
    crate.add(new VelocityComponent(10, mass, entity -> {}, false));
    crate.add(new DrawComponent(new Animation(texture)));
    crate.add(new CollideComponent(Vector2.ZERO, Vector2.ONE));
    return crate;
  }

  /**
   * Creates a crate entity at the given position with the specified mass, using the default crate
   * texture.
   *
   * <p>The crate can be pushed around and has a default movement speed of {@code 10} units.
   *
   * @param position The starting position of the crate.
   * @param mass The mass of the crate.
   * @return The created crate entity.
   */
  public static Entity crate(Point position, float mass) {
    return crate(position, mass, CRATE_TEXTURE);
  }

  /**
   * Creates a crate entity at the given position with the default mass and the default crate
   * texture.
   *
   * <p>The crate can be pushed around and has a default movement speed of {@code 10} units.
   *
   * @param position The starting position of the crate.
   * @return The created crate entity.
   */
  public static Entity crate(Point position) {
    return crate(position, VelocityComponent.DEFAULT_MASS, CRATE_TEXTURE);
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
    Entity catapult = new Entity("catapult");
    catapult.add(new PositionComponent(spawnPoint));
    catapult.add(new DrawComponent(new Animation(CATAPULT)));
    TriConsumer<Entity, Entity, Direction> action =
        (you, other, direction) -> {
          if (!other.isPresent(CatapultableComponent.class)) return;
          if (other
              .fetch(CatapultableComponent.class)
              .map(CatapultableComponent::isFlying)
              .orElse(false)) return;
          other
              .fetch(VelocityComponent.class)
              .ifPresent(
                  vc -> {
                    vc.currentVelocity(Vector2.ZERO);
                    vc.clearForces();
                  });
          other
              .fetch(CatapultableComponent.class)
              .ifPresent(
                  cc -> {
                    cc.deactivate().accept(other);
                    cc.flies();
                  });
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
        new VelocityComponent(speed, entity -> resetCatapultedEntity(entity, entityVc), true);
    other.add(vc);

    other.add(
        new ProjectileComponent(
            start, goal, forceToApply, entity -> resetCatapultedEntity(entity, entityVc)));
    other
        .fetch(PositionComponent.class)
        .ifPresentOrElse(
            pc -> pc.position(start),
            () -> {
              other.add(new PositionComponent(start));
            });
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
    other
        .fetch(CatapultableComponent.class)
        .ifPresent(
            cc -> {
              cc.reactivate().accept(other);
              cc.lands();
            });
    other.remove(ProjectileComponent.class);
    other.remove(FlyComponent.class);
    if (entityVc != null) {
      other.add(entityVc);
    }
  }

  /**
   * Create a book, that the player can read.
   *
   * @param position Position of the book.
   * @param text Text that is shown in a Dialog on interaction.
   * @param title Title of the dialog.
   * @param onClose Callback for closing the dialog.
   * @return The book Entity.
   */
  public static Entity book(Point position, String text, String title, IVoidFunction onClose) {
    Entity book = new Entity("book");
    book.add(new PositionComponent(position));
    book.add(
        new InteractionComponent(
            1, true, (entity, entity2) -> OkDialog.showOkDialog(text, title, onClose)));
    book.add(
        new DrawComponent(new Animation(Math.random() < 0.5 ? BOOK_TEXTURE : SPELL_BOOK_TEXTURE)));
    return book;
  }

  /**
   * Creates a new Entity that blocks a door aka. locks a door tile.
   *
   * @param door the door Tile to be locked
   * @param requiredKeyType the key type which is needed to unlock the door
   * @return a new DoorBlocker Entity
   */
  public static Entity createDoorBlocker(
      DoorTile door, final Class<? extends Item> requiredKeyType) {
    if (!ItemKey.class.equals(requiredKeyType) && !ItemBigKey.class.equals(requiredKeyType)) {
      throw new IllegalArgumentException(
          "DoorBlocker entity could not be created: Only ItemKey.class or ItemBigKey.class are allowed as requiredKeyType");
    }

    String reqKeyName =
        requiredKeyType.equals(ItemKey.class) ? "silbernen Schlüssel" : "großen goldenen Schlüssel";
    Entity doorBlocker = new Entity("doorBlocker");
    float x = door.position().x();
    float y = door.position().y();
    Point blockerPosition = new Point(x, (y - 0.4f));
    doorBlocker.add(new PositionComponent(blockerPosition));

    doorBlocker.add(
        new InteractionComponent(
            2.0f,
            true,
            (interacted, interactor) -> {
              InventoryComponent invComp = interactor.fetch(InventoryComponent.class).orElse(null);
              if (invComp == null) {
                return;
              }

              if (!invComp.hasItem(requiredKeyType)) {
                DialogUtils.showTextPopup(
                    "Du brauchst einen " + reqKeyName + " um diese Tür zu öffnen!",
                    "Fehlender Schlüssel.");
                return;
              }

              YesNoDialog.showYesNoDialog(
                  "Willst du deinen " + reqKeyName + " verwenden, um die Tür zu öffnen?",
                  "Verschlossene Tür.",
                  () -> {
                    invComp.itemOfClass(requiredKeyType).ifPresent(invComp::remove);
                    Game.remove(interacted);
                    door.open();
                  },
                  () -> {
                    // "No" - do nothing
                  });
            }));
    door.close();
    doorBlocker.add(new DrawComponent(new Animation(DOOR_BLOCKER_TEXTURE)));
    return doorBlocker;
  }

  /**
   * Creates a generic destroyable object entity.
   *
   * <p>It has an {@link InventoryComponent} and can store items. All stored items are dropped upon
   * destruction.
   *
   * @param name The entity name (e.g. "stone", "vase").
   * @param texturePath The path prefix for the textures (e.g. "objects/stone").
   * @param spawnPoint The world position where the entity should be created.
   * @param requiredItemClass the class of an {@link Item} that must be present in the interactor's
   *     inventory to destroy this object. If {@code null}, no item is required for destruction.
   * @param items the items contained in the object that will drop upon destruction.
   * @return A new {@link Entity} configured with destruction behavior and animations.
   */
  public static Entity newDestroyableObject(
      String name,
      SimpleIPath texturePath,
      Point spawnPoint,
      final Class<? extends Item> requiredItemClass,
      final Set<Item> items) {

    Entity destroyableObj = new Entity(name);
    destroyableObj.add(new PositionComponent(spawnPoint));
    InventoryComponent objInvComp = new InventoryComponent(items.size());
    destroyableObj.add(objInvComp);
    items.forEach(objInvComp::add);
    // Initial InteractionComponent
    InteractionComponent baseIC =
        new InteractionComponent(
            2.0f,
            true,
            (interacted, interactor) -> {
              // Original behavior will be wrapped below
            });
    destroyableObj.add(baseIC);

    destroyableObj.add(new CollideComponent(Vector2.ZERO, Vector2.ONE));

    Map<String, Animation> animationMap = Animation.loadAnimationSpritesheet(texturePath);
    State stIdle = State.fromMap(animationMap, "idle");
    State stBreaking = State.fromMap(animationMap, "breaking");
    State stBroken = State.fromMap(animationMap, "broken");
    StateMachine sm = new StateMachine(Arrays.asList(stIdle, stBreaking, stBroken), stIdle);
    sm.addTransition(stIdle, "break", stBreaking);
    sm.addEpsilonTransition(stBreaking, State::isAnimationFinished, stBroken);
    DrawComponent dc = new DrawComponent(sm);
    destroyableObj.add(dc);

    // Wrapper-InteractionComponent
    destroyableObj
        .fetch(InteractionComponent.class)
        .ifPresent(
            oldIC -> {
              InteractionComponent wrapperIC =
                  new InteractionComponent(
                      2.0f,
                      true,
                      (interacted, interactor) -> {
                        // check if a specific item is required for destruction
                        if (requiredItemClass != null) {
                          boolean hasRequiredItem =
                              interactor
                                  .fetch(InventoryComponent.class)
                                  .map(inv -> inv.hasItem(requiredItemClass))
                                  .orElse(false);

                          if (!hasRequiredItem) {
                            return;
                          }
                        }

                        // start breaking Animation
                        dc.sendSignal("break");

                        // Drop all items from DestroyableObject inventory
                        Arrays.stream(objInvComp.items())
                            .filter(Objects::nonNull)
                            .forEach(
                                itemInInv -> {
                                  itemInInv.drop(spawnPoint);
                                  objInvComp.remove(itemInInv);
                                });

                        // remove interaction after successfully destroying the object
                        interacted.remove(InteractionComponent.class);
                      });

              destroyableObj.remove(InteractionComponent.class);
              destroyableObj.add(wrapperIC);
            });

    return destroyableObj;
  }

  /**
   * Creates a destructible vase entity.
   *
   * <p>Uses {@link #newDestroyableObject(String, SimpleIPath, Point, Class, Set)} with parameters
   * suitable for a vase: does not require a hammer to break and drops all items stored inside the
   * vase.
   *
   * @param spawnPoint The world position where the vase should be spawned.
   * @param items the items contained in the vase that will drop upon destruction.
   * @return A new {@link Entity} representing the destructible vase.
   */
  public static Entity newVase(Point spawnPoint, final Set<Item> items) {
    return newDestroyableObject("vase", VASE_TEXTURES, spawnPoint, null, items);
  }

  /**
   * Creates a destructible stone entity.
   *
   * <p>Uses {@link #newDestroyableObject(String, SimpleIPath, Point, Class, Set)} with parameters
   * suitable for a stone: requires a hammer to break and drops all items stored inside the stone.
   *
   * @param spawnPoint The world position where the stone should be spawned.
   * @param items the items contained in the stone that will drop upon destruction.
   * @return A new {@link Entity} representing the destructible stone.
   */
  public static Entity newStone(Point spawnPoint, final Set<Item> items) {
    return newDestroyableObject("stone", STONE_TEXTURES, spawnPoint, ItemHammer.class, items);
  }

  /**
   * Creates a destructible vase entity with randomized drops.
   *
   * <p>Uses {@link #newDestroyableObject(String, SimpleIPath, Point, Class, Set)} with parameters
   * suitable for a vase: does not require a hammer to break and drops all items stored inside the
   * vase.
   *
   * @param spawnPoint The world position where the vase should be spawned.
   * @param dropChance A value between 0.0 and 1.0 indicating the probability that the vase drops an
   *     item. If the random roll fails, the vase will drop nothing.
   * @return A new {@link Entity} representing the destructible vase.
   */
  public static Entity newVase(Point spawnPoint, float dropChance) {
    Set<Item> items = generateRandomDrop(dropChance);
    return newDestroyableObject("vase", VASE_TEXTURES, spawnPoint, null, items);
  }

  /**
   * Creates a destructible stone entity with randomized drops.
   *
   * <p>Uses {@link #newDestroyableObject(String, SimpleIPath, Point, Class, Set)} with parameters
   * suitable for a stone: requires a hammer to break and drops all items stored inside the stone.
   *
   * @param spawnPoint The world position where the stone should be spawned.
   * @param dropChance A value between 0.0 and 1.0 indicating the probability that the stone drops
   *     an item. If the random roll fails, the stone will drop nothing.
   * @return A new {@link Entity} representing the destructible stone.
   */
  public static Entity newStone(Point spawnPoint, float dropChance) {
    Set<Item> items = generateRandomDrop(dropChance);
    return newDestroyableObject("stone", STONE_TEXTURES, spawnPoint, ItemHammer.class, items);
  }

  /**
   * Helper to create a random drop set based on probability.
   *
   * <p>Item distribution is hardcoded as an example:
   *
   * <ul>
   *   <li>50% Heart
   *   <li>30% Arrows
   *   <li>20% Fairy
   * </ul>
   *
   * @param dropChance probability that any item will drop.
   * @return a set containing one random item, or empty if no drop occurs.
   */
  private static Set<Item> generateRandomDrop(float dropChance) {
    Set<Item> result = new HashSet<>();
    if (Math.random() <= dropChance) {
      double roll = Math.random();
      if (roll < 0.5) {
        result.add(new ItemHeart(5));
      } else if (roll < 0.8) {
        result.add(new ItemWoodenArrow(8));
      } else {
        result.add(new ItemFairy());
      }
    }
    return result;
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

  private static class FillState extends State {
    private final Animation empty;

    public FillState(String name, IPath pathFull, IPath pathEmpty, AnimationConfig config) {
      super(name, pathFull, config);
      empty = new Animation(pathEmpty, config);
    }

    public FillState(String name, IPath pathFull, IPath pathEmpty) {
      this(name, pathFull, pathEmpty, null);
    }

    public FillState(String name, Animation full, Animation empty) {
      super(name, full);
      this.empty = empty;
    }

    @Override
    public Animation getAnimation() {
      boolean isEmpty = (boolean) data;
      return isEmpty ? empty : super.getAnimation();
    }

    public static FillState fromMap(Map<String, Animation> animationMap, String name) {
      return new FillState(
          name, animationMap.get(name + "_full"), animationMap.get(name + "_empty"));
    }
  }
}
