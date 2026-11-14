package feature.prefabs.types;

import com.badlogic.gdx.graphics.Color;
import engine.Entity;
import engine.level.elements.ILevel;
import engine.utils.Point;
import feature.components.DecoComponent;
import feature.entities.deco.Deco;
import feature.entities.deco.DecoFactory;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import feature.interaction.cables.CablePuzzle;
import feature.interaction.cables.Cables;
import feature.interaction.cables.CablesComponent;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.PrefabEvent;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabPort;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import tools.jackson.databind.JsonNode;

/**
 * Server-side cable puzzle: players plug loose cables into their sockets and raise {@link
 * #ALL_CONNECTED} once every cable is in. Link it to e.g. {@link DoorControlPrefab#OPEN} to open a
 * door with it.
 *
 * <p>Every cable has a label, a color and a socket. The socket shows the label of its cable unless
 * it has its own, e.g. the definition of a term. The sockets can be shuffled once at spawn, so the
 * order stays the same for all players. See {@link Cables} for the dialog.
 */
public final class CablesPrefab extends Prefab {

  private static final String TYPE = "cables";
  private static final String DISPLAY_NAME = "Cables";
  private static final float INTERACTION_RADIUS = 1.5f;
  private static final List<Color> COLOR_DEFAULTS =
      List.of(
          Color.valueOf("e0483e"),
          Color.valueOf("3d7be0"),
          Color.valueOf("f7c31d"),
          Color.valueOf("63e283"),
          Color.valueOf("eef2f4"),
          Color.valueOf("ff9b30"),
          Color.valueOf("a66be0"),
          Color.valueOf("3fc8d0"));

  /** Raised when a player plugged in the last cable, with the player as payload. */
  public static final PrefabEvent<Entity> ALL_CONNECTED =
      PrefabEvent.withPlayer("all-connected", "All Connected");

  /** Raised when a player put a cable into another cable's socket, with the player as payload. */
  public static final PrefabEvent<Entity> WRONG_PLUG =
      PrefabEvent.withPlayer("wrong-plug", "Wrong Plug");

  /** Bottom-left position of the decoration. */
  public static final PrefabProperty<Point> POSITION =
      PrefabProperty.point("position", "Position", new Point(0, 0));

  /** Decoration that opens the puzzle, such as an electric panel or a terminal. */
  public static final PrefabProperty<String> DECO =
      PrefabProperty.selection(
          "deco",
          "Decoration",
          Deco.LabElectricPanel.name(),
          Arrays.stream(Deco.values()).map(Enum::name).toList());

  /** Title of the dialog, empty for the default title. */
  public static final PrefabProperty<String> TITLE =
      PrefabProperty.string("title", "Title (empty = default)", "", value -> true);

  /** How to plug the cables, shown above them; empty for the default help. */
  public static final PrefabProperty<String> HELP =
      PrefabProperty.string("help", "Help Text (empty = default)", "", value -> true);

  /** Whether the sockets sit in a random order instead of the order of their cables. */
  public static final PrefabProperty<Boolean> SHUFFLE =
      PrefabProperty.bool("shuffle", "Shuffle Sockets", true);

  /**
   * Whether the puzzle stays solved; otherwise every opening starts with loose cables and raises
   * {@link #ALL_CONNECTED} again when solved.
   */
  public static final PrefabProperty<Boolean> ONCE =
      PrefabProperty.bool("once", "Stays Solved", true);

  /** Number of cables. */
  public static final PrefabProperty<Integer> CABLE_COUNT =
      PrefabProperty.integer(
          "cable-count", "Cable Count", 3, CablePuzzle.MIN_CABLES, CablePuzzle.MAX_CABLES);

  /** Label of every cable, only used up to {@link #CABLE_COUNT}. */
  public static final List<PrefabProperty<String>> CABLES =
      IntStream.rangeClosed(1, CablePuzzle.MAX_CABLES)
          .mapToObj(
              number ->
                  forCable(
                      PrefabProperty.string(
                          "cable-" + number,
                          "Cable " + number,
                          "Cable " + number,
                          value -> !value.isBlank()),
                      number))
          .toList();

