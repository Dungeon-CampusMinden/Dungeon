package feature.prefabs.types;

import engine.Entity;
import engine.level.elements.ILevel;
import feature.prefabs.ActionContext;
import feature.prefabs.GameFlags;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabAction;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEvent;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabPort;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Server-side logic prefab that routes a signal by {@link GameFlags game flags}.
 *
 * <p>Every flag has its own event. {@link #CHECK} raises the event of the first flag in the list
 * that exists, or {@link #NO_FLAG} if none of them does.
 */
public final class FlagSwitchPrefab extends Prefab {

  private static final String TYPE = "flag-switch";
  private static final String DISPLAY_NAME = "Flag Switch";

  // Checked flags, in order.
  private static final PrefabFlagList FLAGS = PrefabFlagList.create(2);

  /**
   * Event of every flag, raised when it is the first existing one, with the player that caused the
   * check. Instances only offer the events of their used flags.
   */
  public static final List<PrefabEvent<Entity>> FLAG_EXISTS = flagEvents();

  /** Raised when none of the flags exists, with the player that caused the check. */
  public static final PrefabEvent<Entity> NO_FLAG =
      PrefabEvent.withPlayer("no-flag", "No Flag Set");

  /** Raises the event of the first existing flag. */
  public static final PrefabAction CHECK =
      PrefabAction.of("check", "Check Flags", FlagSwitchPrefab::check);

  private static final List<PrefabProperty<?>> PROPERTIES = FLAGS.properties();

  /** Creates the flag-switch definition. */
  public FlagSwitchPrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a bound view for one authored flag switch.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public FlagSwitchPrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES, level, name);
  }

  @Override
  protected List<PrefabPort> declaredPorts() {
    List<PrefabPort> ports = new ArrayList<>();
    ports.add(CHECK);
    ports.addAll(FLAG_EXISTS);
    ports.add(NO_FLAG);
    return ports;
  }

  @Override
  protected boolean isPortAvailable(PrefabInstance instance, PrefabPort port) {
    int index = FLAG_EXISTS.indexOf(port);
    return index < 0 || index < instance.get(FLAGS.count());
  }

  @Override
  public String portDisplayName(PrefabInstance instance, PrefabPort port) {
    int index = FLAG_EXISTS.indexOf(port);
    if (index < 0 || index >= instance.get(FLAGS.count())) return port.displayName();
    return PrefabTextList.portName(port, FLAGS.names().get(index).get(instance));
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    return List.of();
  }

  @Override
  public Optional<String> editorSummary(PrefabInstance instance) {
    return Optional.of(FLAGS.summary(instance));
  }

  private static void check(ActionContext context) {
    PrefabInstance instance = context.instance();
    Entity player = context.player().orElse(null);
    PrefabCreationContext creation = context.creationContext();
    List<String> flags = FLAGS.values(instance);
    for (int index = 0; index < flags.size(); index++) {
      if (GameFlags.isSet(flags.get(index))) {
        fire(creation, instance, FLAG_EXISTS.get(index), player);
        return;
      }
    }
    fire(creation, instance, NO_FLAG, player);
  }

  private static List<PrefabEvent<Entity>> flagEvents() {
    List<PrefabEvent<Entity>> events = new ArrayList<>(PrefabFlagList.MAX_FLAGS);
    for (int index = 0; index < PrefabFlagList.MAX_FLAGS; index++) {
      int number = index + 1;
      events.add(PrefabEvent.withPlayer("flag-" + number, "Flag " + number));
    }
    return List.copyOf(events);
  }
}
