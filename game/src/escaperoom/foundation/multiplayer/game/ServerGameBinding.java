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
import escaperoom.foundation.definition.NumericInputDefinition;
import escaperoom.foundation.definition.RoomDefinition;
import escaperoom.foundation.multiplayer.binding.ServerBinding;
import escaperoom.foundation.presentation.GamePresentation;
import escaperoom.foundation.presentation.GamePresentation.ComposedPresentation;
import escaperoom.foundation.presentation.GamePresentation.InformationSourcePresentation;
import escaperoom.foundation.presentation.GamePresentation.NumericInputPresentation;
import escaperoom.foundation.runtime.CodeOutcome;
import escaperoom.foundation.runtime.HintPreview;
import escaperoom.foundation.runtime.ReleasedHint;
import escaperoom.foundation.runtime.TerminalResult;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import feature.interaction.keypad.KeypadComponent;
import feature.interaction.keypad.KeypadFactory;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.LongSupplier;

/** Dungeon world bridge for one already materialized Foundation room. */
public final class ServerGameBinding {
  private static final String HINT_ASSET = "items/book/wisdom_scroll.png";
  private static final String KEYPAD_BACK = "Back";
  private static final String KEYPAD_SUBMIT = "Submit";
  private static final int HIDDEN_DIGIT_LIMIT = 8;
  private static final float KEYPAD_INTERACTION_RADIUS = 1.5f;

  private final ServerBinding serverBinding;
  private final FoundationSnapshotTranslator snapshotTranslator;
  private final GamePresentation presentation;
  private final DoorTile doorTile;
  private final ExitTile exitTile;
  private final LongSupplier monotonicNanos;
  private final Runnable onTerminalComplete;
  private final FoundationTracking tracking = new FoundationTracking();
  private final Map<String, ComposedPresentation> riddles;
  private final Map<String, NumericInputDefinition> numericDefinitions;
  private final Map<String, Integer> shownIntros = new LinkedHashMap<>();
  private final Set<String> terminalPresentedSlots = new LinkedHashSet<>();
  private long lastTickNanos;

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
   * @param componentStations authoritative source and numeric-input positions
   * @param hintStations authoritative optional hint interaction positions
   * @param monotonicNanos monotonic server-loop time source
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
      final LongSupplier monotonicNanos,
      final Runnable onTerminalComplete) {
    this.serverBinding = Objects.requireNonNull(serverBinding, "serverBinding");
    numericDefinitions = indexNumericDefinitions(Objects.requireNonNull(definition, "definition"));
    this.presentation = Objects.requireNonNull(presentation, "presentation");
    this.snapshotTranslator = Objects.requireNonNull(snapshotTranslator, "snapshotTranslator");
    FoundationDialogs.register();
    this.doorTile = Objects.requireNonNull(doorTile, "doorTile");
    this.exitTile = Objects.requireNonNull(exitTile, "exitTile");
    this.monotonicNanos = Objects.requireNonNull(monotonicNanos, "monotonicNanos");
    this.onTerminalComplete = Objects.requireNonNull(onTerminalComplete, "onTerminalComplete");
    Objects.requireNonNull(levelSystem, "levelSystem").onEndTile(() -> {});
    riddles = index(presentation.riddles());
    addStations(componentStations, hintStations);
    this.doorTile.close();
    lastTickNanos = monotonicNanos.getAsLong();
  }

  /** Advances the authority and mirrors exit presence and door state. */
  public synchronized void tick() {
    long now = monotonicNanos.getAsLong();
    serverBinding.tick(Duration.ofNanos(Math.max(0, now - lastTickNanos)));
    lastTickNanos = now;
    reconcileIntros();

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
                      input ->
                          Game.add(
                              keypad(
                                  riddle.id(),
                                  input,
                                  requireNumericDefinition(input.id()),
                                  requireStation(stations, input.id()))));
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
            () ->
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
                                handleKeypadAction(
                                    entity, player, riddleId, presentation.id(), action),
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
              tracking.attempt(riddleId, inputId, attempt, result, player);
              if (result.outcome() == CodeOutcome.CORRECT) {
                component.isUnlocked(true);
                keypad.fetch(DrawComponent.class).orElseThrow().sendSignal("open");
                Game.audio()
                    .playGlobal(SoundSpec.builder("retro_event_correct").targets(player.id()));
                return true;
              } else if (result.outcome() == CodeOutcome.INCORRECT) {
                Game.audio()
                    .playGlobal(SoundSpec.builder("retro_event_wrong").targets(player.id()));
              }
              return false;
            })
        .orElse(false);
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

  private NumericInputDefinition requireNumericDefinition(final String inputId) {
    NumericInputDefinition definition = numericDefinitions.get(inputId);
    if (definition == null) {
      throw new IllegalArgumentException("unknown Foundation numeric input definition " + inputId);
    }
    return definition;
  }

  private static Map<String, ComposedPresentation> index(final List<ComposedPresentation> riddles) {
    Map<String, ComposedPresentation> indexed = new LinkedHashMap<>();
    riddles.forEach(riddle -> indexed.put(riddle.id(), riddle));
    return Map.copyOf(indexed);
  }

  private static Map<String, NumericInputDefinition> indexNumericDefinitions(
      final RoomDefinition definition) {
    Map<String, NumericInputDefinition> indexed = new LinkedHashMap<>();
    definition.progression().riddleNodes().stream()
        .map(node -> node.riddle())
        .flatMap(riddle -> riddle.inputs().stream())
        .filter(NumericInputDefinition.class::isInstance)
        .map(NumericInputDefinition.class::cast)
        .forEach(numeric -> indexed.put(numeric.id(), numeric));
    return Map.copyOf(indexed);
  }

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
    station.add(new InteractionComponent(() -> new Interaction(interaction)));
    return station;
  }

  private boolean entityInExit(final int entityId) {
    return Game.findEntityById(entityId)
        .flatMap(Game::tileAtEntity)
        .filter(tile -> tile == exitTile)
        .isPresent();
  }
}