  /** Label of the socket of every cable, empty for the label of the cable. */
  public static final List<PrefabProperty<String>> SOCKETS =
      IntStream.rangeClosed(1, CablePuzzle.MAX_CABLES)
          .mapToObj(
              number ->
                  forCable(
                      PrefabProperty.string(
                          "socket-" + number,
                          "Socket " + number + " (empty = label of Cable " + number + ")",
                          "",
                          value -> true),
                      number))
          .toList();

  /** Color of every cable. */
  public static final List<PrefabProperty<Color>> COLORS =
      IntStream.rangeClosed(1, CablePuzzle.MAX_CABLES)
          .mapToObj(
              number ->
                  forCable(
                      PrefabProperty.color(
                          "color-" + number, "Color " + number, COLOR_DEFAULTS.get(number - 1)),
                      number))
          .toList();

  private static final List<PrefabProperty<?>> PROPERTIES = propertyList();

  /** Creates the cables definition. */
  public CablesPrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a bound view for one authored cable puzzle.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public CablesPrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES, level, name);
  }

  @Override
  protected List<PrefabPort> declaredPorts() {
    return List.of(ALL_CONNECTED, WRONG_PLUG);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Entity entity = context.createEntity(instance.name());
    DecoFactory.createDeco(entity, value(instance, POSITION), Deco.valueOf(value(instance, DECO)));
    entity.remove(DecoComponent.class);

    CablesComponent cables = new CablesComponent(puzzle(instance), value(instance, ONCE));
    cables.onConnected(who -> fire(context, instance, ALL_CONNECTED, who));
    cables.onWrongPlug(who -> fire(context, instance, WRONG_PLUG, who));
    entity.add(cables);
    String title = value(instance, TITLE);
    String help = value(instance, HELP);
    entity.add(
        new InteractionComponent(
            new Interaction(
                (self, who) -> Cables.show(self, who, title, help), INTERACTION_RADIUS)));
    return List.of(entity);
  }

  @Override
  public void renderEditorFeedback(
      ILevel level, PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {
    feedback.point(value(instance, POSITION), instance.name());
  }

  // Socket i belongs to cable i until the sockets are shuffled.
  private static CablePuzzle puzzle(PrefabInstance instance) {
    int count = instance.get(CABLE_COUNT);
    List<Integer> order = new ArrayList<>(IntStream.range(0, count).boxed().toList());
    if (instance.get(SHUFFLE)) Collections.shuffle(order);
    List<CablePuzzle.Cable> cables = new ArrayList<>();
    List<String> sockets = new ArrayList<>();
    for (int index = 0; index < count; index++) {
      cables.add(
          new CablePuzzle.Cable(
              CABLES.get(index).get(instance),
              COLORS.get(index).get(instance),
              order.indexOf(index)));
      int owner = order.get(index);
      String socket = SOCKETS.get(owner).get(instance);
      sockets.add(socket.isBlank() ? CABLES.get(owner).get(instance) : socket);
    }
    return new CablePuzzle(cables, sockets);
  }

  private static List<PrefabProperty<?>> propertyList() {
    List<PrefabProperty<?>> properties = new ArrayList<>();
    properties.addAll(List.of(POSITION, DECO, TITLE, HELP, SHUFFLE, ONCE, CABLE_COUNT));
    for (int index = 0; index < CablePuzzle.MAX_CABLES; index++) {
      properties.add(CABLES.get(index));
      properties.add(SOCKETS.get(index));
      properties.add(COLORS.get(index));
    }
    return List.copyOf(properties);
  }

  // The editor shows the properties of a cable only up to the cable count.
  private static <T> PrefabProperty<T> forCable(PrefabProperty<T> property, int number) {
    return property.visibleWhen(values -> cableCount(values) >= number);
  }

  private static int cableCount(Map<String, JsonNode> values) {
    JsonNode count = values.get(CABLE_COUNT.key());
    return count == null || !count.isInt() ? CABLE_COUNT.defaultValue() : count.asInt();
  }
}
