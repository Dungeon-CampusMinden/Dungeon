package escaperoom.foundation.multiplayer.game;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.level.elements.tile.DoorTile;
import engine.level.elements.tile.ExitTile;
import engine.sound.SoundSpec;
import engine.systems.LevelSystem;
import engine.utils.Point;
import engine.utils.components.path.SimpleIPath;
import escaperoom.foundation.definition.AssignmentInputDefinition;
import escaperoom.foundation.definition.ChoiceInputDefinition;
import escaperoom.foundation.definition.InputDefinition;
import escaperoom.foundation.definition.NumericInputDefinition;
import escaperoom.foundation.definition.RoomDefinition;
import escaperoom.foundation.multiplayer.binding.ServerBinding;
import escaperoom.foundation.presentation.GamePresentation;
import escaperoom.foundation.presentation.GamePresentation.AssignmentInputPresentation;
import escaperoom.foundation.presentation.GamePresentation.ChoiceInputPresentation;
import escaperoom.foundation.presentation.GamePresentation.ComposedPresentation;
import escaperoom.foundation.presentation.GamePresentation.InformationSourcePresentation;
import escaperoom.foundation.presentation.GamePresentation.NumericInputPresentation;
import escaperoom.foundation.runtime.CodeOutcome;
import escaperoom.foundation.runtime.HintPreview;
import escaperoom.foundation.runtime.Projection;
import escaperoom.foundation.runtime.ReleasedHint;
import escaperoom.foundation.runtime.TerminalResult;
import feature.entities.MiscFactory;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import feature.interaction.keypad.KeypadComponent;
import feature.interaction.keypad.KeypadFactory;
import feature.tasks.AnswerItem;
import feature.tasks.AssignmentChecker;
import feature.tasks.AssignmentComponent;
import feature.tasks.TaskFactory;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiConsumer;

/** Dungeon world bridge for one already materialized Foundation room. */
public final class ServerGameBinding {
  private static final String HINT_ASSET = "items/book/wisdom_scroll.png";
  private static final String TERMINAL_ASSET = "objects/tech/Computer_1.png";
  private static final String KEYPAD_BACK = "Back";
  private static final String KEYPAD_SUBMIT = "Submit";
  private static final int HIDDEN_DIGIT_LIMIT = 8;
  private static final float KEYPAD_INTERACTION_RADIUS = 1.5f;

  private final ServerBinding serverBinding;
  private final FoundationSnapshotTranslator snapshotTranslator;
  private final GamePresentation presentation;
  private final DoorTile doorTile;
  private final ExitTile exitTile;
  private final Runnable onTerminalComplete;
  private final FoundationTracking tracking = new FoundationTracking();
  private final Map<String, ComposedPresentation> riddles;
  private final Map<String, InputDefinition> inputDefinitions;
  private final Map<String, PendingAssignment> pendingAssignments = new LinkedHashMap<>();
  private final Map<String, Integer> shownIntros = new LinkedHashMap<>();
  private final Set<String> terminalPresentedSlots = new LinkedHashSet<>();

  /**
   * Creates the bridge and immediately binds it to the derived room.
   *
   * @param serverBinding Foundation server binding
   * @param definition complete host-owned room definition
   * @param presentation complete player-facing room presentation
   * @param snapshotTranslator Foundation keypad snapshot translator
   * @param doorTile authoritative common-exit door
   * @param exitTile authoritative common-exit tile
   * @param levelSystem level system whose default exit callback is disabled
   * @param componentStations authoritative source and input-device positions
   * @param hintStations authoritative optional hint interaction positions
   * @param onTerminalComplete callback after the first client completes the terminal pages
   */
  public ServerGameBinding(
      final ServerBinding serverBinding,
      final RoomDefinition definition,
      final GamePresentation presentation,
      final FoundationSnapshotTranslator snapshotTranslator,
      final DoorTile doorTile,
      final ExitTile exitTile,
      final LevelSystem levelSystem,
      final Map<String, Point> componentStations,
      final Map<String, Point> hintStations,
      final Runnable onTerminalComplete) {
    this.serverBinding = Objects.requireNonNull(serverBinding, "serverBinding");
    inputDefinitions = indexInputDefinitions(Objects.requireNonNull(definition, "definition"));
    this.presentation = Objects.requireNonNull(presentation, "presentation");
    this.snapshotTranslator = Objects.requireNonNull(snapshotTranslator, "snapshotTranslator");
    FoundationDialogs.register();
    this.doorTile = Objects.requireNonNull(doorTile, "doorTile");
    this.exitTile = Objects.requireNonNull(exitTile, "exitTile");
    this.onTerminalComplete = Objects.requireNonNull(onTerminalComplete, "onTerminalComplete");
    Objects.requireNonNull(levelSystem, "levelSystem").onEndTile(() -> {});
    riddles = index(presentation.riddles());
    addStations(componentStations, hintStations);
    this.doorTile.close();
  }

