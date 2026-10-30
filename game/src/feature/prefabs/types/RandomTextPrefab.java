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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import tools.jackson.databind.JsonNode;

/**
 * Server-side generator of a random text, e.g. the answer of a text keypad that changes every
 * session.
 *
 * <p>The text is either a sequence of random upper-case letters, or one entry picked from an
 * authored list. Without a seed, a new text is generated whenever the instance spawns. With a seed,
 * the texts follow the same sequence in every session.
 */
public final class RandomTextPrefab extends Prefab {

  private static final String TYPE = "random-text";
  private static final String DISPLAY_NAME = "Random Text";
  private static final String MODE_LETTERS = "Random Letters";
  private static final String MODE_LIST = "Pick From List";
  private static final Random RANDOM = new SecureRandom();
  private static final SpawnState<Random> GENERATORS = new SpawnState<>();

  /** How the text is generated. */
  public static final PrefabProperty<String> MODE =
      PrefabProperty.selection("mode", "Mode", MODE_LETTERS, List.of(MODE_LETTERS, MODE_LIST));

  /** Number of letters of a random-letters text. */
  public static final PrefabProperty<Integer> LENGTH =
      PrefabProperty.integer("length", "Letters", 4, 1, 16)
          .visibleWhen(values -> !isListMode(values));

  /** Comma-separated texts one is picked from. */
  public static final PrefabProperty<String> CHOICES =
      PrefabProperty.string(
              "choices",
              "Choices (comma-separated)",
              "Red, Green, Blue",
              value -> !choices(value).isEmpty())
          .visibleWhen(RandomTextPrefab::isListMode);

  /** Seed for texts that are the same in every session; empty for new texts every session. */
  public static final PrefabProperty<String> SEED =
      PrefabProperty.string("seed", "Seed (empty = random)", "", RandomTextPrefab::isValidSeed);

  /** The generated text. */
  public static final ArtifactOutput<String> TEXT =
      ArtifactOutput.of("text", "Text", ArtifactType.TEXT);

  /** Raised whenever a new text was generated, including when the instance spawns. */
  public static final PrefabEvent<Void> GENERATED = PrefabEvent.signal("generated", "Generated");

  /** Generates a new text. */
  public static final PrefabAction REGENERATE =
      PrefabAction.of(
          "regenerate",
          "Regenerate",
          context ->
              GENERATORS
                  .get(context.level(), context.instance().name())
                  .ifPresent(
                      random -> generate(context.creationContext(), context.instance(), random)));

  private static final List<PrefabProperty<?>> PROPERTIES = List.of(MODE, LENGTH, CHOICES, SEED);

  /** Creates the random-text definition. */
  public RandomTextPrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a bound view for one authored random text.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public RandomTextPrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES, level, name);
  }

  /**
   * Returns the current text of this instance.
   *
   * @return the text, or empty if the instance is not spawned
   */
  public Optional<String> text() {
    return artifact(TEXT);
  }

  @Override
  protected List<PrefabPort> declaredPorts() {
    return List.of(TEXT, GENERATED, REGENERATE);
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
    String seed = value(instance, SEED).strip();
    return Optional.of(
        seed.isEmpty()
            ? "random each session"
            : "text " + text(new Random(Long.parseLong(seed)), instance));
  }

  private static void generate(
      PrefabCreationContext context, PrefabInstance instance, Random random) {
    publish(context, instance, TEXT, text(random, instance));
    fire(context, instance, GENERATED, null);
  }

  private static String text(Random random, PrefabInstance instance) {
    if (instance.get(MODE).equals(MODE_LIST)) {
      List<String> choices = choices(instance.get(CHOICES));
      return choices.get(random.nextInt(choices.size()));
    }
    int length = instance.get(LENGTH);
    StringBuilder text = new StringBuilder(length);
    for (int index = 0; index < length; index++) text.append((char) ('A' + random.nextInt(26)));
    return text.toString();
  }

  private static List<String> choices(String value) {
    return Arrays.stream(value.split(",")).map(String::strip).filter(s -> !s.isEmpty()).toList();
  }

  private static boolean isListMode(Map<String, JsonNode> values) {
    JsonNode mode = values.get(MODE.key());
    return mode != null && MODE_LIST.equals(mode.asText());
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
