package escaperoom.foundation.room.model;

import escaperoom.foundation.definition.ComposedRiddleDefinition;
import escaperoom.foundation.definition.NumericInputDefinition;
import escaperoom.foundation.definition.RoomDefinition;
import escaperoom.foundation.definition.TimerMode;
import escaperoom.foundation.presentation.GamePresentation;
import escaperoom.foundation.presentation.GamePresentation.ComposedPresentation;
import escaperoom.foundation.presentation.GamePresentation.ResourcePresentation;
import feature.entities.CharacterClass;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Complete immutable room input shared by every participant running the same DEER project. */
public final class FoundationRoom {
  private final String title;
  private final long seed;
  private final String inputSha256;
  private final List<CharacterClass> playableCharacterClasses;
  private final RoomDefinition definition;
  private final GamePresentation presentation;
  private final RoomLayout layout;
  private final List<VerifiedAsset> assets;

  /**
   * Creates and validates one complete deterministic Foundation room.
   *
   * @param title player-facing room title
   * @param seed deterministic project seed
   * @param inputSha256 canonical complete project identity
   * @param playableCharacterClasses ordered pool of server-assigned player classes
   * @param definition complete validated authority definition
   * @param presentation complete room presentation
   * @param layout deterministic room layout
   * @param assets exact verified custom assets
   */
  public FoundationRoom(
      final String title,
      final long seed,
      final String inputSha256,
      final List<CharacterClass> playableCharacterClasses,
      final RoomDefinition definition,
      final GamePresentation presentation,
      final RoomLayout layout,
      final List<VerifiedAsset> assets) {
    this.title = RoomModelChecks.requireText(title, "room title");
    if (seed < 0) {
      throw new IllegalArgumentException("room seed must be nonnegative");
    }
    this.seed = seed;
    this.inputSha256 = RoomModelChecks.requireSha256(inputSha256, "room input SHA-256");
    List<CharacterClass> characterClasses =
        Objects.requireNonNull(playableCharacterClasses, "playableCharacterClasses");
    if (characterClasses.isEmpty()) {
      throw new IllegalArgumentException("playable character classes must not be empty");
    }
    if (characterClasses.stream().anyMatch(Objects::isNull)) {
      throw new IllegalArgumentException("playable character classes must not contain null");
    }
    if (new HashSet<>(characterClasses).size() != characterClasses.size()) {
      throw new IllegalArgumentException("playable character classes must be unique");
    }
    this.playableCharacterClasses = List.copyOf(characterClasses);
    this.definition = Objects.requireNonNull(definition, "definition");
    this.presentation = Objects.requireNonNull(presentation, "presentation");
    this.layout = Objects.requireNonNull(layout, "layout");
    this.assets =
        RoomModelChecks.copyUnique(assets, VerifiedAsset::logicalPath, "custom assets").stream()
            .sorted(Comparator.comparing(VerifiedAsset::logicalPath))
            .toList();
    validate();
  }

  /**
   * Returns the player-facing room title.
   *
   * @return room title
   */
  public String title() {
    return title;
  }

  /**
   * Returns the deterministic project seed.
   *
   * @return nonnegative seed
   */
  public long seed() {
    return seed;
  }

  /**
   * Returns the canonical complete DEER-project identity.
   *
   * @return lowercase SHA-256
   */
  public String inputSha256() {
    return inputSha256;
  }

  /**
   * Returns the ordered pool of server-assigned player classes.
   *
   * @return immutable playable character class order
   */
  public List<CharacterClass> playableCharacterClasses() {
    return playableCharacterClasses;
  }

  /**
   * Returns the complete validated authority definition.
   *
   * @return room definition
   */
  public RoomDefinition definition() {
    return definition;
  }

  /**
   * Returns the complete room presentation.
   *
   * @return presentation
   */
  public GamePresentation presentation() {
    return presentation;
  }

  /**
   * Returns the deterministic in-memory layout.
   *
   * @return immutable layout
   */
  public RoomLayout layout() {
    return layout;
  }