  /** Advances the authority and mirrors exit presence and door state. */
  public synchronized void tick() {
    serverBinding.tick();
    reconcileIntros();
    checkActivatedAssignments();

    Set<String> present = new LinkedHashSet<>();
    for (Map.Entry<String, Integer> player : serverBinding.readyPlayerEntities().entrySet()) {
      if (entityInExit(player.getValue())) {
        present.add(player.getKey());
      }
    }
    serverBinding.reconcileExitPresence(present);
    var projection = serverBinding.projection();
    tracking.observe(projection);
    if (projection.doorOpen()) {
      doorTile.open();
    } else {
      doorTile.close();
    }
    projection.terminal().ifPresent(this::showTerminal);
  }

  private void reconcileIntros() {
    if (serverBinding.projection().terminal().isPresent()) {
      return;
    }
    Map<String, Integer> technicalPlayers = serverBinding.technicalPlayerEntities();
    Map<String, Integer> introEligiblePlayers = serverBinding.introEligiblePlayerEntities();
    shownIntros
        .entrySet()
        .removeIf(entry -> !Objects.equals(technicalPlayers.get(entry.getKey()), entry.getValue()));
    Set<String> readySlots = serverBinding.readyPlayerEntities().keySet();
    introEligiblePlayers.forEach(
        (slotId, entityId) -> {
          if (readySlots.contains(slotId) || shownIntros.containsKey(slotId)) {
            return;
          }
          if (Game.findEntityById(entityId).isEmpty()) {
            throw new IllegalStateException(
                "technically ready Foundation player entity is missing");
          }
          shownIntros.put(slotId, entityId);
          FoundationDialogs.showIntro(
              presentation, entityId, () -> serverBinding.completeIntro(slotId, entityId));
        });
  }

  private void addStations(
      final Map<String, Point> componentStations, final Map<String, Point> hintStations) {
    Map<String, Point> stations = Objects.requireNonNull(componentStations, "componentStations");
    riddles.values().stream()
        .forEach(
            riddle -> {
              riddle
                  .informationSources()
                  .forEach(
                      source ->
                          Game.add(
                              station(
                                  source.title(),
                                  source.runtimeAssetPath(),
                                  requireStation(stations, source.id()),
                                  (ignored, player) ->
                                      inspectSource(riddle.id(), source, player))));
              riddle.inputs().stream()
                  .forEach(
                      input -> {
                        switch (input) {
                          case NumericInputPresentation numeric ->
                              Game.add(
                                  keypad(
                                      riddle.id(),
                                      numeric,
                                      requireDefinition(numeric.id(), NumericInputDefinition.class),
                                      requireStation(stations, numeric.id())));
                          case ChoiceInputPresentation choice ->
                              Game.add(
                                  choiceTerminal(
                                      riddle.id(),
                                      choice,
                                      requireDefinition(choice.id(), ChoiceInputDefinition.class),
                                      requireStation(stations, choice.id())));
                          case AssignmentInputPresentation assignment ->
                              addAssignment(
                                  riddle.id(),
                                  assignment,
                                  requireDefinition(
                                      assignment.id(), AssignmentInputDefinition.class),
                                  stations);
                        }
                      });
            });
    Objects.requireNonNull(hintStations, "hintStations")
        .forEach(
            (riddleId, position) ->
                Game.add(
                    station(
                        "foundation_" + riddleId + "_hint",
                        HINT_ASSET,
                        position,
                        (ignored, player) -> requestHint(riddleId, player))));
  }

