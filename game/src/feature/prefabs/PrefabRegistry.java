package feature.prefabs;

import engine.level.elements.ILevel;
import feature.prefabs.types.BookshelfImagePrefab;
import feature.prefabs.types.ColorGradeRegionPrefab;
import feature.prefabs.types.DecoDialogPrefab;
import feature.prefabs.types.DecoImagePrefab;
import feature.prefabs.types.DesignLabelRegionPrefab;
import feature.prefabs.types.DoorLeverPrefab;
import feature.prefabs.types.DoorKeypadPrefab;
import feature.prefabs.types.DialogTriggerPrefab;
import feature.prefabs.types.LevelHiderPrefab;
import feature.prefabs.types.PressurePlatePrefab;
import feature.prefabs.types.PushableStonePrefab;
import feature.prefabs.types.TorchPrefab;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

/** Hard-coded registry of prefab definitions available to level files and the editor. */
public final class PrefabRegistry {

  private static final Map<String, Prefab> PREFABS = new LinkedHashMap<>();
  private static final Map<String, BiFunction<ILevel, String, ? extends Prefab>> VIEW_FACTORIES =
      new LinkedHashMap<>();

  static {
    register(new DoorKeypadPrefab(), DoorKeypadPrefab::new);
    register(
        new BookshelfImagePrefab(),
        (level, name) -> ((Prefab) new BookshelfImagePrefab()).bind(level, name));
    register(
        new LevelHiderPrefab(),
        (level, name) -> ((Prefab) new LevelHiderPrefab()).bind(level, name));
    register(new DesignLabelRegionPrefab(), DesignLabelRegionPrefab::new);
    register(new DoorLeverPrefab(), DoorLeverPrefab::new);
    register(new DecoDialogPrefab(), DecoDialogPrefab::new);
    register(new ColorGradeRegionPrefab(), ColorGradeRegionPrefab::new);
    register(new DialogTriggerPrefab(), DialogTriggerPrefab::new);
    register(new PushableStonePrefab(), PushableStonePrefab::new);
    register(new PressurePlatePrefab(), PressurePlatePrefab::new);
    register(new TorchPrefab(), TorchPrefab::new);
    register(new DecoImagePrefab(), DecoImagePrefab::new);
  }

  private PrefabRegistry() {}

  /**
   * Registers a prefab definition.
   *
   * @param prefab definition to register
   */
  public static void register(Prefab prefab) {
    register(prefab, null);
  }

  /**
   * Registers a definition and its explicit per-authored-instance view factory.
   *
   * <p>The factory must create a distinct, level-bound view for each call. Definitions registered
   * without a factory remain available for serialization and editor use, but cannot be returned
   * through {@link ILevel#prefabs(Class)}.
   *
   * @param prefab definition to register
   * @param viewFactory creates a view from its owning level and authored instance name
   */
  public static void register(
      Prefab prefab, BiFunction<ILevel, String, ? extends Prefab> viewFactory) {
    Prefab previous = PREFABS.putIfAbsent(prefab.type(), prefab);
    if (previous != null) {
      throw new IllegalStateException("Duplicate prefab type ID: " + prefab.type());
    }
    if (viewFactory != null) VIEW_FACTORIES.put(prefab.type(), viewFactory);
  }

  /**
   * Looks up a prefab definition.
   *
   * @param type stable type ID
   * @return matching definition
   */
  public static Optional<Prefab> find(String type) {
    return Optional.ofNullable(PREFABS.get(type));
  }

  /**
   * Resolves a prefab definition or fails with a level-facing message.
   *
   * @param type stable type ID
   * @return matching definition
   */
  public static Prefab require(String type) {
    return find(type)
        .orElseThrow(() -> new IllegalArgumentException("Unknown prefab type: '" + type + "'"));
  }

  /**
   * Creates a new per-instance view using the registered explicit factory.
   *
   * @param level owning level
   * @param instance authored instance to bind
   * @return a distinct, bound view of the registered type
   */
  public static Prefab createView(ILevel level, PrefabInstance instance) {
    Prefab definition = require(instance.type());
    BiFunction<ILevel, String, ? extends Prefab> factory = VIEW_FACTORIES.get(instance.type());
    if (factory == null) {
      throw new IllegalStateException(
          "Prefab type '" + instance.type() + "' has no typed-instance view factory");
    }
    Prefab view = factory.apply(level, instance.name());
    if (view == definition
        || !view.isBound()
        || !definition.getClass().isInstance(view)
        || !view.type().equals(definition.type())
        || !view.name().equals(instance.name())) {
      throw new IllegalStateException(
          "Invalid typed-instance view factory for prefab type '" + instance.type() + "'");
    }
    return view;
  }

  /**
   * Returns definitions in their editor display order.
   *
   * @return registered prefab definitions
   */
  public static Collection<Prefab> all() {
    return Collections.unmodifiableCollection(PREFABS.values());
  }
}
