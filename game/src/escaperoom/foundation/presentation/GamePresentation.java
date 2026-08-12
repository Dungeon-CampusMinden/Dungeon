package escaperoom.foundation.presentation;

import escaperoom.foundation.runtime.TerminalResult;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable player-facing content for one Foundation room.
 *
 * @param riddles ordered player-facing riddle presentations
 * @param introText ordered authored introduction pages
 * @param mission authored shared player objective
 * @param successText ordered authored common-exit success pages
 * @param failureText optional ordered authored hard-timeout pages
 */
public record GamePresentation(
    List<ComposedPresentation> riddles,
    List<String> introText,
    String mission,
    List<String> successText,
    Optional<List<String>> failureText) {

  /** Creates an ordered presentation with unique riddle and resource identifiers. */
  public GamePresentation {
    riddles = List.copyOf(Objects.requireNonNull(riddles, "riddles"));
    introText = requiredPages(introText, "presentation intro text");
    mission = required(mission, "presentation mission");
    successText = requiredPages(successText, "presentation success text");
    failureText = Objects.requireNonNull(failureText, "presentation failure text");
    failureText = failureText.map(value -> requiredPages(value, "presentation failure text"));
    if (riddles.isEmpty()) {
      throw new IllegalArgumentException("presentation must contain at least one riddle");
    }
    Map<String, String> identifiers = new LinkedHashMap<>();
    for (ComposedPresentation riddle : riddles) {
      register(identifiers, riddle.id(), "riddle");
      for (InformationSourcePresentation source : riddle.informationSources()) {
        register(identifiers, source.id(), "information source");
        for (ResourcePresentation resource : source.resources()) {
          register(identifiers, resource.id(), "information source resource");
        }
      }
      for (NumericInputPresentation input : riddle.inputs()) {
        register(identifiers, input.id(), "input");
      }
    }
  }

  /**
   * Returns the authored pages for one terminal result.
   *
   * @param terminal terminal authority result
   * @return authored success or hard-timeout pages
   */
  public List<String> terminalPages(final TerminalResult terminal) {
    return switch (Objects.requireNonNull(terminal, "terminal")) {
      case SUCCESS -> successText;
      case HARD_TIMEOUT ->
          failureText.orElseThrow(
              () -> new IllegalStateException("hard-timeout presentation text is missing"));
      case ABORTED ->
          throw new IllegalArgumentException("aborted sessions have no authored terminal text");
    };
  }

  /**
   * Player-facing presentation of one composed riddle.
   *
   * @param id stable riddle identifier
   * @param informationSources readable source presentations
   * @param inputs numeric input-device presentations
   */
  public record ComposedPresentation(
      String id,
      List<InformationSourcePresentation> informationSources,
      List<NumericInputPresentation> inputs) {
    /** Creates immutable composed presentation content. */
    public ComposedPresentation {
      id = required(id, "composed presentation id");
      informationSources =
          List.copyOf(Objects.requireNonNull(informationSources, "informationSources"));
      inputs = List.copyOf(Objects.requireNonNull(inputs, "inputs"));
    }
  }

  /**
   * Presentation and marker asset for one readable source.
   *
   * @param id stable source identifier
   * @param surfaceId authored container surface identifier
   * @param title player-facing container title
   * @param runtimeAssetPath marker asset path
   * @param resources ordered source contents
   */
  public record InformationSourcePresentation(
      String id,
      String surfaceId,
      String title,
      String runtimeAssetPath,
      List<ResourcePresentation> resources) {
    /** Creates immutable information-source content. */
    public InformationSourcePresentation {
      id = required(id, "information source presentation id");
      surfaceId = required(surfaceId, "information source presentation surface id");
      title = required(title, "information source presentation title");
      runtimeAssetPath = required(runtimeAssetPath, "information source runtime asset path");
      resources = List.copyOf(Objects.requireNonNull(resources, "resources"));
      if (resources.isEmpty()) {
        throw new IllegalArgumentException("information source requires resources");
      }
    }
  }

  /**
   * Presentation for one numeric keypad without its answer.
   *
   * @param id stable input identifier
   * @param surfaceId authored keypad surface identifier
   * @param title player-facing keypad title
   */
  public record NumericInputPresentation(String id, String surfaceId, String title) {
    /** Creates immutable numeric input presentation. */
    public NumericInputPresentation {
      id = required(id, "numeric input presentation id");
      surfaceId = required(surfaceId, "numeric input presentation surface id");
      title = required(title, "numeric input presentation title");
    }
  }

  /**
   * Player-facing content for one information-source resource.
   *
   * @param id stable resource identifier
   * @param title player-facing clue title
   * @param text player-facing clue text
   * @param runtimeAssetPath optional runtime asset used by the clue dialog
   */
  public record ResourcePresentation(
      String id, String title, String text, Optional<String> runtimeAssetPath) {

    /** Creates immutable resource clue content. */
    public ResourcePresentation {
      id = required(id, "resource presentation id");
      title = required(title, "resource presentation title");
      text = required(text, "resource presentation text");
      runtimeAssetPath = Objects.requireNonNull(runtimeAssetPath, "resource runtime asset path");
      runtimeAssetPath =
          runtimeAssetPath.map(path -> required(path, "resource runtime asset path"));
    }
  }

  private static String required(final String value, final String label) {
    Objects.requireNonNull(value, label);
    if (value.isBlank()) {
      throw new IllegalArgumentException(label + " must not be blank");
    }
    return value;
  }

  private static List<String> requiredPages(final List<String> pages, final String label) {
    List<String> copy =
        List.copyOf(Objects.requireNonNull(pages, label)).stream()
            .map(page -> required(page, label))
            .toList();
    if (copy.isEmpty()) {
      throw new IllegalArgumentException(label + " must contain at least one page");
    }
    return copy;
  }

  private static void register(
      final Map<String, String> identifiers, final String id, final String owner) {
    String previous = identifiers.putIfAbsent(id, owner);
    if (previous != null) {
      throw new IllegalArgumentException(
          "presentation id '" + id + "' is shared by " + previous + " and " + owner);
    }
  }
}