  private void inspectSource(
      final String riddleId, final InformationSourcePresentation source, final Entity player) {
    serverBinding
        .inspectSource(player, riddleId, source.id())
        .ifPresent(inspection -> showResources(inspection.resources(), player.id()));
  }

  private Entity keypad(
      final String riddleId,
      final NumericInputPresentation presentation,
      final NumericInputDefinition definition,
      final Point position) {
    Entity keypad =
        KeypadFactory.createKeypad(
            new Point(position),
            digits(definition.answer()),
            () -> {},
            definition.showDigitCount());
    keypad.name(presentation.title());
    snapshotTranslator.registerKeypad(keypad.id(), presentation.id());
    keypad.add(
        new InteractionComponent(
            new Interaction(
                (entity, player) -> {
                  if (serverBinding.inputActive(player, riddleId, presentation.id())) {
                    FoundationDialogs.showKeypad(
                        entity,
                        definition.showDigitCount()
                            ? definition.answer().length()
                            : HIDDEN_DIGIT_LIMIT,
                        definition.showDigitCount(),
                        action ->
                            handleKeypadAction(entity, player, riddleId, presentation.id(), action),
                        player.id());
                  }
                },
                KEYPAD_INTERACTION_RADIUS)));
    return keypad;
  }

  private boolean handleKeypadAction(
      final Entity keypad,
      final Entity player,
      final String riddleId,
      final String inputId,
      final String action) {
    if (!serverBinding.inputActive(player, riddleId, inputId)) {
      return false;
    }
    KeypadComponent component = keypad.fetch(KeypadComponent.class).orElseThrow();
    if (action.length() == 1 && action.charAt(0) >= '0' && action.charAt(0) <= '9') {
      int digit = action.charAt(0) - '0';
      component.addDigit(digit);
      playKeypadButtonSound(digit, player.id());
      return false;
    }
    if (KEYPAD_BACK.equals(action)) {
      component.backspace();
      playKeypadButtonSound(-1, player.id());
      return false;
    }
    if (!KEYPAD_SUBMIT.equals(action) || component.isUnlocked()) {
      return false;
    }

    String attempt =
        component.enteredDigits().stream()
            .map(String::valueOf)
            .reduce((left, right) -> left + right)
            .orElse("");
    return serverBinding
        .enterNumericCode(player, riddleId, inputId, attempt)
        .map(
            result -> {
              tracking.attempt(riddleId, inputId, "numeric-code", attempt, result, player);
              if (result.outcome() == CodeOutcome.CORRECT) {
                component.isUnlocked(true);
                keypad.fetch(DrawComponent.class).orElseThrow().sendSignal("open");
                Game.audio()
                    .playGlobal(SoundSpec.builder("retro_event_correct").targets(player.id()));
                return true;
              } else if (result.outcome() == CodeOutcome.INCORRECT) {
                Game.audio()
                    .playGlobal(SoundSpec.builder("retro_event_wrong").targets(player.id()));
                component.enteredDigits().clear();
              }
              return false;
            })
        .orElse(false);
  }

  private Entity choiceTerminal(
      final String riddleId,
      final ChoiceInputPresentation presentation,
      final ChoiceInputDefinition definition,
      final Point position) {
    return station(
        presentation.title(),
        TERMINAL_ASSET,
        position,
        (ignored, player) -> {
          if (serverBinding.inputActive(player, riddleId, presentation.id())) {
            TaskFactory.showChoice(
                definition.task(),
                presentation.title(),
                player,
                (who, selection) -> submitChoice(who, riddleId, presentation.id(), selection));
          }
        });
  }

