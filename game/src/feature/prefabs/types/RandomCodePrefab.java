package feature.prefabs.types;

import engine.Entity;
import engine.level.elements.ILevel;
import engine.utils.Point;
import feature.prefabs.ArtifactOutput;
import feature.prefabs.ArtifactType;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabAction;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.PrefabEvent;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabPort;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabRuntime;
import feature.prefabs.PrefabSide;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Server-side generator of a random digit code, e.g. for a keypad whose solution changes every
 * session. The code is an artifact that can be linked to keypads and shown in dialogs.
 *
 * <p>Without a seed, a new code is generated whenever the instance spawns. With a seed, the code is
 * the same in every session, and each {@link #REGENERATE} continues a fixed sequence.
 */
public final class RandomCodePrefab extends Prefab {

  private static final String TYPE = "random-code";
  private static final String DISPLAY_NAME = "Random Code";
  private static final Random RANDOM = new SecureRandom();

  /** Editor-only marker position, used to select the instance and to draw its links. */
  public static final PrefabProperty<Point> POSITION =
      PrefabProperty.point("position", "Marker Position", new Point(0, 0), new Point(0.5f, 0.5f));

  /** Number of digits of the code. */
  public static final PrefabProperty<Integer> LENGTH =
      PrefabProperty.integer("length", "Digits", 4, 1, 8);

  /** Seed for a code that is the same in every session; empty for a new code every session. */
  public static final PrefabProperty<String> SEED =
      PrefabProperty.string("seed", "Seed (empty = random)", "", RandomCodePrefab::isValidSeed);

  /** The generated code, with leading zeros. */
  public static final ArtifactOutput<String> CODE =
      ArtifactOutput.of("code", "Code", ArtifactType.TEXT);

  /** Raised whenever a new code was generated, including when the instance spawns. */
  public static final PrefabEvent<Void> GENERATED = PrefabEvent.signal("generated", "Generated");

  /** Generates a new code. */
  public static final PrefabAction REGENERATE =
      PrefabAction.of(
          "regenerate",
          "Regenerate",
          context ->
              generate(
                  context.creationContext(),
                  context.instance(),
                  PrefabRuntime.artifact(context.level(), context.instance().name(), CODE)));

  private static final List<PrefabProperty<?>> PROPERTIES = List.of(POSITION, LENGTH, SEED);

  /** Creates the random-code definition. */
  public RandomCodePrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a bound view for one authored random code.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public RandomCodePrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES, level, name);
  }

  /**
   * Returns the current code of this instance.
   *
   * @return the code, or empty if the instance is not spawned
   */
  public Optional<String> code() {
    return artifact(CODE);
  }

  @Override
  protected List<PrefabPort> declaredPorts() {
    return List.of(CODE, GENERATED, REGENERATE);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    generate(context, instance, Optional.empty());
    return List.of();
  }

  @Override
  public void renderEditorFeedback(
      ILevel level, PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {
    Point offset = POSITION.editorFeedbackOffset();
    Point marker = value(instance, POSITION).translate(offset.x(), offset.y());
    String seed = value(instance, SEED).strip();
    String preview =
        seed.isEmpty()
            ? "random each session"
            : "code " + code(new Random(Long.parseLong(seed)), value(instance, LENGTH));
    feedback.point(marker, instance.name());
    if (selected) feedback.label(marker.translate(0f, -0.45f), "(" + preview + ")");
  }

  /**
   * Publishes a new code.
   *
   * @param context creation context of the instance
   * @param instance the instance
   * @param previous previous code, used to continue the sequence of a seeded instance
   */
  private static void generate(
      PrefabCreationContext context, PrefabInstance instance, Optional<String> previous) {
    String seed = instance.get(SEED).strip();
    Random random;
    if (seed.isEmpty()) random = RANDOM;
    else if (previous.isEmpty()) random = new Random(Long.parseLong(seed));
    else random = new Random(Long.parseLong(seed) * 31 + previous.get().hashCode());
    publish(context, instance, CODE, code(random, instance.get(LENGTH)));
    fire(context, instance, GENERATED, null);
  }

  private static String code(Random random, int length) {
    StringBuilder code = new StringBuilder(length);
    for (int index = 0; index < length; index++) code.append(random.nextInt(10));
    return code.toString();
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
