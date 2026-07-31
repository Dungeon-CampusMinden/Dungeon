package foundation.room.model;

import foundation.definition.ComposedRiddleDefinition;
import foundation.definition.DoorDefinition;
import foundation.definition.ExitDefinition;
import foundation.definition.NumericInputDefinition;
import foundation.definition.RiddleDefinition;
import foundation.definition.RoomDefinition;
import foundation.definition.RosterDefinition;
import foundation.definition.RosterSlotDefinition;
import foundation.definition.SectionDefinition;
import foundation.definition.TimerDefinition;
import foundation.presentation.GamePresentation;
import foundation.presentation.GamePresentation.ComposedPresentation;
import foundation.presentation.GamePresentation.ResourcePresentation;
import foundation.presentation.GamePresentation.RiddlePresentation;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/** Complete immutable room input shared by every participant running the same DEER project. */
public final class FoundationRoom {
  private final String id;
  private final String title;
  private final long seed;
  private final String inputSha256;
  private final int minimumPlayers;
  private final int maximumPlayers;
  private final List<SectionDefinition> sections;
  private final TimerDefinition timer;
  private final DoorDefinition door;
  private final ExitDefinition exit;
  private final GamePresentation presentation;
  private final RoomLayout layout;
  private final List<VerifiedAsset> assets;

  /**
   * Creates and validates one complete deterministic Foundation room.
   *
   * @param id stable room identifier
   * @param title player-facing room title
   * @param seed deterministic project seed
   * @param inputSha256 canonical complete project identity
   * @param minimumPlayers minimum ready players
   * @param maximumPlayers maximum admitted players
   * @param sections canonical Foundation sections
   * @param timer room timer definition
   * @param door common-door definition
   * @param exit common-exit definition
   * @param presentation complete room presentation
   * @param layout deterministic room layout
   * @param assets exact verified custom assets
   */
  public FoundationRoom(
      final String id,
      final String title,
      final long seed,
      final String inputSha256,
      final int minimumPlayers,
      final int maximumPlayers,
      final List<SectionDefinition> sections,
      final TimerDefinition timer,
      final DoorDefinition door,
      final ExitDefinition exit,
      final GamePresentation presentation,
      final RoomLayout layout,
      final List<VerifiedAsset> assets) {
    this.id = RoomModelChecks.requireId(id, "room id");
    this.title = RoomModelChecks.requireText(title, "room title");
    if (seed < 0) {
      throw new IllegalArgumentException("room seed must be nonnegative");
    }
    this.seed = seed;
    this.inputSha256 = RoomModelChecks.requireSha256(inputSha256, "room input SHA-256");
    if (minimumPlayers < 1 || maximumPlayers > 4 || minimumPlayers > maximumPlayers) {
      throw new IllegalArgumentException("player bounds must satisfy 1 <= min <= max <= 4");
    }
    this.minimumPlayers = minimumPlayers;
    this.maximumPlayers = maximumPlayers;
    this.sections = List.copyOf(Objects.requireNonNull(sections, "sections"));
    if (this.sections.isEmpty()) {
      throw new IllegalArgumentException("room must contain Foundation sections");
    }
    this.timer = Objects.requireNonNull(timer, "timer");
    this.door = Objects.requireNonNull(door, "door");
    this.exit = Objects.requireNonNull(exit, "exit");
    this.presentation = Objects.requireNonNull(presentation, "presentation");
    this.layout = Objects.requireNonNull(layout, "layout");
    this.assets =
        RoomModelChecks.copyUnique(assets, VerifiedAsset::logicalPath, "custom assets").stream()
            .sorted(Comparator.comparing(VerifiedAsset::logicalPath))
            .toList();
    validate();
  }

  /**
   * Returns the stable room identifier.
   *
   * @return room identifier
   */
  public String id() {
    return id;
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
   * Returns the minimum ready player count.
   *
   * @return minimum player count
   */
  public int minimumPlayers() {
    return minimumPlayers;
  }

  /**
   * Returns the maximum admitted player count.
   *
   * @return maximum player count
   */
  public int maximumPlayers() {
    return maximumPlayers;
  }

  /**
   * Returns the canonical Foundation sections.
   *
   * @return immutable authored section order
   */
  public List<SectionDefinition> sections() {
    return sections;
  }

  /**
   * Returns the room timer definition.
   *
   * @return timer definition
   */
  public TimerDefinition timer() {
    return timer;
  }

  /**
   * Returns the common-door definition.
   *
   * @return door definition
   */
  public DoorDefinition door() {
    return door;
  }

  /**
   * Returns the common-exit definition.
   *
   * @return exit definition
   */
  public ExitDefinition exit() {
    return exit;
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

  /**
   * Creates the reusable authority definition for the complete room capacity.
   *
   * @return complete room definition
   */
  public RoomDefinition createDefinition() {
    List<RosterSlotDefinition> slots =
        IntStream.rangeClosed(1, maximumPlayers)
            .mapToObj(number -> new RosterSlotDefinition("slot_" + number, number))
            .toList();
    return new RoomDefinition(
        id, minimumPlayers, new RosterDefinition(slots), sections, timer, door, exit);
  }

  private void validate() {
    List<RiddleDefinition> definitions =
        sections.stream().flatMap(section -> section.riddles().stream()).toList();
    List<RiddlePresentation> presentations = presentation.riddles();
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
        .map(ComposedPresentation.class::cast)
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
      final RiddleDefinition definition,
      final RiddlePresentation presentation,
      final RiddlePlacement placement) {
    if (!definition.id().equals(presentation.id())
        || !definition.id().equals(placement.riddleId())) {
      throw new IllegalArgumentException(
          "definition, presentation, and placement identities must match");
    }
    if (!definition.hints().isEmpty() != placement.hintPoint().isPresent()) {
      throw new IllegalArgumentException("hint placement must match the riddle definition");
    }
    ComposedRiddleDefinition composed = (ComposedRiddleDefinition) definition;
    if (!(presentation instanceof ComposedPresentation composedPresentation)) {
      throw new IllegalArgumentException("composed definition requires composed presentation");
    }
    List<String> sourceKeys =
        composedPresentation.informationSources().stream()
            .map(source -> componentKey(source.id(), source.surfaceId()))
            .toList();
    if (!composed.informationSources().stream()
            .map(source -> componentKey(source.id(), source.surfaceId()))
            .toList()
            .equals(sourceKeys)
        || !composed.informationSources().stream()
            .map(source -> source.resourceIds())
            .toList()
            .equals(
                composedPresentation.informationSources().stream()
                    .map(
                        source ->
                            source.resources().stream().map(ResourcePresentation::id).toList())
                    .toList())
        || !composed.inputs().stream()
            .filter(NumericInputDefinition.class::isInstance)
            .map(NumericInputDefinition.class::cast)
            .map(input -> componentKey(input.id(), input.surfaceId()))
            .toList()
            .equals(
                composedPresentation.inputs().stream()
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
                composed.informationSources().stream()
                    .map(source -> componentKey(source.id(), source.surfaceId())),
                composed.inputs().stream()
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
