package feature.prefabs.types;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.VelocityComponent;
import engine.level.elements.ILevel;
import engine.utils.Point;
import engine.utils.Vector2;
import engine.utils.components.draw.DepthLayer;
import feature.components.CollideComponent;
import feature.components.DecoComponent;
import feature.entities.WorldItemBuilder;
import feature.entities.deco.Deco;
import feature.entities.deco.DecoFactory;
import feature.hud.dialogs.DialogFactory;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import feature.inventory.Item;
import feature.prefabs.ActionContext;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabAction;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.PrefabEvent;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabPort;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Server-side air vent holding a list of items, which it blows out into the room when {@link #SPEW}
 * runs, like the vent spewing out paper snippets in The Last Hour.
 *
 * <p>Interacting with the vent only shows a dialog, its items can not be taken out directly. Every
 * item is also an artifact, so e.g. an {@link ItemReceptaclePrefab} can wait for one of them. A
 * reset puts the items back into the vent and removes spewed items that are still lying around.
 */
public final class AirVentPrefab extends Prefab {

  private static final String TYPE = "air-vent";
  private static final String DISPLAY_NAME = "Air Vent";
  private static final String VENT_ID = "air-vent-impulse";
  private static final float SPEW_SPEED = 45f;
  private static final float SPEW_MAX_SPEED = 45f;
  private static final float SPEW_MASS = 25f;
  private static final SpawnState<VentState> VENTS = new SpawnState<>();

  /** Position of the vent. */
  public static final PrefabProperty<Point> POSITION =
      PrefabProperty.point("position", "Position", new Point(0, 0));

  /** Decoration shown for the vent. */
  public static final PrefabProperty<String> DECO =
      PrefabProperty.selection(
          "deco",
          "Decoration",
          Deco.FloorBarsSmall.name(),
          Arrays.stream(Deco.values()).map(Enum::name).toList());

  /** Dialog script shown when a player interacts with the vent. */
  public static final PrefabProperty<String> MESSAGE =
      PrefabProperty.string(
          "message",
          "Interaction Dialog",
          "Warm air is blowing out of the vent.",
          value -> !value.isBlank());

  /** Direction the items fly into when they are spewed out. */
  public static final PrefabProperty<String> DIRECTION =
      PrefabProperty.selection(
          "direction",
          "Spew Direction",
          Direction.DOWN.label,
          Arrays.stream(Direction.values()).map(direction -> direction.label).toList());

  /** Raised when a player interacted with the vent, with the player as payload. */
  public static final PrefabEvent<Entity> INTERACTED =
      PrefabEvent.withPlayer("interacted", "Interacted");

  /** Raised when the vent spewed out its items. */
  public static final PrefabEvent<Void> ITEMS_SPEWED =
      PrefabEvent.signal("items-spewed", "Items Spewed");

  /** Blows the items out of the vent. Does nothing once the vent is empty. */
  public static final PrefabAction SPEW =
      PrefabAction.of("spew", "Spew Items", AirVentPrefab::spew);

  private static final List<PrefabProperty<?>> PROPERTIES = allProperties();

  /** Creates the air-vent definition. */
  public AirVentPrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a bound view for one authored air vent.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public AirVentPrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES, level, name);
  }

  @Override
  protected List<PrefabPort> declaredPorts() {
    List<PrefabPort> ports = new ArrayList<>(PrefabItems.OUTPUTS);
    ports.addAll(List.of(SPEW, ITEMS_SPEWED, INTERACTED));
    return ports;
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    VENTS.start(
        context.level(),
        instance.name(),
        new VentState(
            new ArrayList<>(PrefabItems.create(context.level(), instance)), new ArrayList<>()));

    Entity vent = context.createEntity(instance.name());
    DecoFactory.createDeco(vent, value(instance, POSITION), Deco.valueOf(value(instance, DECO)));
    // Keeps the vent out of the level's decorations, it belongs to this prefab.
    vent.remove(DecoComponent.class);
    String message = value(instance, MESSAGE);
    vent.add(
        new InteractionComponent(
            new Interaction(
                (entity, who) -> {
                  DialogFactory.showDialogDialog(message, () -> {}, who.id());
                  fire(context, instance, INTERACTED, who);
                })));
    return List.of(vent);
  }

  @Override
  public void onDespawn(PrefabCreationContext context, PrefabInstance instance) {
    VENTS
        .end(context.level(), instance.name())
        .ifPresent(
            state ->
                state.spewed().stream()
                    .filter(
                        entity ->
                            Game.findEntityById(entity.id())
                                .filter(found -> found == entity)
                                .isPresent())
                    .forEach(Game::remove));
  }

  @Override
  public void renderEditorFeedback(
      ILevel level, PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {
    Point position = value(instance, POSITION);
    feedback.point(position, instance.name());
    if (selected) {
      Direction direction = Direction.fromLabel(value(instance, DIRECTION));
      Point center = position.translate(0.5f, 0.5f);
      feedback.label(
          center.translate(direction.x * 1.2f, direction.y * 1.2f),
          "(" + value(instance, PrefabItems.COUNT) + " items)");
    }
  }

  private static void spew(ActionContext context) {
    VENTS
        .get(context.level(), context.instance().name())
        .filter(state -> !state.items().isEmpty())
        .ifPresent(
            state -> {
              Direction direction = Direction.fromLabel(context.value(DIRECTION));
              Point origin =
                  context.value(POSITION).translate(direction.x * 0.25f, direction.y * 0.25f);
              List<Item> items = state.items();
              for (int index = 0; index < items.size(); index++) {
                Entity item = WorldItemBuilder.buildWorldItem(items.get(index), origin);
                item.fetch(CollideComponent.class).ifPresent(collide -> collide.isSolid(false));
                item.fetch(DrawComponent.class)
                    .ifPresent(draw -> draw.depth(DepthLayer.BackgroundDeco.depth()));
                VelocityComponent velocity = new VelocityComponent(SPEW_MAX_SPEED, SPEW_MASS);
                velocity.applyForce(VENT_ID, impulse(direction, index, items.size()));
                item.add(velocity);
                Game.add(item);
                state.spewed().add(item);
              }
              items.clear();
              fire(context.creationContext(), context.instance(), ITEMS_SPEWED, null);
            });
  }

  /**
   * Returns the impulse of one spewed item. The items fan out sideways and fly different distances.
   *
   * @param direction spew direction
   * @param index index of the item
   * @param count number of spewed items
   * @return impulse of the item
   */
  private static Vector2 impulse(Direction direction, int index, int count) {
    float sideways = count == 1 ? 0f : -1f + 2f * index / (count - 1);
    float forward = 1f + (index % 3) * 0.5f;
    // The sideways axis is the spew direction rotated by 90 degrees.
    return Vector2.of(
        (direction.x * forward - direction.y * sideways) * SPEW_SPEED,
        (direction.y * forward + direction.x * sideways) * SPEW_SPEED);
  }

  private static List<PrefabProperty<?>> allProperties() {
    List<PrefabProperty<?>> properties =
        new ArrayList<>(List.of(POSITION, DECO, MESSAGE, DIRECTION));
    properties.addAll(PrefabItems.properties());
    return List.copyOf(properties);
  }

  /**
   * Runtime state of one spawned vent.
   *
   * @param items items still inside the vent
   * @param spewed world items the vent spewed out
   */
  private record VentState(List<Item> items, List<Entity> spewed) {}

  /** Directions the vent can spew items into. */
  private enum Direction {
    DOWN("Down", 0, -1),
    UP("Up", 0, 1),
    LEFT("Left", -1, 0),
    RIGHT("Right", 1, 0);

    private final String label;
    private final int x;
    private final int y;

    Direction(String label, int x, int y) {
      this.label = label;
      this.x = x;
      this.y = y;
    }

    private static Direction fromLabel(String label) {
      for (Direction direction : values()) {
        if (direction.label.equals(label)) return direction;
      }
      return DOWN;
    }
  }
}
