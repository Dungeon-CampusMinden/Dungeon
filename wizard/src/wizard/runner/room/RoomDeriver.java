package wizard.runner.room;

import foundation.definition.CollectionInputDefinition;
import foundation.definition.ComposedRiddleDefinition;
import foundation.definition.DoorDefinition;
import foundation.definition.ExitDefinition;
import foundation.definition.HintDefinition;
import foundation.definition.InformationSourceDefinition;
import foundation.definition.InputDefinition;
import foundation.definition.NumericInputDefinition;
import foundation.definition.RiddleDefinition;
import foundation.definition.SectionDefinition;
import foundation.definition.TimerDefinition;
import foundation.definition.TimerMode;
import foundation.presentation.GamePresentation;
import foundation.presentation.GamePresentation.ComposedPresentation;
import foundation.presentation.GamePresentation.InformationSourcePresentation;
import foundation.presentation.GamePresentation.InputPresentation;
import foundation.presentation.GamePresentation.NumericInputPresentation;
import foundation.presentation.GamePresentation.ResourcePresentation;
import foundation.presentation.GamePresentation.RiddlePresentation;
import foundation.room.model.FoundationRoom;
import foundation.room.model.RoomLayout;
import foundation.room.model.VerifiedAsset;
import java.util.ArrayList;
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
import wizard.runner.validation.ValidationResult;

/** One-way mapper from validated Wizard project input to an in-memory Foundation room. */
public final class RoomDeriver {
  private static final String FUND_ASSET = "objects/treasurechest/treasurechest.png";
  private static final String NUMERIC_ASSET = "objects/keypad/on.png";

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
    List<SectionDefinition> sections = sections(plan, riddles);
    TimerDefinition timer = timer(project);
    String doorId = endSurfaceId(project);
    DoorDefinition door = new DoorDefinition(doorId);
    ExitDefinition exit = new ExitDefinition(endNodeId(project), doorId);
    GamePresentation presentation = presentation(project, plan, riddles, assets);
    RoomLayout layout = plan.layout();
    return new FoundationRoom(
        project.metadata().id(),
        project.metadata().title(),
        project.seed(),
        hostInputSha256,
        project.session().playerCount().min(),
        project.session().playerCount().max(),
        sections,
        timer,
        door,
        exit,
        presentation,
        layout,
        verifiedAssets);
  }

  private static List<SectionDefinition> sections(
      final SingleRoomPlanner.Plan plan, final Map<String, Riddle> riddles) {
    List<SectionDefinition> result = new ArrayList<>();
    for (SingleRoomPlanner.Section plannedSection : plan.sections()) {
      List<RiddleDefinition> sectionRiddles =
          plannedSection.riddleIds().stream()
              .map(riddleId -> definition(requireRiddle(riddles, riddleId)))
              .toList();
      result.add(new SectionDefinition(plannedSection.id(), sectionRiddles));
    }
    return List.copyOf(result);
  }

  private static RiddleDefinition definition(final Riddle riddle) {
    List<HintDefinition> hints =
        IntStream.range(0, riddle.hints().size())
            .mapToObj(
                index -> {
                  ProjectDefinition.Hint hint = riddle.hints().get(index);
                  return new HintDefinition(hint.id(), hint.title(), hint.text(), index + 1);
                })
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

  private static String endNodeId(final ProjectDefinition project) {
    return project.riddleGraph().nodes().stream()
        .filter(node -> node.kind() == GraphNodeKind.END)
        .map(GraphNode::id)
        .findFirst()
        .orElseThrow();
  }

  private static GamePresentation presentation(
      final ProjectDefinition project,
      final SingleRoomPlanner.Plan plan,
      final Map<String, Riddle> riddles,
      final Map<String, Asset> assets) {
    List<RiddlePresentation> presentations =
        plan.sections().stream()
            .flatMap(section -> section.riddleIds().stream())
            .map(riddleId -> presentation(requireRiddle(riddles, riddleId), assets))
            .toList();
    return new GamePresentation(
        presentations,
        project.scenario().introText(),
        project.scenario().mission(),
        project.scenario().successText(),
        project.scenario().failureText());
  }

  private static RiddlePresentation presentation(
      final Riddle riddle, final Map<String, Asset> assets) {
    List<InformationSourcePresentation> sources =
        riddle.informationSources().stream()
            .map(
                source ->
                    new InformationSourcePresentation(
                        source.id(),
                        source.surfaceId(),
                        FUND_ASSET,
                        source.resources().stream()
                            .map(resource -> resourcePresentation(resource, assets))
                            .toList()))
            .toList();
    List<InputPresentation> inputs =
        riddle.inputs().stream()
            .filter(NumericInput.class::isInstance)
            .map(NumericInput.class::cast)
            .map(
                input ->
                    (InputPresentation)
                        new NumericInputPresentation(input.id(), input.surfaceId(), NUMERIC_ASSET))
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

  private static <T> Map<String, T> index(
      final List<T> values, final Function<T, String> identity) {
    return values.stream()
        .collect(Collectors.toMap(identity, Function.identity(), (first, ignored) -> first));
  }
}