  /**
   * Places the chests and answer items of one assignment and registers it for checking.
   *
   * <p>Players can sort before the riddle is active; the placement is checked again when it becomes
   * active.
   *
   * @param riddleId owning riddle identifier
   * @param presentation chest and item presentation
   * @param definition host-owned assignment task
   * @param stations placement points by component identifier
   */
  private void addAssignment(
      final String riddleId,
      final AssignmentInputPresentation presentation,
      final AssignmentInputDefinition definition,
      final Map<String, Point> stations) {
    Map<String, Entity> containers = new LinkedHashMap<>();
    presentation
        .containers()
        .forEach(
            container -> {
              Entity chest =
                  MiscFactory.newChest(
                      Set.of(), new Point(requireStation(stations, container.id())));
              chest.name(container.title());
              containers.put(container.id(), chest);
              Game.add(chest);
            });
    presentation
        .items()
        .forEach(
            item ->
                new AnswerItem(item.id(), item.name(), item.sprite())
                    .drop(new Point(requireStation(stations, item.id()))));
    Entity assignment =
        TaskFactory.buildItemAssignment(
            definition.task(),
            containers,
            actor -> submitAssignment(actor, riddleId, presentation.id(), containers, definition),
            (task, boxes) ->
                riddleActive(riddleId) && AssignmentChecker.CONTENTS.check(task, boxes));
    Game.add(assignment);
    pendingAssignments.put(
        presentation.id(),
        new PendingAssignment(riddleId, assignment.fetch(AssignmentComponent.class).orElseThrow()));
  }

  private void submitAssignment(
      final Entity actor,
      final String riddleId,
      final String inputId,
      final Map<String, Entity> containers,
      final AssignmentInputDefinition definition) {
    Optional<Entity> player =
        serverBinding.gameplayReady(actor)
            ? Optional.of(actor)
            : serverBinding.readyPlayerEntities().values().stream()
                .flatMap(entityId -> Game.findEntityById(entityId).stream())
                .findFirst();
    Map<String, String> placement =
        AssignmentChecker.placement(definition.task(), containers).orElse(Map.of());
    player.ifPresent(
        solver ->
            serverBinding
                .enterAssignment(solver, riddleId, inputId, placement)
                .ifPresent(
                    result -> {
                      tracking.attempt(
                          riddleId,
                          inputId,
                          "assignment",
                          new TreeMap<>(placement).toString(),
                          result,
                          solver);
                      if (result.outcome() == CodeOutcome.CORRECT) {
                        Game.audio()
                            .playGlobal(
                                SoundSpec.builder("retro_event_correct").targets(solver.id()));
                      }
                    }));
  }

  private boolean riddleActive(final String riddleId) {
    return serverBinding.projection().riddles().stream()
        .anyMatch(
            riddle ->
                riddle.id().equals(riddleId)
                    && riddle.status() == Projection.ProgressStatus.ACTIVE);
  }

  /** Checks sorted assignments once when their riddle becomes active. */
  private void checkActivatedAssignments() {
    pendingAssignments
        .values()
        .removeIf(
            pending -> {
              if (!riddleActive(pending.riddleId())) {
                return false;
              }
              pending.component().recheck();
              return true;
            });
  }

  private void submitChoice(
      final Entity player,
      final String riddleId,
      final String inputId,
      final Set<String> selection) {
    serverBinding
        .enterChoice(player, riddleId, inputId, selection)
        .ifPresent(
            result -> {
              String rawAnswer = String.join(",", new TreeSet<>(selection));
              tracking.attempt(riddleId, inputId, "choice", rawAnswer, result, player);
              if (result.outcome() == CodeOutcome.CORRECT) {
                Game.audio()
                    .playGlobal(SoundSpec.builder("retro_event_correct").targets(player.id()));
              } else if (result.outcome() == CodeOutcome.INCORRECT) {
                Game.audio()
                    .playGlobal(SoundSpec.builder("retro_event_wrong").targets(player.id()));
              }
            });
  }

  private static void playKeypadButtonSound(final int digit, final int targetEntityId) {
    float pitch = 1 + (digit - 5) * 0.05f;
    Game.audio()
        .playGlobal(SoundSpec.builder("retro_beep_01").pitch(pitch).targets(targetEntityId));
  }

  private void requestHint(final String riddleId, final Entity player) {
    Optional<HintPreview> preview = serverBinding.previewNextHint(player, riddleId);
    if (preview.isPresent()) {
      HintPreview next = preview.orElseThrow();
      FoundationDialogs.showHintConfirmation(
          next.severity(), player.id(), () -> confirmHint(riddleId, next, player));
      return;
    }
    if (serverBinding.gameplayReady(player)) {
      List<ReleasedHint> released =
          serverBinding.projection().riddles().stream()
              .filter(riddle -> riddle.id().equals(riddleId))
              .findFirst()
              .map(riddle -> riddle.releasedHints())
              .orElse(List.of());
      showHints(released, 0, player.id());
    }
  }

