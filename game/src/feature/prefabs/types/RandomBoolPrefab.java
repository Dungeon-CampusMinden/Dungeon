package feature.prefabs.types;

import engine.Entity;
import engine.level.elements.ILevel;
import feature.prefabs.ArtifactOutput;
import feature.prefabs.ArtifactType;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabAction;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEvent;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabPort;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Server-side generator of a random truth value, e.g. to decide which of two doors opens.
 *
 * <p>Besides publishing the value as an artifact, the instance raises {@link #ROLLED_TRUE} or
 * {@link #ROLLED_FALSE}, so other prefabs can branch on it. Without a seed, a new value is
 * generated whenever the instance spawns. With a seed, the values follow the same sequence in every
 * session.
 */
public final class RandomBoolPrefab extends Prefab {

  private static final String TYPE = "random-bool";
  private static final String DISPLAY_NAME = "Random Bool";
  private static final Random RANDOM = new SecureRandom();
  private static final SpawnState<Random> GENERATORS = new SpawnState<>();

  /** Chance of generating {@code true}, in percent. */
  public static final PrefabProperty<Integer> CHANCE =
      PrefabProperty.integer("chance", "Chance of True (%)", 50, 0, 100);

  /** Seed for values that are the same in every session; empty for new values every session. */
  public static final PrefabProperty<String> SEED =
      PrefabProperty.string("seed", "Seed (empty = random)", "", RandomBoolPrefab::isValidSeed);

  /** The generated value. */
  public static final ArtifactOutput<Boolean> VALUE =
      ArtifactOutput.of("value", "Value", ArtifactType.BOOLEAN);

  /** Raised whenever a new value was generated, including when the instance spawns. */
  public static final PrefabEvent<Void> GENERATED = PrefabEvent.signal("generated", "Generated");

  /** Raised whenever {@code true} was generated. */
  public static final PrefabEvent<Void> ROLLED_TRUE =
      PrefabEvent.signal("rolled-true", "Rolled True");

  /** Raised whenever {@code false} was generated. */
  public static final PrefabEvent<Void> ROLLED_FALSE =
      PrefabEvent.signal("rolled-false", "Rolled False");

  /** Generates a new value. */
  public static final PrefabAction REGENERATE =
      PrefabAction.of(
          "regenerate",
          "Regenerate",
          context ->
              GENERATORS
                  .get(context.level(), context.instance().name())
                  .ifPresent(
                      random -> generate(context.creationContext(), context.instance(), random)));

  private static final List<PrefabProperty<?>> PROPERTIES = List.of(CHANCE, SEED);

  /** Creates the random-bool definition. */
  public RandomBoolPrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a bound view for one authored random bool.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public RandomBoolPrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES, level, name);
  }

  /**
   * Returns the current value of this instance.
   *
   * @return the value, or empty if the instance is not spawned
   */
  public Optional<Boolean> value() {
    return artifact(VALUE);
  }

  @Override
  protected List<PrefabPort> declaredPorts() {
    return List.of(VALUE, GENERATED, ROLLED_TRUE, ROLLED_FALSE, REGENERATE);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    String seed = value(instance, SEED).strip();
    Random random = seed.isEmpty() ? RANDOM : new Random(Long.parseLong(seed));
    GENERATORS.start(context.level(), instance.name(), random);
    generate(context, instance, random);
    return List.of();
  }

  @Override
  public void onDespawn(PrefabCreationContext context, PrefabInstance instance) {
    GENERATORS.end(context.level(), instance.name());
  }

  @Override
  public Optional<String> editorSummary(PrefabInstance instance) {
    return Optional.of(value(instance, CHANCE) + "% true");
  }

  private static void generate(
      PrefabCreationContext context, PrefabInstance instance, Random random) {
    boolean value = random.nextInt(100) < instance.get(CHANCE);
    publish(context, instance, VALUE, value);
    fire(context, instance, GENERATED, null);
    fire(context, instance, value ? ROLLED_TRUE : ROLLED_FALSE, null);
  }

  private static boolean isValidSeed(String value) {
    if (value.isBlank()) return true;
    try {
      Long.parseLong(value.strip());
      return true;
    } catch (NumberFormatException exception) {
      return false;
    }
  }
}
