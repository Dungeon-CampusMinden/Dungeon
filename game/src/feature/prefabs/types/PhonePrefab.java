package feature.prefabs.types;

import engine.Entity;
import engine.level.elements.ILevel;
import engine.utils.Point;
import feature.hud.dialogs.DialogFactory;
import feature.interaction.keypad.KeypadFactory;
import feature.interaction.phone.PhoneComponent;
import feature.interaction.phone.PhoneFactory;
import feature.interaction.phone.PhoneLine;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.PrefabEvent;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabPort;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.JsonNode;

/**
 * Server-side phone with up to {@link #MAX_LINES} lines. Dialing the number of a line closes the
 * phone, shows the line's announcement and raises the line's own event, so each line can trigger
 * something different, e.g. link one to {@link DoorControlPrefab#OPEN} or to {@link
 * DialogPrefab#SHOW_DIALOG}. Any other number gets "no connection" and raises {@link
 * #NO_CONNECTION}.
 *
 * <p>The note of important extensions beside the dial pad lists the lines marked for it; without
 * such a line, the phone has no note.
 */
public final class PhonePrefab extends Prefab {

  /** Maximum number of lines. */
  public static final int MAX_LINES = 8;

  private static final String TYPE = "phone";
  private static final String DISPLAY_NAME = "Phone";

  /** Raised when a player dialed a number without a line, with the player as payload. */
  public static final PrefabEvent<Entity> NO_CONNECTION =
      PrefabEvent.withPlayer("no-connection", "No Connection");

  public static final PrefabProperty<Point> POSITION =
      PrefabProperty.point("position", "Position", new Point(0, 0), new Point(0.5f, 0.5f));

  /** Digits a number can have. */
  public static final PrefabProperty<Integer> MAX_DIGITS =
      PrefabProperty.integer("max-digits", "Max Digits", 4, 1, PhoneFactory.MAX_DIGITS);

  /** Text the phone shows for a number without a line; empty shows the engine's default text. */
  public static final PrefabProperty<String> NO_CONNECTION_TEXT =
      PrefabProperty.string(
          "no-connection-text", "No Connection Text (empty = default)", "", value -> true);

  /** Number of lines. */
  public static final PrefabProperty<Integer> LINE_COUNT =
      PrefabProperty.integer("line-count", "Line Count", 1, 1, MAX_LINES);

  /** Properties and event of every line, only used up to {@link #LINE_COUNT}. */
  public static final List<Line> LINES = lines();

  private static final List<PrefabEvent<Entity>> DIALED = LINES.stream().map(Line::dialed).toList();

  private static final List<PrefabProperty<?>> PROPERTIES = propertyList();

  /**
   * Properties and event of one line.
   *
   * @param number digits that reach the line
   * @param name what the note calls the line
   * @param onNote whether the note lists the line
   * @param announcement dialog script shown to the caller; empty shows none
   * @param dialed raised on the server when a player dialed the line, with the player as payload
   */
  public record Line(
      PrefabProperty<String> number,
      PrefabProperty<String> name,
      PrefabProperty<Boolean> onNote,
      PrefabProperty<String> announcement,
      PrefabEvent<Entity> dialed) {}