  private void confirmHint(final String riddleId, final HintPreview preview, final Entity player) {
    serverBinding
        .confirmNextHint(player, riddleId, preview.id())
        .ifPresentOrElse(
            hint -> {
              tracking.hintUsed(riddleId, hint, player);
              FoundationDialogs.showHint(hint, player.id(), () -> {});
            },
            () -> requestHint(riddleId, player));
  }

  private static void showHints(
      final List<ReleasedHint> hints, final int index, final int targetEntityId) {
    if (index >= hints.size()) {
      return;
    }
    FoundationDialogs.showHint(
        hints.get(index), targetEntityId, () -> showHints(hints, index + 1, targetEntityId));
  }

  private static void showResources(
      final List<GamePresentation.ResourcePresentation> resources, final int targetEntityId) {
    showResource(resources, 0, targetEntityId);
  }

  private static void showResource(
      final List<GamePresentation.ResourcePresentation> resources,
      final int index,
      final int targetEntityId) {
    if (index >= resources.size()) {
      return;
    }
    FoundationDialogs.showResource(
        resources.get(index),
        targetEntityId,
        () -> showResource(resources, index + 1, targetEntityId));
  }

  private void showTerminal(final TerminalResult terminal) {
    List<String> pages =
        terminal == TerminalResult.ABORTED
            ? List.of("Die Sitzung wurde abgebrochen.")
            : presentation.terminalPages(terminal);
    serverBinding
        .technicalPlayerEntities()
        .forEach(
            (slotId, entityId) -> {
              if (terminalPresentedSlots.contains(slotId)) {
                return;
              }
              FoundationDialogs.showTerminal(pages, entityId, onTerminalComplete);
              terminalPresentedSlots.add(slotId);
            });
  }

  private <T extends InputDefinition> T requireDefinition(
      final String inputId, final Class<T> type) {
    InputDefinition definition = inputDefinitions.get(inputId);
    if (!type.isInstance(definition)) {
      throw new IllegalArgumentException(
          "unknown Foundation " + type.getSimpleName() + " " + inputId);
    }
    return type.cast(definition);
  }

  private static Map<String, ComposedPresentation> index(final List<ComposedPresentation> riddles) {
    Map<String, ComposedPresentation> indexed = new LinkedHashMap<>();
    riddles.forEach(riddle -> indexed.put(riddle.id(), riddle));
    return Map.copyOf(indexed);
  }

  private static Map<String, InputDefinition> indexInputDefinitions(
      final RoomDefinition definition) {
    Map<String, InputDefinition> indexed = new LinkedHashMap<>();
    definition.progression().riddleNodes().stream()
        .map(node -> node.riddle())
        .flatMap(riddle -> riddle.inputs().stream())
        .forEach(input -> indexed.put(input.id(), input));
    return Map.copyOf(indexed);
  }

  private record PendingAssignment(String riddleId, AssignmentComponent component) {}

  private static Point requireStation(final Map<String, Point> stations, final String componentId) {
    Point point = stations.get(componentId);
    if (point == null) {
      throw new IllegalArgumentException("missing Foundation component station " + componentId);
    }
    return point;
  }

  private static List<Integer> digits(final String answer) {
    return answer.chars().map(digit -> digit - '0').boxed().toList();
  }

  private static Entity station(
      final String entityName,
      final String asset,
      final Point position,
      final BiConsumer<Entity, Entity> interaction) {
    Entity station = new Entity(entityName);
    station.add(new PositionComponent(new Point(Objects.requireNonNull(position, "position"))));
    station.add(new DrawComponent(new SimpleIPath(Objects.requireNonNull(asset, "asset"))));
    station.add(new InteractionComponent(new Interaction(interaction)));
    return station;
  }

  private boolean entityInExit(final int entityId) {
    return Game.findEntityById(entityId)
        .flatMap(Game::tileAtEntity)
        .filter(tile -> tile == exitTile)
        .isPresent();
  }
}
