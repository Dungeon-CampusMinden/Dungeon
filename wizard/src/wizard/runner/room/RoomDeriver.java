package wizard.runner.room;

import escaperoom.foundation.definition.CollectionInputDefinition;
import escaperoom.foundation.definition.ComposedRiddleDefinition;
import escaperoom.foundation.definition.DoorDefinition;
import escaperoom.foundation.definition.ExitDefinition;
import escaperoom.foundation.definition.HintDefinition;
import escaperoom.foundation.definition.InformationSourceDefinition;
import escaperoom.foundation.definition.InputDefinition;
import escaperoom.foundation.definition.NumericInputDefinition;
import escaperoom.foundation.definition.ProgressionDefinition;
import escaperoom.foundation.definition.ProgressionDefinition.Edge;
import escaperoom.foundation.definition.ProgressionDefinition.RiddleNode;
import escaperoom.foundation.definition.RoomDefinition;
import escaperoom.foundation.definition.RosterDefinition;
import escaperoom.foundation.definition.RosterSlotDefinition;
import escaperoom.foundation.definition.TimerDefinition;
import escaperoom.foundation.definition.TimerMode;
import escaperoom.foundation.presentation.GamePresentation;
import escaperoom.foundation.presentation.GamePresentation.ComposedPresentation;
import escaperoom.foundation.presentation.GamePresentation.InformationSourcePresentation;
import escaperoom.foundation.presentation.GamePresentation.NumericInputPresentation;
import escaperoom.foundation.presentation.GamePresentation.ResourcePresentation;
import escaperoom.foundation.room.model.FoundationRoom;
import escaperoom.foundation.room.model.RoomLayout;
import escaperoom.foundation.room.model.VerifiedAsset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import wizard.runner.model.ProjectDefinition;
import wizard.runner.model.ProjectDefinition.Asset;
import wizard.runner.model.ProjectDefinition.CollectionInput;
import wizard.runner.model.ProjectDefinition.GraphNode;
import wizard.runner.model.ProjectDefinition.GraphNodeKind;
import wizard.runner.model.ProjectDefinition.LimitMode;
import wizard.runner.model.ProjectDefinition.NumericInput;
import wizard.runner.model.ProjectDefinition.Resource;
import wizard.runner.model.ProjectDefinition.Riddle;
import wizard.runner.model.ProjectDefinition.Surface;
import wizard.runner.validation.ValidationResult;

/** One-way mapper from validated Wizard project input to an in-memory Foundation room. */
public final class RoomDeriver {
  private static final String FUND_ASSET = "objects/treasurechest/treasurechest.png";

  /**
   * Derives one complete room shared by host and clients without filesystem output.
   *
   * @param validation valid closed production validation result
   * @return immutable deterministic Foundation room
   */
  public FoundationRoom derive(final ValidationResult validation) {
    Objects.requireNonNull(validation, "validation");
    if (!validation.valid()) {
      throw new IllegalArgumentException("Foundation room derivation requires valid project input");
    }
    ProjectDefinition project =
        validation
            .model()
            .orElseThrow(() -> new IllegalArgumentException("validated project model is missing"));
    String hostInputSha256 =
        validation
            .hostInputSha256()
            .orElseThrow(() -> new IllegalArgumentException("validated host hash is missing"));
    SingleRoomPlanner.Plan plan = SingleRoomPlanner.planRoom(project);
    return derive(project, hostInputSha256, plan, validation.assets());
  }

  private static FoundationRoom derive(
      final ProjectDefinition project,
      final String hostInputSha256,
      final SingleRoomPlanner.Plan plan,
      final List<VerifiedAsset> verifiedAssets) {
    Map<String, Riddle> riddles = index(project.riddles(), Riddle::id);
    Map<String, Asset> assets = index(project.assets(), Asset::id);
    Map<String, Surface> surfaces = index(project.surfaces(), Surface::id);
    ProgressionDefinition progression = progression(project, plan, riddles);
    TimerDefinition timer = timer(project);
    String doorId = endSurfaceId(project);
    DoorDefinition door = new DoorDefinition(doorId);
    ExitDefinition exit = new ExitDefinition(nodeId(project, GraphNodeKind.END), doorId);
    List<RosterSlotDefinition> slots =
        IntStream.rangeClosed(1, project.session().playerCount().max())
            .mapToObj(number -> new RosterSlotDefinition("slot_" + number, number))
            .toList();
    RoomDefinition definition =
        new RoomDefinition(
            project.metadata().id(),
            project.session().playerCount().min(),
            new RosterDefinition(slots),
            progression,
            timer,
            door,
            exit);
    GamePresentation presentation = presentation(project, plan, riddles, assets, surfaces);
    RoomLayout layout = plan.layout();
    return new FoundationRoom(
        project.metadata().title(),
        project.seed(),
        hostInputSha256,
        WizardThemeCatalog.playableCharacterClasses(project.scenario().themeId()),
        definition,
        presentation,
        layout,
        verifiedAssets);
  }

