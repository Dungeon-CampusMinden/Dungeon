package wizard.runner.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;
import wizard.runner.model.ProjectDefinition.Asset;
import wizard.runner.model.ProjectDefinition.CollectionInput;
import wizard.runner.model.ProjectDefinition.GraphEdge;
import wizard.runner.model.ProjectDefinition.GraphNode;
import wizard.runner.model.ProjectDefinition.GraphNodeKind;
import wizard.runner.model.ProjectDefinition.Hint;
import wizard.runner.model.ProjectDefinition.InformationSource;
import wizard.runner.model.ProjectDefinition.Input;
import wizard.runner.model.ProjectDefinition.LimitMode;
import wizard.runner.model.ProjectDefinition.Metadata;
import wizard.runner.model.ProjectDefinition.NumericInput;
import wizard.runner.model.ProjectDefinition.PlayerCount;
import wizard.runner.model.ProjectDefinition.Resource;
import wizard.runner.model.ProjectDefinition.ResourceKind;
import wizard.runner.model.ProjectDefinition.Riddle;
import wizard.runner.model.ProjectDefinition.RiddleGraph;
import wizard.runner.model.ProjectDefinition.Scenario;
import wizard.runner.model.ProjectDefinition.Session;
import wizard.runner.model.ProjectDefinition.Surface;
import wizard.runner.model.ProjectDefinition.SurfaceKind;
import wizard.runner.model.ProjectDefinition.TimeLimit;

/** Maps a schema-valid DEER tree to the immutable active Foundation profile. */
public final class ProjectDefinitionMapper {
  /** Creates the stateless production mapper. */
  public ProjectDefinitionMapper() {}

  /**
   * Maps a schema-valid document without normalizing authored text.
   *
   * @param root schema-valid DEER root object
   * @return immutable project definition
   */
  public ProjectDefinition map(final JsonNode root) {
    JsonNode metadata = root.get("metadata");
    JsonNode session = root.get("session");
    JsonNode scenario = root.get("scenario");
    JsonNode graph = root.get("riddleGraph");
    return new ProjectDefinition(
        root.path("seed").longValue(),
        new Metadata(text(metadata, "id"), text(metadata, "title"), text(metadata, "locale")),
        new Session(
            new PlayerCount(
                session.path("playerCount").path("min").intValue(),
                session.path("playerCount").path("max").intValue()),
            new TimeLimit(
                session.path("time").path("limitMinutes").intValue(),
                enumValue(LimitMode.class, text(session.path("time"), "limitMode")))),
        new Scenario(
            text(scenario, "themeId"),
            text(scenario, "mission"),
            textArray(scenario.get("introText")),
            textArray(scenario.get("successText")),
            optionalTextArray(scenario, "failureText")),
        array(root.get("surfaces"), this::surface),
        new RiddleGraph(
            array(graph.get("nodes"), this::node), array(graph.get("edges"), this::edge)),
        array(root.get("riddles"), this::riddle),
        array(root.get("assets"), this::asset));
  }

  private Surface surface(final JsonNode node) {
    return new Surface(
        text(node, "id"), enumValue(SurfaceKind.class, text(node, "kind")), text(node, "title"));
  }

  private GraphNode node(final JsonNode node) {
    return new GraphNode(
        text(node, "id"),
        enumValue(GraphNodeKind.class, text(node, "kind")),
        optionalText(node, "riddleId"),
        optionalText(node, "surfaceId"));
  }

  private GraphEdge edge(final JsonNode node) {
    return new GraphEdge(text(node, "from"), text(node, "to"));
  }

  private Riddle riddle(final JsonNode node) {
    return new Riddle(
        text(node, "id"),
        text(node, "title"),
        array(node.get("informationSources"), this::informationSource),
        array(node.get("inputs"), this::input),
        array(node.get("hints"), this::hint));
  }

  private InformationSource informationSource(final JsonNode node) {
    return new InformationSource(
        text(node, "id"), text(node, "surfaceId"), array(node.get("resources"), this::resource));
  }

  private Input input(final JsonNode node) {
    return switch (text(node, "type")) {
      case "collection" -> new CollectionInput(text(node, "id"), text(node, "informationSourceId"));
      case "numeric" ->
          new NumericInput(
              text(node, "id"),
              text(node, "surfaceId"),
              text(node, "answer"),
              node.get("showDigitCount").booleanValue());
      default -> throw new IllegalArgumentException("unsupported schema-valid input type");
    };
  }

  private Resource resource(final JsonNode node) {
    return new Resource(
        text(node, "id"),
        enumValue(ResourceKind.class, text(node, "kind")),
        text(node, "title"),
        optionalText(node, "text"),
        optionalText(node, "assetId"));
  }

  private Hint hint(final JsonNode node) {
    return new Hint(text(node, "id"), text(node, "title"), text(node, "text"));
  }

  private Asset asset(final JsonNode node) {
    return new Asset(text(node, "id"), text(node, "path"), text(node, "mediaType"));
  }

  private static List<String> textArray(final JsonNode array) {
    return IntStream.range(0, array.size())
        .mapToObj(index -> array.get(index).textValue())
        .toList();
  }

  private static <T> List<T> array(final JsonNode array, final Function<JsonNode, T> mapper) {
    return IntStream.range(0, array.size())
        .mapToObj(index -> mapper.apply(array.get(index)))
        .toList();
  }

  private static String text(final JsonNode node, final String key) {
    return node.get(key).textValue();
  }

  private static Optional<String> optionalText(final JsonNode node, final String key) {
    JsonNode value = node.get(key);
    return value == null ? Optional.empty() : Optional.of(value.textValue());
  }

  private static Optional<List<String>> optionalTextArray(final JsonNode node, final String key) {
    JsonNode value = node.get(key);
    return value == null ? Optional.empty() : Optional.of(textArray(value));
  }

  private static <E extends Enum<E>> E enumValue(final Class<E> type, final String value) {
    return Enum.valueOf(type, value.toUpperCase(Locale.ROOT));
  }
}
