package feature.prefabs.types;

import engine.Entity;
import engine.level.elements.ILevel;
import feature.prefabs.ActionContext;
import feature.prefabs.GameFlags;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabAction;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabPort;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import java.util.List;
import java.util.Optional;

/** Server-side logic prefab that sets, removes or toggles a {@link GameFlags game flag}. */
public final class FlagSetPrefab extends Prefab {

  private static final String TYPE = "flag-set";
  private static final String DISPLAY_NAME = "Flag Set";
  private static final String OPERATION_SET = "Set";
  private static final String OPERATION_REMOVE = "Remove";
  private static final String OPERATION_TOGGLE = "Toggle";

  /** Name of the changed flag. */
  public static final PrefabProperty<String> FLAG =
      PrefabProperty.string("flag", "Flag", "flag", value -> !value.isBlank());

  /** What happens to the flag. */
  public static final PrefabProperty<String> OPERATION =
      PrefabProperty.selection(
          "operation",
          "Operation",
          OPERATION_SET,
          List.of(OPERATION_SET, OPERATION_REMOVE, OPERATION_TOGGLE));

  /** Changes the flag. */
  public static final PrefabAction APPLY = PrefabAction.of("apply", "Apply", FlagSetPrefab::apply);

  private static final List<PrefabProperty<?>> PROPERTIES = List.of(FLAG, OPERATION);

  /** Creates the flag-set definition. */
  public FlagSetPrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a bound view for one authored flag-set instance.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public FlagSetPrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES, level, name);
  }

  @Override
  protected List<PrefabPort> declaredPorts() {
    return List.of(APPLY);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    return List.of();
  }

  @Override
  public Optional<String> editorSummary(PrefabInstance instance) {
    return Optional.of(value(instance, OPERATION) + " \"" + value(instance, FLAG) + "\"");
  }

  private static void apply(ActionContext context) {
    String flag = context.value(FLAG);
    Optional<Entity> player = context.player();
    switch (context.value(OPERATION)) {
      case OPERATION_SET -> GameFlags.set(flag, player);
      case OPERATION_REMOVE -> GameFlags.remove(flag, player);
      default -> GameFlags.toggle(flag, player);
    }
  }
}
