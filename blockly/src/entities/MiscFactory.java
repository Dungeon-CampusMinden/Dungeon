package entities;

import client.Client;
import components.*;
import contrib.components.*;
import contrib.entities.LeverFactory;
import contrib.hud.DialogUtils;
import contrib.utils.ICommand;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Direction;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.components.draw.Animation;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.Map;

/** Factory class for creating miscellaneous game entities. */
public class MiscFactory {

  private static final IPath STONE = new SimpleIPath("objects/stone/stone.png");
  private static final IPath PRESSURE_PLATE_ON =
      new SimpleIPath("objects/pressureplate/on/pressureplate_0.png");
  private static final IPath PRESSURE_PLATE_OFF =
      new SimpleIPath("objects/pressureplate/off/pressureplate_0.png");

  private static final IPath PICKUP_BOCK_PATH = new SimpleIPath("items/book/spell_book.png");
  private static final IPath BREADCRUMB_PATH = new SimpleIPath("items/breadcrumbs.png");
  private static final IPath CLOVER_PATH = new SimpleIPath("items/clover.png");
  private static final IPath SCROLL_PATH = new SimpleIPath("items/book/magic_scroll.png");

  /**
   * Creates a stone entity at the given position.
   *
   * <p>A Stone is blocking and pushable entity.
   *
   * @param position The initial position of the stone.
   * @return A new stone entity.
   */
  public static Entity stone(Point position) {
    Entity stone = new Entity("stone");
    stone.add(new PushableComponent());
    stone.add(new PositionComponent(position.toCenteredPoint()));
    stone.add(new BlockComponent());
    stone.add(new VelocityComponent(Client.MOVEMENT_FORCE.x()));
    stone.add(new CollideComponent());
    stone.add(new BlockViewComponent());
    DrawComponent dc = new DrawComponent(Animation.fromSingleImage(STONE));
    stone.add(dc);
    return stone;
  }

  /**
   * Creates a pressure plate entity at the given position.
   *
   * <p>The pressure plate is an entity that reacts to collisions by toggling its lever state.
   *
   * @param position The initial position of the pressure plate.
   * @return A new pressure plate entity lever and collision behavior.
   */
  public static Entity pressurePlate(Point position) {
    Entity pressurePlate = new Entity("pressureplate");
    pressurePlate.add(new PositionComponent(position.toCenteredPoint()));
    DrawComponent dc = new DrawComponent(Animation.fromSingleImage(PRESSURE_PLATE_OFF));
    Map<String, Animation> animationMap =
        Map.of("off", dc.currentAnimation(), "on", Animation.fromSingleImage(PRESSURE_PLATE_ON));
    dc.animationMap(animationMap);
    dc.currentAnimation("off");
    pressurePlate.add(dc);
    LeverComponent lc = new LeverComponent(false, ICommand.NOOP);
    pressurePlate.add(lc);
    TriConsumer<Entity, Entity, Direction> collide =
        (entity, entity2, direction) -> {
          // dont trigger for projectiles
          if (entity2.isPresent(ProjectileComponent.class)) return;
          lc.toggle();
          if (lc.isOn()) dc.currentAnimation("on");
          else dc.currentAnimation("off");
        };
    pressurePlate.add(new CollideComponent(collide, collide));
    return pressurePlate;
  }

  /**
   * Create a Lever with a {@link BlockComponent}.
   *
   * @param position Position to place on (will be centered)
   * @return lever entity
   */
  public static Entity blocklyLever(final Point position) {
    Entity lever = LeverFactory.createLever(position.toCenteredPoint());
    lever.add(new BlockComponent());
    return lever;
  }

  /**
   * Create a Lever that looks like a torch with a {@link BlockComponent}.
   *
   * @param position Position to place on (will be centered)
   * @return lever entity
   */
  public static Entity blocklyTorch(final Point position) {
    Entity torch = LeverFactory.createTorch(position.toCenteredPoint());
    torch.add(new BlockComponent());
    return torch;
  }

  /**
   * Creates a book pickup entity at the given position.
   *
   * <p>The book pickup is an entity that, upon interaction, displays a text popup and removes
   * itself from the game world. This can be used for storytelling purposes or to unlock new
   * features in a Blockly-based level.
   *
   * @param position The initial position of the book pickup.
   * @param title The title displayed in the popup dialog.
   * @param pickupText The text content of the popup dialog shown to the player upon interaction.
   * @return A new book pickup entity with interaction behavior.
   */
  public static Entity bookPickup(Point position, String title, String pickupText) {
    Entity pickup = new Entity("Book Pickup");
    pickup.add(new PositionComponent(position.toCenteredPoint()));
    pickup.add(new DrawComponent(Animation.fromSingleImage(PICKUP_BOCK_PATH)));
    pickup.add(
        new InteractionComponent(
            0,
            false,
            (entity, entity2) -> {
              DialogUtils.showTextPopup(pickupText, title);
              Game.remove(pickup);
            }));
    return pickup;
  }

  /**
   * Creates a breadcrumb entity at the given position.
   *
   * <p>The breadcrumb is a temporary item that can be picked up and removed upon interaction. It
   * can be used for marking paths.
   *
   * @param position The initial position of the breadcrumb.
   * @return A new breadcrumb entity.
   */
  public static Entity breadcrumb(Point position) {
    Entity breadcrumb = new Entity("breadcrumb");
    breadcrumb.add(new PositionComponent(position.toCenteredPoint()));
    breadcrumb.add(new DrawComponent(Animation.fromSingleImage(BREADCRUMB_PATH)));
    breadcrumb.add(new BlocklyItemComponent());
    breadcrumb.add(new BreadcrumbComponent());
    breadcrumb.add(
        new InteractionComponent(
            0,
            false,
            (entity, entity2) -> {
              Game.remove(breadcrumb);
            }));
    return breadcrumb;
  }

  /**
   * Creates a Clover entity at the given position.
   *
   * <p>The Clover is a temporary item that can be picked up and removed upon interaction. It can be
   * used for marking paths.
   *
   * @param position The initial position of the clover.
   * @return A new clover entity.
   */
  public static Entity clover(Point position) {
    Entity breadcrumb = new Entity("clover");
    breadcrumb.add(new PositionComponent(position.toCenteredPoint()));
    breadcrumb.add(new DrawComponent(Animation.fromSingleImage(CLOVER_PATH)));
    breadcrumb.add(new BlocklyItemComponent());
    breadcrumb.add(new CloverComponent());
    breadcrumb.add(
        new InteractionComponent(
            0,
            false,
            (entity, entity2) -> {
              Game.remove(breadcrumb);
            }));
    return breadcrumb;
  }

  /**
   * Creates a fireballScroll at the given position.
   *
   * <p>The fireballScroll is a temporary item that can be picked up to increase the current
   * ammunition.
   *
   * @param position The initial position of the fireballScroll.
   * @return A new fireballScroll entity.
   */
  public static Entity fireballScroll(Point position) {
    Entity fireballScroll = new Entity("fireballScroll");
    fireballScroll.add(new PositionComponent(position.toCenteredPoint()));
    fireballScroll.add(new DrawComponent(Animation.fromSingleImage(SCROLL_PATH)));
    fireballScroll.add(new BlocklyItemComponent());
    fireballScroll.add(
        new InteractionComponent(
            0,
            false,
            (entity, hero) -> {
              hero.fetch(AmmunitionComponent.class).map(AmmunitionComponent::collectAmmo);
              Game.remove(fireballScroll);
            }));
    return fireballScroll;
  }
}