  /**
   * Returns the exact verified custom assets.
   *
   * @return immutable asset snapshots
   */
  public List<VerifiedAsset> assets() {
    return assets;
  }

  private void validate() {
    if (definition.timer().mode() == TimerMode.HARD && presentation.failureText().isEmpty()) {
      throw new IllegalArgumentException("hard timer requires presentation failure text");
    }
    List<ComposedRiddleDefinition> definitions =
        definition.progression().riddleNodes().stream().map(node -> node.riddle()).toList();
    List<ComposedPresentation> presentations = presentation.riddles();
    List<RiddlePlacement> placements = layout.riddlePlacements();
    if (definitions.size() != presentations.size() || definitions.size() != placements.size()) {
      throw new IllegalArgumentException(
          "definition, presentation, and placement counts must match");
    }
    for (int index = 0; index < definitions.size(); index++) {
      validateRiddle(definitions.get(index), presentations.get(index), placements.get(index));
    }
    validateAssets();
  }

  private void validateAssets() {
    Map<String, VerifiedAsset> available =
        assets.stream().collect(Collectors.toMap(VerifiedAsset::logicalPath, asset -> asset));
    presentation.riddles().stream()
        .flatMap(composed -> composed.informationSources().stream())
        .flatMap(source -> source.resources().stream())
        .map(ResourcePresentation::runtimeAssetPath)
        .flatMap(Optional::stream)
        .filter(path -> path.startsWith("assets/custom/"))
        .forEach(
            path -> {
              if (!available.containsKey(path)) {
                throw new IllegalArgumentException(
                    "information source references an unverified custom asset: " + path);
              }
            });
  }

  private static void validateRiddle(
      final ComposedRiddleDefinition definition,
      final ComposedPresentation presentation,
      final RiddlePlacement placement) {
    if (!definition.id().equals(presentation.id())
        || !definition.id().equals(placement.riddleId())) {
      throw new IllegalArgumentException(
          "definition, presentation, and placement identities must match");
    }
    if (!definition.hints().isEmpty() != placement.hintPoint().isPresent()) {
      throw new IllegalArgumentException("hint placement must match the riddle definition");
    }
    List<String> sourceKeys =
        presentation.informationSources().stream()
            .map(source -> componentKey(source.id(), source.surfaceId()))
            .toList();
    if (!definition.informationSources().stream()
            .map(source -> componentKey(source.id(), source.surfaceId()))
            .toList()
            .equals(sourceKeys)
        || !definition.informationSources().stream()
            .map(source -> source.resourceIds())
            .toList()
            .equals(
                presentation.informationSources().stream()
                    .map(
                        source ->
                            source.resources().stream().map(ResourcePresentation::id).toList())
                    .toList())
        || !definition.inputs().stream()
            .filter(NumericInputDefinition.class::isInstance)
            .map(NumericInputDefinition.class::cast)
            .map(input -> componentKey(input.id(), input.surfaceId()))
            .toList()
            .equals(
                presentation.inputs().stream()
                    .map(input -> componentKey(input.id(), input.surfaceId()))
                    .toList())) {
      throw new IllegalArgumentException(
          "composed presentation components must match the room definition");
    }
    List<String> placedComponents =
        placement.components().stream()
            .map(component -> componentKey(component.componentId(), component.surfaceId()))
            .toList();
    List<String> expectedComponents =
        Stream.concat(
                definition.informationSources().stream()
                    .map(source -> componentKey(source.id(), source.surfaceId())),
                definition.inputs().stream()
                    .filter(NumericInputDefinition.class::isInstance)
                    .map(NumericInputDefinition.class::cast)
                    .map(input -> componentKey(input.id(), input.surfaceId())))
            .toList();
    if (!expectedComponents.equals(placedComponents)) {
      throw new IllegalArgumentException(
          "surface-bound riddle components must match their placements");
    }
  }

  private static String componentKey(final String componentId, final String surfaceId) {
    return componentId + "\u0000" + surfaceId;
  }
}