  private static ProgressionDefinition progression(
      final ProjectDefinition project,
      final SingleRoomPlanner.Plan plan,
      final Map<String, Riddle> riddles) {
    Map<String, GraphNode> nodesByRiddle =
        project.riddleGraph().nodes().stream()
            .filter(node -> node.kind() == GraphNodeKind.RIDDLE)
            .collect(Collectors.toMap(node -> node.riddleId().orElseThrow(), Function.identity()));
    List<RiddleNode> nodes =
        plan.riddleIds().stream()
            .map(
                riddleId ->
                    new RiddleNode(
                        nodesByRiddle.get(riddleId).id(),
                        definition(requireRiddle(riddles, riddleId))))
            .toList();
    List<Edge> edges =
        project.riddleGraph().edges().stream()
            .map(edge -> new Edge(edge.from(), edge.to()))
            .sorted(Comparator.comparing(Edge::from).thenComparing(Edge::to))
            .toList();
    return new ProgressionDefinition(
        nodeId(project, GraphNodeKind.START), nodeId(project, GraphNodeKind.END), nodes, edges);
  }

  private static ComposedRiddleDefinition definition(final Riddle riddle) {
    List<HintDefinition> hints =
        riddle.hints().stream()
            .map(hint -> new HintDefinition(hint.id(), hint.title(), hint.text(), hint.severity()))
            .toList();
    List<InformationSourceDefinition> sources =
        riddle.informationSources().stream()
            .map(
                source ->
                    new InformationSourceDefinition(
                        source.id(),
                        source.surfaceId(),
                        source.resources().stream().map(Resource::id).toList()))
            .toList();
    List<InputDefinition> inputs =
        riddle.inputs().stream()
            .map(
                input ->
                    input instanceof CollectionInput collection
                        ? (InputDefinition)
                            new CollectionInputDefinition(
                                collection.id(), collection.informationSourceId())
                        : (InputDefinition) numericDefinition((NumericInput) input))
            .toList();
    return new ComposedRiddleDefinition(riddle.id(), sources, inputs, hints);
  }

  private static NumericInputDefinition numericDefinition(final NumericInput input) {
    return new NumericInputDefinition(
        input.id(), input.surfaceId(), input.answer(), input.showDigitCount());
  }

  private static TimerDefinition timer(final ProjectDefinition project) {
    return new TimerDefinition(
        project.session().time().limitMinutes(),
        project.session().time().mode() == LimitMode.HARD ? TimerMode.HARD : TimerMode.SOFT);
  }

  private static String endSurfaceId(final ProjectDefinition project) {
    return project.riddleGraph().nodes().stream()
        .filter(node -> node.kind() == GraphNodeKind.END)
        .findFirst()
        .flatMap(GraphNode::surfaceId)
        .orElseThrow(() -> new IllegalArgumentException("validated project has no exit surface"));
  }

  private static String nodeId(final ProjectDefinition project, final GraphNodeKind kind) {
    return project.riddleGraph().nodes().stream()
        .filter(node -> node.kind() == kind)
        .map(GraphNode::id)
        .findFirst()
        .orElseThrow();
  }

  private static GamePresentation presentation(
      final ProjectDefinition project,
      final SingleRoomPlanner.Plan plan,
      final Map<String, Riddle> riddles,
      final Map<String, Asset> assets,
      final Map<String, Surface> surfaces) {
    List<ComposedPresentation> presentations =
        plan.riddleIds().stream()
            .map(riddleId -> presentation(requireRiddle(riddles, riddleId), assets, surfaces))
            .toList();
    return new GamePresentation(
        presentations,
        project.scenario().introText(),
        project.scenario().mission(),
        project.scenario().successText(),
        project.scenario().failureText());
  }

  private static ComposedPresentation presentation(
      final Riddle riddle, final Map<String, Asset> assets, final Map<String, Surface> surfaces) {
    List<InformationSourcePresentation> sources =
        riddle.informationSources().stream()
            .map(
                source ->
                    new InformationSourcePresentation(
                        source.id(),
                        source.surfaceId(),
                        requireSurface(surfaces, source.surfaceId()).title(),
                        FUND_ASSET,
                        source.resources().stream()
                            .map(resource -> resourcePresentation(resource, assets))
                            .toList()))
            .toList();
    List<NumericInputPresentation> inputs =
        riddle.inputs().stream()
            .filter(NumericInput.class::isInstance)
            .map(NumericInput.class::cast)
            .map(
                input ->
                    new NumericInputPresentation(
                        input.id(),
                        input.surfaceId(),
                        requireSurface(surfaces, input.surfaceId()).title()))
            .toList();
    return new ComposedPresentation(riddle.id(), sources, inputs);
  }

  private static ResourcePresentation resourcePresentation(
      final Resource resource, final Map<String, Asset> assets) {
    String text = resource.text().orElse(resource.title());
    Optional<String> runtimePath = resource.assetId().map(assets::get).map(Asset::path);
    return new ResourcePresentation(resource.id(), resource.title(), text, runtimePath);
  }

  private static Riddle requireRiddle(final Map<String, Riddle> riddles, final String riddleId) {
    Riddle riddle = riddles.get(riddleId);
    if (riddle == null) {
      throw new IllegalArgumentException("planned riddle is missing from the project: " + riddleId);
    }
    return riddle;
  }

  private static Surface requireSurface(
      final Map<String, Surface> surfaces, final String surfaceId) {
    Surface surface = surfaces.get(surfaceId);
    if (surface == null) {
      throw new IllegalArgumentException(
          "presented surface is missing from the project: " + surfaceId);
    }
    return surface;
  }

  private static <T> Map<String, T> index(
      final List<T> values, final Function<T, String> identity) {
    return values.stream()
        .collect(Collectors.toMap(identity, Function.identity(), (first, ignored) -> first));
  }
}
