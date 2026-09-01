package wizard.runner.model;

import escaperoom.foundation.definition.HintSeverity;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable active-profile DEER definition consumed by Foundation runtime planning.
 *
 * @param seed stable deterministic layout seed from 0 through 9007199254740991
 * @param metadata project metadata
 * @param session session contract
 * @param scenario scenario text
 * @param surfaces interaction surfaces
 * @param riddleGraph progression graph
 * @param riddles active-profile riddles
 * @param assets declared assets
 */
public record ProjectDefinition(
    long seed,
    Metadata metadata,
    Session session,
    Scenario scenario,
    List<Surface> surfaces,
    RiddleGraph riddleGraph,
    List<Riddle> riddles,
    List<Asset> assets) {
  /** Creates a deeply immutable project definition. */
  public ProjectDefinition {
    if (seed < 0 || seed > 9_007_199_254_740_991L) {
      throw new IllegalArgumentException("seed must be a nonnegative IEEE-754 safe integer");
    }
    Objects.requireNonNull(metadata, "metadata");
    Objects.requireNonNull(session, "session");
    Objects.requireNonNull(scenario, "scenario");
    surfaces = List.copyOf(Objects.requireNonNull(surfaces, "surfaces"));
    Objects.requireNonNull(riddleGraph, "riddleGraph");
    riddles = List.copyOf(Objects.requireNonNull(riddles, "riddles"));
    assets = List.copyOf(Objects.requireNonNull(assets, "assets"));
  }

  /**
   * Immutable project metadata.
   *
   * @param id stable room identifier
   * @param title player-facing title
   * @param locale content locale
   * @param operatorEmail optional operator email for tracking recovery notices
   */
  public record Metadata(String id, String title, String locale, Optional<String> operatorEmail) {
    /** Creates project metadata. */
    public Metadata {
      id = requireText(id, "metadata.id");
      title = requireText(title, "metadata.title");
      locale = requireText(locale, "metadata.locale");
      operatorEmail = Objects.requireNonNull(operatorEmail, "metadata.operatorEmail");
    }
  }

  /**
   * Immutable session contract.
   *
   * @param playerCount supported player-count range
   * @param time session time limit
   */
  public record Session(PlayerCount playerCount, TimeLimit time) {
    /** Creates a session contract. */
    public Session {
      Objects.requireNonNull(playerCount, "session.playerCount");
      Objects.requireNonNull(time, "session.time");
    }
  }

  /**
   * Inclusive supported player-count range.
   *
   * @param min minimum players
   * @param max maximum players
   */
  public record PlayerCount(int min, int max) {}

  /**
   * Session time limit.
   *
   * @param limitMinutes authored limit in minutes
   * @param mode timeout behavior
   */
  public record TimeLimit(int limitMinutes, LimitMode mode) {
    /** Creates a time limit. */
    public TimeLimit {
      Objects.requireNonNull(mode, "mode");
    }
  }

  /** Supported time-limit behavior. */
  public enum LimitMode {
    /** Timeout terminates the session. */
    HARD,
    /** Timeout records overtime without terminating the session. */
    SOFT
  }

  /**
   * Immutable scenario text.
   *
   * @param themeId supported theme identifier
   * @param mission scenario mission
   * @param introText ordered introduction pages
   * @param successText ordered common-exit success pages
   * @param failureText optional ordered hard-timeout pages
   */
  public record Scenario(
      String themeId,
      String mission,
      List<String> introText,
      List<String> successText,
      Optional<List<String>> failureText) {
    /** Creates scenario text. */
    public Scenario {
      themeId = requireText(themeId, "scenario.themeId");
      mission = requireText(mission, "scenario.mission");
      introText = requirePages(introText, "scenario.introText");
      successText = requirePages(successText, "scenario.successText");
      failureText = Objects.requireNonNull(failureText, "scenario.failureText");
      failureText = failureText.map(pages -> requirePages(pages, "scenario.failureText"));
    }
  }

  /**
   * One interaction surface.
   *
   * @param id stable surface identifier
   * @param kind surface kind
   * @param title authored title
   */
  public record Surface(String id, SurfaceKind kind, String title) {
    /** Creates a surface. */
    public Surface {
      id = requireText(id, "surface.id");
      Objects.requireNonNull(kind, "surface.kind");
      title = requireText(title, "surface.title");
    }
  }

  /** Supported Foundation surface kinds. */
  public enum SurfaceKind {
    /** Shared level world. */
    WORLD,
    /** Collectible container. */
    CONTAINER,
    /** Numeric input keypad. */
    KEYPAD,
    /** Common exit door. */
    DOOR
  }

  /**
   * Immutable graph topology.
   *
   * @param nodes graph nodes
   * @param edges unconditional edges
   */
  public record RiddleGraph(List<GraphNode> nodes, List<GraphEdge> edges) {
    /** Creates a graph. */
    public RiddleGraph {
      nodes = List.copyOf(Objects.requireNonNull(nodes, "nodes"));
      edges = List.copyOf(Objects.requireNonNull(edges, "edges"));
    }
  }

  /**
   * One graph node with the reference required by its kind.
   *
   * @param id stable node identifier
   * @param kind node kind
   * @param riddleId riddle reference when applicable
   * @param surfaceId exit surface reference when applicable
   */
  public record GraphNode(
      String id, GraphNodeKind kind, Optional<String> riddleId, Optional<String> surfaceId) {
    /** Creates a graph node. */
    public GraphNode {
      id = requireText(id, "node.id");
      Objects.requireNonNull(kind, "node.kind");
      riddleId = Objects.requireNonNull(riddleId, "node.riddleId");
      surfaceId = Objects.requireNonNull(surfaceId, "node.surfaceId");
    }
  }

  /** Supported graph-node kinds. */
  public enum GraphNodeKind {
    /** Unique graph entry. */
    START,
    /** Node bound to one riddle. */
    RIDDLE,
    /** Unique common exit. */
    END
  }

  /**
   * One unconditional progression edge.
   *
   * @param from source node identifier
   * @param to target node identifier
   */
  public record GraphEdge(String from, String to) {
    /** Creates an edge. */
    public GraphEdge {
      from = requireText(from, "edge.from");
      to = requireText(to, "edge.to");
    }
  }

  /**
   * One active-profile riddle.
   *
   * @param id stable riddle identifier
   * @param title authored title
   * @param informationSources optional readable information sources
   * @param inputs mandatory AND-composed inputs
   * @param hints ordered hints
   */
  public record Riddle(
      String id,
      String title,
      List<InformationSource> informationSources,
      List<Input> inputs,
      List<Hint> hints) {
    /** Creates a riddle. */
    public Riddle {
      id = requireText(id, "riddle.id");
      title = requireText(title, "riddle.title");
      informationSources =
          List.copyOf(Objects.requireNonNull(informationSources, "informationSources"));
      inputs = List.copyOf(Objects.requireNonNull(inputs, "inputs"));
      if (inputs.isEmpty()) {
        throw new IllegalArgumentException("riddle.inputs must not be empty");
      }
      hints = List.copyOf(Objects.requireNonNull(hints, "hints"));
    }
  }

  /**
   * One readable information source.
   *
   * @param id stable source identifier
   * @param surfaceId container surface identifier
   * @param resources ordered nonempty source contents
   */
  public record InformationSource(String id, String surfaceId, List<Resource> resources) {
    /** Creates an information source. */
    public InformationSource {
      id = requireText(id, "informationSource.id");
      surfaceId = requireText(surfaceId, "informationSource.surfaceId");
      resources = List.copyOf(Objects.requireNonNull(resources, "informationSource.resources"));
      if (resources.isEmpty()) {
        throw new IllegalArgumentException("informationSource.resources must not be empty");
      }
    }
  }

  /** Closed active-profile input variants. */
  public sealed interface Input permits CollectionInput, NumericInput {
    /**
     * Returns the stable input identifier.
     *
     * @return stable identifier
     */
    String id();
  }

  /**
   * Required interaction with one information source.
   *
   * @param id stable input identifier
   * @param informationSourceId referenced source identifier
   */
  public record CollectionInput(String id, String informationSourceId) implements Input {
    /** Creates a collection input. */
    public CollectionInput {
      id = requireText(id, "input.id");
      informationSourceId = requireText(informationSourceId, "input.informationSourceId");
    }
  }

  /**
   * One numeric keypad input.
   *
   * @param id stable input identifier
   * @param surfaceId keypad surface identifier
   * @param answer exact numeric answer
   * @param showDigitCount whether the digit count is shown
   */
  public record NumericInput(String id, String surfaceId, String answer, boolean showDigitCount)
      implements Input {
    /** Creates a numeric input. */
    public NumericInput {
      id = requireText(id, "input.id");
      surfaceId = requireText(surfaceId, "input.surfaceId");
      answer = requireText(answer, "input.answer");
    }
  }

  /**
   * One riddle-owned resource.
   *
   * @param id stable resource identifier
   * @param kind resource variant
   * @param title authored title
   * @param text inline text when applicable
   * @param assetId asset reference when applicable
   */
  public record Resource(
      String id, ResourceKind kind, String title, Optional<String> text, Optional<String> assetId) {
    /** Creates a resource. */
    public Resource {
      id = requireText(id, "resource.id");
      Objects.requireNonNull(kind, "resource.kind");
      title = requireText(title, "resource.title");
      text = Objects.requireNonNull(text, "resource.text");
      assetId = Objects.requireNonNull(assetId, "resource.assetId");
    }
  }

  /** Supported resource variants. */
  public enum ResourceKind {
    /** Inline immutable text. */
    INLINE_TEXT,
    /** Reference to a declared image asset. */
    ASSET
  }

  /**
   * One ordered optional hint.
   *
   * @param id stable hint identifier
   * @param title authored title
   * @param text authored hint text
   * @param severity disclosure category announced before release
   */
  public record Hint(String id, String title, String text, HintSeverity severity) {
    /** Creates a hint. */
    public Hint {
      id = requireText(id, "hint.id");
      title = requireText(title, "hint.title");
      text = requireText(text, "hint.text");
      Objects.requireNonNull(severity, "hint.severity");
    }
  }

  /**
   * One declared image asset.
   *
   * @param id stable asset identifier
   * @param path portable authoring path
   * @param mediaType declared image media type, verified for custom assets and retained as bundled
   *     authoring metadata
   */
  public record Asset(String id, String path, String mediaType) {
    /** Creates an asset declaration. */
    public Asset {
      id = requireText(id, "asset.id");
      path = requireText(path, "asset.path");
      mediaType = requireText(mediaType, "asset.mediaType");
    }
  }

  private static List<String> copyTexts(final List<String> source, final String label) {
    Objects.requireNonNull(source, label);
    return source.stream().map(value -> requireText(value, label)).toList();
  }

  private static List<String> requirePages(final List<String> source, final String label) {
    List<String> pages = copyTexts(source, label);
    if (pages.isEmpty()) {
      throw new IllegalArgumentException(label + " must contain at least one page");
    }
    return pages;
  }

  private static String requireText(final String value, final String label) {
    Objects.requireNonNull(value, label);
    if (value.isBlank()) {
      throw new IllegalArgumentException(label + " must not be blank");
    }
    return value;
  }
}
