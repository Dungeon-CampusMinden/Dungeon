package feature.prefabs.types;

import engine.Entity;
import engine.level.elements.ILevel;
import feature.prefabs.ActionContext;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabAction;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEvent;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabPort;
import feature.prefabs.PrefabSide;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Server-side gate that passes a signal on only the first time, e.g. to show a dialog once.
 *
 * <p>The first {@link #TRIGGER} raises {@link #TRIGGERED} with the player that caused it, later
 * ones are ignored. Resetting the instance, or deactivating and activating it, opens the gate
 * again.
 */
public final class OncePrefab extends Prefab {

  private static final String TYPE = "once";
  private static final String DISPLAY_NAME = "Once";
  private static final SpawnState<boolean[]> TRIGGERED_STATE = new SpawnState<>();

  /** Raised on the first trigger only, with the player that caused it. */
  public static final PrefabEvent<Entity> TRIGGERED =
      PrefabEvent.withPlayer("triggered", "Triggered");

  /** Raises {@link #TRIGGERED} if the gate was not triggered before. */
  public static final PrefabAction TRIGGER =
      PrefabAction.of("trigger", "Trigger", OncePrefab::trigger);

  /** Creates the once definition. */
  public OncePrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, List.of());
  }

  /**
   * Creates a bound view for one authored once gate.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public OncePrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, List.of(), level, name);
  }

  /**
   * Registers a listener called on the server when this instance passed its signal on.
   *
   * @param listener receives the player that caused the trigger, or {@code null}
   */
  public void onTriggered(Consumer<Entity> listener) {
    listen(TRIGGERED, listener);
  }

  @Override
  protected List<PrefabPort> declaredPorts() {
    return List.of(TRIGGER, TRIGGERED);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    TRIGGERED_STATE.start(context.level(), instance.name(), new boolean[1]);
    return List.of();
  }

  @Override
  public void onDespawn(PrefabCreationContext context, PrefabInstance instance) {
    TRIGGERED_STATE.end(context.level(), instance.name());
  }

  @Override
  public Optional<String> editorSummary(PrefabInstance instance) {
    return Optional.of("passes the first trigger on");
  }

  private static void trigger(ActionContext context) {
    TRIGGERED_STATE
        .get(context.level(), context.instance().name())
        .filter(triggered -> !triggered[0])
        .ifPresent(
            triggered -> {
              triggered[0] = true;
              fire(
                  context.creationContext(),
                  context.instance(),
                  TRIGGERED,
                  context.player().orElse(null));
            });
  }
}