  /** Creates the phone definition. */
  public PhonePrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a bound view for one authored phone.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public PhonePrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES, level, name);
  }

  @Override
  protected List<PrefabPort> declaredPorts() {
    List<PrefabPort> ports = new ArrayList<>(DIALED);
    ports.add(NO_CONNECTION);
    return ports;
  }

  @Override
  protected boolean isPortAvailable(PrefabInstance instance, PrefabPort port) {
    int index = DIALED.indexOf(port);
    return index < 0 || index < instance.get(LINE_COUNT);
  }

  @Override
  public String portDisplayName(PrefabInstance instance, PrefabPort port) {
    int index = DIALED.indexOf(port);
    if (index < 0 || index >= instance.get(LINE_COUNT)) return port.displayName();
    Line line = LINES.get(index);
    String label = (line.number().get(instance) + " " + line.name().get(instance)).strip();
    return port.displayName() + " \"" + label + "\"";
  }

  @Override
  protected void validate(PrefabInstance instance) {
    // A phone without reactions refuses numbers with too many digits and numbers taken twice, like
    // the spawned phone would.
    PhoneComponent check = new PhoneComponent(value(instance, MAX_DIGITS));
    for (Line line : usedLines(instance)) check.addLine(phoneLine(instance, line), who -> {});
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Entity phone = context.createEntity(instance.name());
    PhoneFactory.createPhone(phone, value(instance, POSITION), value(instance, MAX_DIGITS));
    PhoneComponent component = phone.fetch(PhoneComponent.class).orElseThrow();
    for (Line line : usedLines(instance)) {
      component.addLine(phoneLine(instance, line), who -> call(context, instance, line, who));
    }
    component.noConnection(value(instance, NO_CONNECTION_TEXT));
    component.onNoConnection(who -> fire(context, instance, NO_CONNECTION, who));
    return List.of(phone);
  }

  @Override
  public void renderEditorFeedback(
      ILevel level, PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {
    Point position = value(instance, POSITION);
    Point offset = POSITION.editorFeedbackOffset();
    feedback.point(position.translate(offset.x(), offset.y()), instance.name());
  }

  private List<Line> usedLines(PrefabInstance instance) {
    return LINES.subList(0, value(instance, LINE_COUNT));
  }

  private PhoneLine phoneLine(PrefabInstance instance, Line line) {
    return new PhoneLine(
        value(instance, line.number()),
        value(instance, line.name()),
        value(instance, line.onNote()));
  }

  // The phone has closed; the line's announcement opens in its place.
  private static void call(
      PrefabCreationContext context, PrefabInstance instance, Line line, Entity who) {
    String announcement = line.announcement().get(instance);
    if (!announcement.isBlank()) DialogFactory.showDialogDialog(announcement, () -> {}, who.id());
    fire(context, instance, line.dialed(), who);
  }

  private static List<PrefabProperty<?>> propertyList() {
    List<PrefabProperty<?>> properties = new ArrayList<>();
    properties.add(POSITION);
    properties.add(MAX_DIGITS);
    properties.add(NO_CONNECTION_TEXT);
    properties.add(LINE_COUNT);
    for (Line line : LINES) {
      properties.add(line.number());
      properties.add(line.name());
      properties.add(line.onNote());
      properties.add(line.announcement());
    }
    return List.copyOf(properties);
  }

  private static List<Line> lines() {
    List<Line> lines = new ArrayList<>(MAX_LINES);
    for (int index = 0; index < MAX_LINES; index++) {
      int number = index + 1;
      String prefix = "line-" + number;
      String label = "Line " + number;
      Line line =
          new Line(
              PrefabProperty.string(
                  prefix + "-number",
                  label + " Number",
                  String.valueOf(number),
                  KeypadFactory::isNumericCode),
              PrefabProperty.string(prefix + "-name", label + " Name", label, value -> true),
              PrefabProperty.bool(prefix + "-on-note", label + " On Note", true),
              PrefabProperty.string(
                  prefix + "-announcement",
                  label + " Announcement (Dialog Script)",
                  "Hello?",
                  value -> true),
              PrefabEvent.withPlayer(prefix, label));
      line.number().visibleWhen(values -> lineCount(values) >= number);
      line.name().visibleWhen(values -> lineCount(values) >= number);
      line.onNote().visibleWhen(values -> lineCount(values) >= number);
      line.announcement().visibleWhen(values -> lineCount(values) >= number);
      lines.add(line);
    }
    return List.copyOf(lines);
  }

  private static int lineCount(Map<String, JsonNode> values) {
    JsonNode count = values.get(LINE_COUNT.key());
    return count == null || !count.isInt() ? LINE_COUNT.defaultValue() : count.asInt();
  }
}
