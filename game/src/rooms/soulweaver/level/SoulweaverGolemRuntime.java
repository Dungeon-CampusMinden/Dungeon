package rooms.soulweaver.level;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.components.VelocityComponent;
import engine.level.DungeonLevel;
import engine.level.Tile;
import engine.level.utils.Coordinate;
import engine.utils.Point;
import engine.utils.Rectangle;
import engine.utils.Vector2;
import engine.utils.components.draw.state.StateMachine;
import feature.collision.CollisionUtils;
import feature.components.CollideComponent;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;
import feature.systems.PositionSync;
import feature.utils.EntityUtils;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import rooms.soulweaver.Soulweaver;
import rooms.soulweaver.SoulweaverAchievements;
import rooms.soulweaver.SoulweaverRoomController;
import rooms.soulweaver.modules.loops.LoopExecution;
import rooms.soulweaver.modules.loops.LoopMaze;
import rooms.soulweaver.modules.loops.LoopProgram;
import rooms.soulweaver.modules.loops.LoopPuzzle;
import rooms.soulweaver.modules.loops.TerminalState;
import rooms.soulweaver.modules.variables.BindingState;
import rooms.soulweaver.modules.variables.GolemProperty;
import rooms.soulweaver.modules.variables.MagicalEssence;
import rooms.soulweaver.modules.variables.SoulVessel;
import rooms.soulweaver.modules.variables.VariablePuzzle;
import rooms.soulweaver.state.SoulweaverPhase;
import rooms.soulweaver.state.VariablePuzzleStage;

/** Server-owned assignment dialogs and collision-checked movement of the one shared golem. */
final class SoulweaverGolemRuntime {
  private static final float SPEED = 2.5f;
  private static final float DECISION_SPEED = 5f;
  private final DungeonLevel level;
  private final SoulweaverRoomController controller = new SoulweaverRoomController();
  private final PositionComponent position;
  private final VelocityComponent velocity;
  private final CollideComponent collision;
  private final Map<GolemProperty, SoulVessel> vessels = new EnumMap<>(GolemProperty.class);
  private final Map<GolemProperty, MagicalEssence> essences = new EnumMap<>(GolemProperty.class);
  private final ArrayDeque<Point> route = new ArrayDeque<>();
  private final Entity golem;
  private final SoulweaverCellarMachinery machinery;
  private final SoulweaverWorkshopRuntime workshop;
  private final SoulweaverDecisionRuntime decisions;
  private final SoulweaverHelp help;
  private boolean assistedLoops;
  private boolean assistedRuneCollection;
  private boolean manualRuneCollected;
  private boolean solvingBinding;
  private java.util.UUID attemptParticipant;
  private String attemptCode = "";
  private String attemptPuzzle = "";
  private int attemptHintLevel;
  private boolean attemptAutomaticSolution;
  private java.util.function.Consumer<String> movementFailed;
  private boolean workshopTransit;
  private boolean awaitingWorkshopRoute;
  private float workshopRouteRetry;
  private LoopExecution attempt;
  private LoopMaze.Direction facing = LoopMaze.Direction.EAST;
  private boolean mazeReady;
  private boolean returning;
  private boolean monsterAlive = true;
  private Entity monster;
  private String activeRune = "";
  private float actionTime;
  private boolean jumping;
  private Point jumpStart;
  private boolean attacking;
  private final List<DrawComponent> wall;
  private float wallBreakTime;
  private boolean wallBroken;
  private Point lastPosition;
  private float stalled;
  private Runnable arrived = () -> {};
  private boolean busy;
  private boolean breakingGate;
  private float pause;
  private String status = "Seelenbindung unvollständig.";
  private String returnFeedback = "";
  private boolean essencesCollected;
  private boolean vesselsCollected;
  private int loopFailures;
  private int checkpointFailures;
  private String bindingFeedback = "Gefäße und Essenzen fehlen. Öffne die beiden Werkstattkisten.";

  SoulweaverGolemRuntime(DungeonLevel level, Entity golem) {
    this.level = level;
    this.golem = golem;
    position = golem.fetch(PositionComponent.class).orElseThrow();
    velocity = golem.fetch(VelocityComponent.class).orElseThrow();
    collision = golem.fetch(CollideComponent.class).orElseThrow();
    wall = SoulweaverProps.wall(level);
    machinery = new SoulweaverCellarMachinery(level, golem);
    workshop = new SoulweaverWorkshopRuntime(level, golem, this);
    decisions = new SoulweaverDecisionRuntime(level, golem, this);
    help = new SoulweaverHelp(this);
  }

  void show(Entity who) {
    if (!authorized(who, "variables-golem", 4.5f)) return;
    if (decisions.active()) {
      decisions.show(who);
    } else if (busy) {
      text(who, status);
    } else if (controller.phase() == SoulweaverPhase.VARIABLES) {
      SoulweaverBinding.open(who, this);
    } else if (controller.phase() == SoulweaverPhase.METHODS) {
      text(who, "Valerius' Pr\u00fcfungsbuch liegt am Eingang zur Werkstatt.");
    } else {
      showTerminal(who);
    }
  }

  BindingState bindingState() {
    return new BindingState(
        controller.variableStage(),
        essencesCollected,
        vesselsCollected,
        vessels,
        essences,
        bindingFeedback);
  }

  void collectBindingSupply(boolean essenceChest, Entity who) {
    String marker = essenceChest ? "variables-essences" : "variables-vessels";
    if (!authorized(who, marker, 3f)) return;
    if (essenceChest ? essencesCollected : vesselsCollected) {
      showText(who, "Die Kiste ist leer.");
      return;
    }
    if (essenceChest) essencesCollected = true;
    else vesselsCollected = true;
    bindingFeedback =
        essencesCollected && vesselsCollected
            ? "Gefäße zuordnen und Essenzen einsetzen. Valerius' Bindungsplan liegt beim Golem."
            : essencesCollected ? "Gefäßvorrat fehlt." : "Essenzfach fehlt.";
    showText(
        who,
        essenceChest
            ? "Essenzen eingepackt. Valerius' Bindungsplan nennt den Wert für jede Fassung. Der Vorrat reicht für mehrere Fassungen."
            : "Seelengefäße eingepackt. Jedes Gefäß trägt eine Prägung für seinen Inhalt. Der Vorrat reicht für mehrere Fassungen.");
  }

  void assignBinding(Entity who, String propertyName, String value, boolean vessel) {
    if (!authorized(who, "variables-golem", 4.5f)
        || busy
        || controller.phase() != SoulweaverPhase.VARIABLES
        || !(vessel ? vesselsCollected : essencesCollected)) return;
    if (bindingState().revealed()
        || vessel && controller.variableStage() != VariablePuzzleStage.VESSELS) return;
    try {
      GolemProperty property = GolemProperty.valueOf(propertyName);
      if (vessel) {
        SoulVessel selected = SoulVessel.valueOf(value);
        recordBinding(
            who,
            property,
            selected.name(),
            true,
            VariablePuzzle.vesselSolution().get(property) == selected
                ? List.of()
                : List.of("Falscher Gefäßtyp für " + property.label() + "."));
        logBinding(
            true,
            property.label() + ": " + selected.label(),
            VariablePuzzle.vesselSolution().get(property) == selected
                ? "richtig"
                : "falscher Gefäßtyp");
        if (VariablePuzzle.vesselSolution().get(property) != selected) {
          bindingFeedback =
              selected == SoulVessel.CRYSTAL_BOTTLE
                      && VariablePuzzle.vesselSolution().get(property) == SoulVessel.IRON_CHEST
                  ? "Die Kristallflasche fasst auch ganze Zahlen. Valerius verwendet hier die Eisenkiste; Kristall ist für Bruchteile vorgesehen."
                  : selected.label()
                      + " trägt: "
                      + selected.capacity()
                      + ". Wähle ein Gefäß, das zum benötigten Wert passt.";
          return;
        }
        vessels.put(property, selected);
        bindingFeedback = property.label() + ": " + selected.label() + " eingesetzt.";
        if (vessels.size() == GolemProperty.values().length) {
          controller.submitVessels(vessels);
          SoulweaverProgress.solved("vessels", "Alle Gefäße sind zugeordnet.");
          SoulweaverProgress.started("essences", "Setze die Werte aus dem Bindungsplan ein.");
          bindingFeedback = "Alle Gefäße eingesetzt. Fehlende Füllungen ergänzen.";
        }
      } else {
        MagicalEssence selected = MagicalEssence.valueOf(value);
        SoulVessel container = vessels.get(property);
        recordBinding(
            who,
            property,
            selected.name(),
            false,
            container == null
                ? List.of("Gefäß fehlt für " + property.label() + ".")
                : !VariablePuzzle.fits(container, selected)
                    ? List.of(
                        "Falscher Datentyp: "
                            + selected.literal()
                            + " passt nicht in "
                            + container.label()
                            + ".")
                    : VariablePuzzle.essenceSolution().get(property) != selected
                        ? List.of(
                            "Falscher Wert für "
                                + property.label()
                                + ": "
                                + selected.literal()
                                + ".")
                        : List.of());
        logBinding(
            false,
            property.label() + " = " + selected.literal(),
            container == null
                ? "Gefäß fehlt"
                : !VariablePuzzle.fits(container, selected)
                    ? "passt nicht in " + container.label()
                    : VariablePuzzle.essenceSolution().get(property) != selected
                        ? "falscher Wert"
                        : "richtig");
        if (container == null || !VariablePuzzle.fits(container, selected)) {
          if (container != null && !solvingBinding) SoulweaverAchievements.WRONG_TYPE.unlock(who);
          bindingFeedback =
              selected.literal()
                  + " passt nicht in "
                  + (container == null
                      ? "diese Fassung."
                      : container.label() + ". " + container.capacity() + ".");
          return;
        }
        MagicalEssence previous = essences.put(property, selected);
        if (!solvingBinding && previous != null && previous != selected)
          SoulweaverAchievements.OVERWRITE.unlock(who);
        if (!solvingBinding
            && property == GolemProperty.ACTIVATED
            && selected == MagicalEssence.BOOLEAN_FALSE) SoulweaverAchievements.SLEEPY.unlock(who);
        bindingFeedback =
            property.label()
                + " = "
                + selected.literal()
                + " gespeichert."
                + (VariablePuzzle.essenceSolution().get(property) == selected
                    ? ""
                    : " Bindung reagiert nicht.");
        if (VariablePuzzle.essencesCorrect(essences)) {
          controller.submitEssences(essences);
          SoulweaverProgress.solved(
              "essences", "Die Seelenbindung ist vollständig. Aktiviere Nox.");
          SoulweaverAchievements.BOUND.unlock();
          bindingFeedback = "Seelenbindung vollständig. Gefäß, Name und Wert bilden eine Variable.";
        }
      }
    } catch (IllegalArgumentException ignored) {
      // Unknown or stale canvas payloads do not change the shared binding.
    }
  }

  void clearBinding(Entity who, String propertyName) {
    if (!authorized(who, "variables-golem", 4.5f)
        || busy
        || controller.phase() != SoulweaverPhase.VARIABLES) return;
    try {
      GolemProperty property = GolemProperty.valueOf(propertyName);
      if (bindingState().revealed()) return;
      if (essences.remove(property) == null
          && controller.variableStage() == VariablePuzzleStage.VESSELS) vessels.remove(property);
      SoulweaverProgress.interaction(helpPuzzle(), "clear-" + property.name(), who);
      bindingFeedback = property.label() + ": Fassung geleert.";
    } catch (IllegalArgumentException ignored) {
      // Ignore unknown property IDs.
    }
  }

  void showBindingBook(Entity who) {
    if (authorized(who, "variables-translation", 3f)) SoulweaverBindingBook.open(who);
  }

  void activate(Entity who) {
    if (!authorized(who, "variables-golem", 4.5f)
        || busy
        || controller.variableStage() != VariablePuzzleStage.REVEAL) return;
    breakingGate = true;
    Point destination = level.getPoint("loop-departure");
    List<Point> path = path(position.position(), destination);
    if (path.isEmpty() && Point.calculateDistance(position.position(), destination) > 0.1f) {
      breakingGate = false;
      text(who, "Aktivierung unterbrochen. Laufweg blockiert.");
      return;
    }
    if (!controller.activateGolem()) {
      breakingGate = false;
      return;
    }
    SoulweaverProgress.interaction("essences", "activate-golem", who);
    status = "Aktivierung läuft.";
    SoulweaverBinding.closeAll();
    move(
        path,
        () -> {
          Game.allPlayers()
              .forEach(
                  player ->
                      player
                          .fetch(PositionComponent.class)
                          .ifPresent(
                              at -> {
                                if (at.position().x()
                                    >= level.getPoint("departure-gate-start").x()) {
                                  at.position(new Point(35, 8));
                                  PositionSync.syncPosition(player);
                                }
                              }));
          SoulweaverGates.departure(level, false);
          breakingGate = false;
          enterCellar();
        });
  }

  /** Begins Act II at the first cellar checkpoint, after activation or from a savegame. */
  private void enterCellar() {
    position.position(
        LoopMaze.world(level.getPoint("maze-origin"), LoopMaze.checkpoints().getFirst().start()));
    PositionSync.syncPosition(golem);
    mazeReady = true;
    SoulweaverProgress.started("cellar-0", "Bringe Nox zur ersten Zielmarke und richte ihn aus.");
    Game.levelEntities()
        .filter(entity -> entity.name().equals("soulweaver-loop-monitor"))
        .flatMap(entity -> entity.fetch(DrawComponent.class).stream())
        .forEach(draw -> draw.stateMachine().setState("active", null));
    face(LoopMaze.checkpoints().getFirst().facing());
    status = "Keller erreicht. Räumauftrag bereit.";
    Soulweaver.saveCheckpoint(SoulweaverPhase.LOOPS);
  }

  TerminalState terminalState() {
    Point origin = level.getPoint("maze-origin");
    int checkpoint = controller.completedLoops();
    return new TerminalState(
        LoopPuzzle.runes().stream()
            .map(rune -> rune.id())
            .filter(controller.collectedLoopRunes()::contains)
            .toList(),
        checkpoint,
        golem.id(),
        (position.position().x() - origin.x()) / LoopMaze.CELL_WIDTH,
        (position.position().y() - origin.y()) / LoopMaze.CELL_HEIGHT,
        busy,
        mazeReady,
        activeRune,
        status);
  }

  void showTerminal(Entity who) {
    if (authorized(who, "loop-terminal", 3f)) SoulweaverTerminal.open(who, this);
  }

  void showObservation(Entity who) {
    if (mazeReady && authorized(who, "loop-monitor", 3f)) {
      SoulweaverObservation.open(who, this);
      SoulweaverAchievements.OBSERVER.unlock(who);
    }
  }

  boolean collectRune(String runeId, Entity who) {
    if (!authorized(who, "rune-" + runeId, 3f)) return false;
    return collectRune(runeId, who, false);
  }

  private boolean collectRune(String runeId, Entity who, boolean assisted) {
    if (!controller.collectLoopRune(runeId)) return false;
    assistedRuneCollection |= assisted;
    if (assisted) {
      SoulweaverProgress.discoverRune(LoopPuzzle.rune(runeId).orElseThrow(), who);
      Game.levelEntities()
          .filter(entity -> entity.name().equals("soulweaver-rune-" + runeId))
          .toList()
          .forEach(Game::remove);
    }
    if (!assisted && !manualRuneCollected) {
      manualRuneCollected = true;
      SoulweaverAchievements.FIRST_RUNE.unlock();
    }
    if (!assistedRuneCollection
        && controller.collectedLoopRunes().size() == LoopPuzzle.runes().size())
      SoulweaverAchievements.ARCHIVIST.unlock();
    return true;
  }

  void showMethods(Entity who) {
    workshop.show(who);
  }

  void beginDecisions() {
    controller.completeMethods();
    velocity.maxSpeed(DECISION_SPEED);
    decisions.start();
    Soulweaver.saveCheckpoint(SoulweaverPhase.DECISIONS);
  }

  /**
   * Rebuilds the beginning of a saved act without replaying earlier acts.
   *
   * @param phase Act II, III or IV
   */
  void restore(SoulweaverPhase phase) {
    controller.restore(phase);
    SoulweaverProgress.restore(phase);
    vessels.putAll(VariablePuzzle.vesselSolution());
    essences.putAll(VariablePuzzle.essenceSolution());
    essencesCollected = true;
    vesselsCollected = true;
    Game.levelEntities()
        .filter(
            entity ->
                entity.name().equals("soulweaver-variables-essences")
                    || entity.name().equals("soulweaver-variables-vessels"))
        .flatMap(entity -> entity.fetch(DrawComponent.class).stream())
        .forEach(draw -> draw.stateMachine().setState("open_empty", null));
    SoulweaverGates.open(level, 1);
    wallBroken = true;
    wall.forEach(
        draw -> {
          draw.tintColor(-1);
          draw.stateMachine().setState("broken", null);
        });
    if (phase == SoulweaverPhase.LOOPS) {
      enterCellar();
      return;
    }
    machinery.restoreCleared();
    monsterAlive = false;
    Game.levelEntities()
        .filter(entity -> entity.name().equals("soulweaver-maze-monster"))
        .toList()
        .forEach(Game::remove);
    if (phase == SoulweaverPhase.METHODS) {
      position.position(level.getPoint("methods-home"));
      PositionSync.syncPosition(golem);
      workshop.arrive();
      return;
    }
    SoulweaverWorkshopWorld.restoreCompleted();
    SoulweaverGates.open(level, 3);
    position.position(SoulweaverDecisionWorld.START);
    PositionSync.syncPosition(golem);
    beginDecisions();
  }

  void completeDecisions() {
    controller.completeDecisions();
  }

  boolean tickEnding() {
    decisions.ending.tick();
    return decisions.ending.active();
  }

  boolean methodsActive() {
    return controller.phase() == SoulweaverPhase.METHODS;
  }

  void moveWorkshop(
      List<Point> points, Runnable success, java.util.function.Consumer<String> failure) {
    movementFailed = failure;
    move(
        points,
        () -> {
          movementFailed = null;
          success.run();
        });
  }

  /** Cancels the running workshop command without invoking its completion callback. */
  void stopWorkshopMovement() {
    if (workshopTransit || awaitingWorkshopRoute) return;
    route.clear();
    arrived = () -> {};
    movementFailed = null;
    pause = 0;
    stalled = 0;
    busy = false;
    velocity.clearForces();
    velocity.currentVelocity(Vector2.ZERO);
  }

  List<Point> workshopPath(Point destination) {
    return path(position.position(), destination);
  }

  void faceWorkshop(LoopMaze.Direction direction) {
    face(direction);
  }

  private void notifyMovementFailure(String reason) {
    var callback = movementFailed;
    movementFailed = null;
    if (callback != null) callback.accept(reason);
  }

  private void returnUpstairs() {
    mazeReady = false;
    SoulweaverGates.departure(level, true);
    workshopTransit = true;
    status = "Der Schutt ist beseitigt. Die Winde leider auch.";
    Game.allPlayers().forEach(player -> showText(player, status));
    awaitingWorkshopRoute = true;
    beginWorkshopReturn();
  }

  private void beginWorkshopReturn() {
    List<Point> points = workshopPath(level.getPoint("methods-home"));
    if (points.isEmpty()
        && Point.calculateDistance(position.position(), level.getPoint("methods-home")) > .1f) {
      busy = false;
      workshopRouteRetry = 1f;
      return;
    }
    awaitingWorkshopRoute = false;
    moveWorkshop(
        points,
        () -> {
          workshopTransit = false;
          workshop.arrive();
          Soulweaver.saveCheckpoint(SoulweaverPhase.METHODS);
        },
        ignored -> awaitingWorkshopRoute = true);
  }

  private boolean authorized(Entity who, String marker, float range) {
    if (Game.isMultiplayerClient()
        || who == null
        || Game.allPlayers().noneMatch(player -> player == who)) return false;
    if (marker.equals("variables-golem")) return EntityUtils.getDistance(golem, who) <= range;
    Point target = level.namedPoints().get(marker);
    return target != null
        && who.fetch(PositionComponent.class)
            .map(at -> Point.calculateDistance(at.position(), target) <= range)
            .orElse(false);
  }

  void executeRune(String runeId, Entity who) {
    executeRune(runeId, who, false);
  }

  private void executeRune(String runeId, Entity who, boolean automaticSolution) {
    if (!authorized(who, "loop-terminal", 3f)
        || busy
        || activeRune.equals(runeId)
        || !mazeReady
        || controller.phase() != SoulweaverPhase.LOOPS
        || !controller.collectedLoopRunes().contains(runeId)) return;
    var rune = LoopPuzzle.rune(runeId);
    if (rune.isEmpty()) return;
    int checkpoint = controller.completedLoops();
    if (Point.calculateDistance(
                position.position(),
                LoopMaze.world(
                    level.getPoint("maze-origin"), LoopMaze.checkpoints().get(checkpoint).start()))
            > .1f
        || facing != LoopMaze.checkpoints().get(checkpoint).facing()) return;
    attemptParticipant = SoulweaverProgress.participant(who).orElse(null);
    attemptPuzzle = "cellar-" + checkpoint;
    attemptCode = rune.orElseThrow().code();
    attemptHintLevel = help.level(attemptPuzzle);
    attemptAutomaticSolution = automaticSolution;
    SoulweaverProgress.interaction(attemptPuzzle, "execute", who);
    attempt = new LoopExecution(checkpoint, rune.orElseThrow(), monsterAlive);
    activeRune = runeId;
    busy = true;
    advanceAction();
  }

  void removeRune(String runeId, Entity who) {
    if (!authorized(who, "loop-terminal", 3f) || busy || !activeRune.equals(runeId)) return;
    SoulweaverProgress.interaction("rune-" + runeId, "remove", who);
    activeRune = "";
  }

  private void advanceAction() {
    attacking = false;
    position.rotation(0);
    golem.fetch(DrawComponent.class).ifPresent(draw -> draw.tintColor(-1));
    var step = attempt.next();
    if (step.isEmpty()) {
      finishAttempt(attempt.success(), attempt.failure());
      return;
    }
    LoopExecution.Step instruction = step.orElseThrow();
    face(instruction.facing());
    status = instruction.action().code();
    if (instruction.action() == LoopProgram.Action.ATTACK) {
      attacking = true;
      actionTime = 0;
    }
    Runnable complete =
        () -> {
          attempt.complete();
          if (monsterAlive && !attempt.monsterAlive()) {
            monsterAlive = false;
            Game.levelEntities()
                .filter(entity -> entity.name().equals("soulweaver-maze-monster"))
                .findFirst()
                .ifPresent(
                    entity -> {
                      monster = entity;
                      Game.remove(entity);
                    });
          }
          advanceAction();
        };
    if (instruction.from().equals(instruction.to())) {
      busy = true;
      pause = .45f;
      arrived = complete;
    } else {
      jumping = instruction.action() == LoopProgram.Action.JUMP;
      jumpStart = position.position();
      actionTime = 0;
      move(
          List.of(LoopMaze.world(level.getPoint("maze-origin"), instruction.to())),
          () -> {
            jumping = false;
            golem.fetch(DrawComponent.class).ifPresent(draw -> draw.tintColor(-1));
            complete.run();
          });
    }
  }

  private void finishAttempt(boolean success, String reason) {
    LoopExecution finished = attempt;
    attempt = null;
    route.clear();
    jumping = false;
    attacking = false;
    position.rotation(0);
    golem.fetch(DrawComponent.class).ifPresent(draw -> draw.tintColor(-1));
    if (!success) recordLoopOutcome(List.of(reason.isEmpty() ? missedGoal(finished) : reason));
    if (success) {
      String challenge = currentChallenge();
      busy = true;
      status = "Programm beendet. Arbeitsposition bestätigt.";
      pause = 1f;
      arrived =
          () -> {
            busy = true;
            status = "Räumauftrag läuft. Steuerprogramm beendet.";
            machinery.clear(
                controller.completedLoops(),
                () -> {
                  controller.completeExecutedLoop(challenge);
                  recordLoopOutcome(List.of());
                  SoulweaverProgress.solved(
                      attemptPuzzle,
                      "Nox hat die Zielmarke erreicht und den Räumauftrag erledigt.");
                  if (controller.completedLoops() == 1) SoulweaverAchievements.FIRST_ROUTE.unlock();
                  if (checkpointFailures > 0) SoulweaverAchievements.SECOND_TRY.unlock();
                  checkpointFailures = 0;
                  if (controller.phase() == SoulweaverPhase.METHODS) {
                    SoulweaverAchievements.CELLAR_CLEAR.unlock();
                    if (loopFailures == 0 && !assistedLoops)
                      SoulweaverAchievements.CLEAN_RUN.unlock();
                    returnUpstairs();
                    return;
                  }
                  SoulweaverProgress.started(
                      "cellar-" + controller.completedLoops(),
                      "Bringe Nox zur nächsten Zielmarke und richte ihn aus.");
                  busy = false;
                  status = "Räumauftrag erledigt. Nächste Arbeitsposition bereit.";
                });
          };
    } else {
      loopFailures++;
      checkpointFailures++;
      returnFeedback =
          reason.isEmpty()
              ? "Programm beendet. " + missedGoal(finished)
              : "Programm gestoppt. " + reason;
      status = returnFeedback + " Nox kehrt zurück.";
      returning = true;
      busy = true;
      pause = 1.5f;
      List<Point> retrace = new ArrayList<>();
      List<LoopMaze.Cell> history = finished.history();
      // Only reached anchors belong in the return path; an interrupted destination is not one.
      for (int i = history.size() - 1; i >= 0; i--)
        retrace.add(LoopMaze.world(level.getPoint("maze-origin"), history.get(i)));
      arrived = () -> move(retrace, this::resetAttempt);
    }
  }

  /**
   * @param finished attempt that ended without an explicit failure
   * @return why the program missed the current checkpoint
   */
  private String missedGoal(LoopExecution finished) {
    return finished.cell().equals(LoopMaze.checkpoints().get(controller.completedLoops()).goal())
        ? "Blickrichtung falsch."
        : "Zielmarke nicht erreicht.";
  }

  private void resetAttempt() {
    jumping = false;
    golem.fetch(DrawComponent.class).ifPresent(draw -> draw.tintColor(-1));
    int checkpoint = controller.completedLoops();
    Point home =
        LoopMaze.world(
            level.getPoint("maze-origin"), LoopMaze.checkpoints().get(checkpoint).start());
    position.position(home);
    PositionSync.syncPosition(golem);
    face(LoopMaze.checkpoints().get(checkpoint).facing());
    if (checkpoint <= 2 && !monsterAlive) {
      monsterAlive = true;
      if (monster != null) Game.add(monster);
    }
    route.clear();
    busy = false;
    returning = false;
    status = returnFeedback + " Nox ist zurück an der Arbeitsposition.";
    if (activeRune.equals("archive-spin")) SoulweaverAchievements.SPIN.unlock();
    activeRune = "";
  }

  private void face(LoopMaze.Direction direction) {
    facing = direction;
    position.viewDirection(
        switch (direction) {
          case EAST -> engine.utils.Direction.RIGHT;
          case NORTH -> engine.utils.Direction.UP;
          case WEST -> engine.utils.Direction.LEFT;
          case SOUTH -> engine.utils.Direction.DOWN;
        });
    // Stationary turns must update the sprite too; idle signals do not re-enter the idle state.
    golem
        .fetch(DrawComponent.class)
        .ifPresent(
            draw ->
                draw.stateMachine().setState(StateMachine.IDLE_STATE, position.viewDirection()));
  }

  private void move(List<Point> path, Runnable then) {
    route.clear();
    route.addAll(path);
    arrived = then;
    busy = true;
    lastPosition = position.position();
    stalled = 0;
  }

  void tick() {
    if (Game.isMultiplayerClient()) return;
    var movement = Game.systems().get(engine.systems.MoveSystem.class);
    if (movement != null && !movement.isRunning()) return;
    // LevelTick runs before VelocitySystem and MoveSystem. The latter owns physical movement.
    velocity.clearForces();
    velocity.currentVelocity(Vector2.ZERO);
    if (machinery.working()) {
      machinery.tick(1f / Game.frameRate());
      return;
    }
    if (awaitingWorkshopRoute) {
      workshopRouteRetry -= 1f / Game.frameRate();
      if (workshopRouteRetry <= 0) beginWorkshopReturn();
    }
    workshop.tick();
    decisions.tick();
    if (wallBreakTime > 0) {
      wallBreakTime -= 1f / Game.frameRate();
      if (wallBreakTime <= 0) wall.forEach(draw -> draw.stateMachine().setState("broken", null));
    }
    if (!busy) {
      return;
    }
    float delta = 1f / Game.frameRate();
    actionTime += delta;
    if (jumping) golem.fetch(DrawComponent.class).ifPresent(draw -> draw.tintColor(0xBBDDFFFF));
    if (attacking) {
      position.rotation((float) Math.sin(actionTime / .45f * Math.PI) * 12f);
      golem.fetch(DrawComponent.class).ifPresent(draw -> draw.tintColor(0xFFCC99FF));
    }
    if (pause > 0) {
      pause -= delta;
      return;
    }
    if (!route.isEmpty() && position.position().equals(route.peekFirst())) {
      route.removeFirst();
      if (returning) {
        jumping = false;
        golem.fetch(DrawComponent.class).ifPresent(draw -> draw.tintColor(-1));
      }
    }
    if (route.isEmpty()) {
      busy = false;
      Runnable callback = arrived;
      arrived = () -> {};
      callback.run();
      return;
    }
    Point target = route.peekFirst();
    Point from = position.position();
    if (returning && !jumping) {
      Point pit = LoopMaze.world(level.getPoint("maze-origin"), LoopMaze.pit());
      if (Math.abs(from.x() - pit.x()) < .01f
          && Math.abs(target.x() - pit.x()) < .01f
          && Math.abs((from.y() + target.y()) / 2 - pit.y()) < .01f
          && Math.abs(Math.abs(from.y() - target.y()) - 2 * LoopMaze.CELL_HEIGHT) < .01f) {
        jumping = true;
        jumpStart = from;
        actionTime = 0;
        face(target.y() > from.y() ? LoopMaze.Direction.NORTH : LoopMaze.Direction.SOUTH);
      }
    }
    if (jumping) {
      // A jump crosses two cells in one short arc, rather than walking onto the pit.
      float progress = Math.min(1, actionTime / .9f);
      Point airborne =
          new Point(
              jumpStart.x() + (target.x() - jumpStart.x()) * progress,
              jumpStart.y()
                  + (target.y() - jumpStart.y()) * progress
                  + (float) Math.sin(progress * Math.PI) * 1.2f);
      if (!fits(from, airborne, false)
          || CollisionUtils.isCollidingWithOtherSolids(golem, airborne)) {
        if (returning) {
          actionTime -= delta;
          status = returnFeedback + " Nox wartet auf einen freien Rückweg.";
        } else finishAttempt(false, "Sprungbahn blockiert.");
        return;
      }
      position.position(airborne);
      PositionSync.syncPosition(golem);
      if (progress == 1) {
        position.position(target);
        PositionSync.syncPosition(golem);
      }
      return;
    }
    stalled = Point.calculateDistance(from, lastPosition) < 0.001f ? stalled + delta : 0;
    lastPosition = from;
    if (stalled > 2f) {
      if (breakingGate || workshopTransit || returning) {
        status = waitingStatus();
        stalled = 0;
        pause = .5f;
        return;
      }
      route.clear();
      busy = false;
      status = "Bewegung unterbrochen. Laufweg blockiert.";
      if (attempt != null) finishAttempt(false, status);
      else notifyMovementFailure(status);
      return;
    }
    float distance = Point.calculateDistance(from, target);
    float speed = decisions.active() ? DECISION_SPEED : SPEED;
    float fraction = distance <= speed * delta ? 1 : speed * delta / distance;
    Point next =
        from.translate((target.x() - from.x()) * fraction, (target.y() - from.y()) * fraction);
    if (breakingGate && touchesGate("departure-gate", next)) SoulweaverGates.departure(level, true);
    if (breakingGate && !wallBroken && touchesGate("act1-gate", next)) {
      SoulweaverGates.open(level, 1);
      wallBroken = true;
      wallBreakTime = 0.6f;
      wall.forEach(
          draw -> {
            draw.tintColor(-1);
            draw.stateMachine().setState("breaking", null);
          });
    }
    if (!fits(from, next, breakingGate) || CollisionUtils.isCollidingWithOtherSolids(golem, next)) {
      if (breakingGate || workshopTransit || returning) {
        status = waitingStatus();
        pause = .5f;
        return;
      }
      route.clear();
      busy = false;
      status = "Wand oder Hindernis erreicht. Bewegung gestoppt.";
      if (attempt != null) finishAttempt(false, status);
      else notifyMovementFailure(status);
      return;
    }
    if (fraction == 1) {
      // Settle the last sub-tick distance exactly. VelocitySystem discards small velocities.
      // Check the swept tile rectangle and the destination's solids before settling.
      if (fits(from, target, breakingGate)
          && !CollisionUtils.isCollidingWithOtherSolids(golem, target)) {
        position.position(target);
        PositionSync.syncPosition(golem);
      }
      return;
    }
    velocity.currentVelocity(from.vectorTo(next).scale(1f / delta));
  }

  /**
   * @return status while a movement that must not fail waits for a blocked path
   */
  private String waitingStatus() {
    return returning
        ? returnFeedback + " Nox wartet auf einen freien Rückweg."
        : workshopTransit
            ? "Nox wartet auf einen freien Weg in der Werkstatt."
            : "Nox wartet auf einen freien Weg zur Schleuse.";
  }

  /**
   * Finds a route by testing the swept floor footprint between grid anchors.
   *
   * @param start the current golem position
   * @param target the destination grid anchor
   * @return ordered waypoints, or an empty list if no movement is possible or needed
   */
  private List<Point> path(Point start, Point target) {
    Coordinate destination = target.toCoordinate();
    Map<Coordinate, Coordinate> previous = new HashMap<>();
    ArrayDeque<Coordinate> queue = new ArrayDeque<>();
    // An interrupted step can stop between cells. Connect it to a reachable grid anchor,
    // including that short physical movement even when the destination is in the same cell.
    for (int x : new int[] {(int) Math.floor(start.x()), (int) Math.ceil(start.x())}) {
      for (int y : new int[] {(int) Math.floor(start.y()), (int) Math.ceil(start.y())}) {
        Coordinate anchor = new Coordinate(x, y);
        if (!previous.containsKey(anchor) && fits(start, new Point(x, y), breakingGate)) {
          queue.addLast(anchor);
          previous.put(anchor, anchor);
        }
      }
    }
    while (!queue.isEmpty()) {
      Coordinate at = queue.removeFirst();
      if (at.equals(destination)) break;
      for (int[] offset : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
        Coordinate next = new Coordinate(at.x() + offset[0], at.y() + offset[1]);
        if (!previous.containsKey(next)
            && fits(new Point(at.x(), at.y()), new Point(next.x(), next.y()), breakingGate)) {
          previous.put(next, at);
          queue.addLast(next);
        }
      }
    }
    if (!previous.containsKey(destination)) return List.of();
    List<Point> points = new ArrayList<>();
    for (Coordinate at = destination; ; at = previous.get(at)) {
      Point point = new Point(at.x(), at.y());
      if (!point.equals(start)) points.add(point);
      if (at.equals(previous.get(at))) break;
    }
    Collections.reverse(points);
    return points;
  }

  private boolean fits(Point from, Point to, boolean allowGate) {
    Rectangle footprint = footprint(from, to);
    float minX = footprint.x();
    float minY = footprint.y();
    float maxX = minX + footprint.width() - 0.001f;
    float maxY = minY + footprint.height() - 0.001f;
    for (int y = (int) Math.floor(minY); y <= Math.floor(maxY); y++) {
      for (int x = (int) Math.floor(minX); x <= Math.floor(maxX); x++) {
        if (allowGate && (inGate("act1-gate", x, y) || inGate("departure-gate", x, y))) continue;
        if (!level.tileAt(new Coordinate(x, y)).map(Tile::isAccessible).orElse(false)) return false;
      }
    }
    // Static furniture belongs in route planning; players remain movable physical obstacles.
    return Game.levelEntities(Set.of(CollideComponent.class, PositionComponent.class))
        .noneMatch(
            entity -> {
              if (entity == golem) return false;
              CollideComponent solid = entity.fetch(CollideComponent.class).orElseThrow();
              if (!solid.isSolid() || !solid.isStatic(entity)) return false;
              PositionComponent at = entity.fetch(PositionComponent.class).orElseThrow();
              var body = solid.collider();
              return footprint.intersects(
                  new Rectangle(
                      body.width() * at.scale().x(),
                      body.height() * at.scale().y(),
                      at.position().x() + body.left() * at.scale().x(),
                      at.position().y() + body.bottom() * at.scale().y()));
            });
  }

  /**
   * Derives movement and gate bounds from the actual collider and sprite scale.
   *
   * @param from the beginning of the movement segment
   * @param to the end of the movement segment
   * @return the swept floor rectangle
   */
  private Rectangle footprint(Point from, Point to) {
    var body = collision.collider();
    Vector2 scale = position.scale();
    return new Rectangle(
        body.width() * scale.x() + Math.abs(to.x() - from.x()),
        body.height() * scale.y() + Math.abs(to.y() - from.y()),
        Math.min(from.x(), to.x()) + body.left() * scale.x(),
        Math.min(from.y(), to.y()) + body.bottom() * scale.y());
  }

  /**
   * @param gate named-point prefix of a breakable gate
   * @param point golem position to test
   * @return whether the golem footprint at that position overlaps the gate tiles
   */
  private boolean touchesGate(String gate, Point point) {
    Rectangle bounds = footprint(point, point);
    for (int y = (int) Math.floor(bounds.y());
        y <= Math.floor(bounds.y() + bounds.height() - 0.001f);
        y++)
      for (int x = (int) Math.floor(bounds.x());
          x <= Math.floor(bounds.x() + bounds.width() - 0.001f);
          x++) if (inGate(gate, x, y)) return true;
    return false;
  }

  private boolean inGate(String gate, int x, int y) {
    Point a = level.namedPoints().get(gate + "-start");
    Point b = level.namedPoints().get(gate + "-end");
    return a != null
        && b != null
        && x >= Math.min(a.x(), b.x())
        && x <= Math.max(a.x(), b.x())
        && y >= Math.min(a.y(), b.y())
        && y <= Math.max(a.y(), b.y());
  }

  SoulweaverHelp help() {
    return help;
  }

  String helpPuzzle() {
    if (decisions.active()) return "decisions";
    if (controller.phase() == SoulweaverPhase.METHODS) return "methods";
    if (controller.phase() == SoulweaverPhase.LOOPS) return "cellar-" + controller.completedLoops();
    return controller.variableStage() == VariablePuzzleStage.VESSELS ? "vessels" : "essences";
  }

  boolean helpReady() {
    if (decisions.active()) return decisions.choosing();
    if (controller.phase() == SoulweaverPhase.METHODS) return workshop.helpReady();
    if (busy) return false;
    return controller.phase() == SoulweaverPhase.LOOPS ? mazeReady : !bindingState().revealed();
  }

  boolean helpSolvable() {
    return controller.phase() != SoulweaverPhase.VARIABLES
        || (controller.variableStage() == VariablePuzzleStage.VESSELS
            ? vesselsCollected
            : essencesCollected);
  }

  String helpStatus() {
    if (decisions.active()) return decisions.helpStatus();
    if (controller.phase() == SoulweaverPhase.METHODS) return workshop.helpStatus();
    if (controller.phase() == SoulweaverPhase.VARIABLES) return bindingFeedback;
    return status;
  }

  boolean helpAuthorized(Entity who) {
    return switch (controller.phase()) {
      case VARIABLES -> authorized(who, "variables-golem", 4.5f);
      case LOOPS -> authorized(who, "loop-terminal", 3f);
      case METHODS -> workshop.helpAuthorized(who);
      case DECISIONS -> decisions.helpAuthorized(who);
      case COMPLETE -> false;
    };
  }

  boolean helpSolveAuthorized(Entity who) {
    return helpReady()
        && helpAuthorized(who)
        && (controller.phase() != SoulweaverPhase.METHODS || workshop.helpSolveAuthorized(who));
  }

  void solveHelp(Entity who) {
    if (!helpSolveAuthorized(who) || !helpSolvable()) return;
    switch (controller.phase()) {
      case VARIABLES -> {
        boolean vessel = controller.variableStage() == VariablePuzzleStage.VESSELS;
        Map<GolemProperty, ? extends Enum<?>> solution =
            vessel ? VariablePuzzle.vesselSolution() : VariablePuzzle.essenceSolution();
        // The attempt precedes the assignments, which already solve the puzzle.
        recordBindingSolution(who, vessel, solution);
        solvingBinding = true;
        try {
          solution.forEach(
              (property, value) -> assignBinding(who, property.name(), value.name(), vessel));
        } finally {
          solvingBinding = false;
        }
      }
      case LOOPS -> {
        String rune = SoulweaverHelp.recommendedRune(controller.completedLoops());
        if (!controller.collectedLoopRunes().contains(rune)) collectRune(rune, who, true);
        assistedLoops = true;
        if (!activeRune.isEmpty()) removeRune(activeRune, who);
        executeRune(rune, who, true);
      }
      case METHODS -> workshop.solveHelp(who);
      case DECISIONS -> decisions.solve(who);
      case COMPLETE -> {}
    }
  }

  /**
   * Records one manual assignment. Essences placed while vessels are still being assigned are not
   * attempts, because the essence puzzle has not started yet.
   *
   * @param who assigning player
   * @param property assigned golem property
   * @param selected selected vessel or essence name
   * @param vessel whether a vessel rather than an essence was assigned
   * @param failureReasons failed conditions of this assignment
   */
  private void recordBinding(
      Entity who,
      GolemProperty property,
      String selected,
      boolean vessel,
      List<String> failureReasons) {
    if (solvingBinding || !vessel && controller.variableStage() == VariablePuzzleStage.VESSELS)
      return;
    SoulweaverProgress.participant(who)
        .ifPresent(
            participant ->
                SoulweaverProgress.attempt(
                    vessel ? "vessels" : "essences",
                    property.name(),
                    vessel ? "vessel" : "essence",
                    property.name() + "=" + selected,
                    participant,
                    new engine.tracking.AttemptDetails(
                        help.level(vessel ? "vessels" : "essences"), false, failureReasons)));
  }

  /**
   * Records a confirmed automatic binding solution as one attempt and one journal line.
   *
   * @param who player who confirmed the solution
   * @param vessel whether the vessel stage was solved
   * @param solution assigned value per property
   */
  private void recordBindingSolution(
      Entity who, boolean vessel, Map<GolemProperty, ? extends Enum<?>> solution) {
    String puzzle = vessel ? "vessels" : "essences";
    String answer =
        solution.entrySet().stream()
            .map(entry -> entry.getKey().name() + "=" + entry.getValue().name())
            .collect(java.util.stream.Collectors.joining(","));
    SoulweaverProgress.participant(who)
        .ifPresent(
            participant ->
                SoulweaverProgress.attempt(
                    puzzle,
                    "solution",
                    vessel ? "vessel" : "essence",
                    answer,
                    participant,
                    new engine.tracking.AttemptDetails(help.level(puzzle), true, List.of())));
    logBinding(vessel, "Lösung eingesetzt", "Hilfe");
  }

  private void logBinding(boolean vessel, String entry, String outcome) {
    if (solvingBinding) return;
    SoulweaverProgress.log(
        vessel ? "vessels" : "essences",
        vessel ? "Gefäße zuordnen" : "Essenzen einsetzen",
        entry + " · " + outcome);
  }

  private void recordLoopOutcome(List<String> failureReasons) {
    SoulweaverProgress.log(
        attemptPuzzle,
        "Kellerauftrag " + (Integer.parseInt(attemptPuzzle.substring(7)) + 1),
        LoopPuzzle.rune(activeRune).map(SoulweaverProgress::runeTitle).orElse(activeRune)
            + " · "
            + (failureReasons.isEmpty() ? "Ziel erreicht" : failureReasons.getFirst())
            + (attemptAutomaticSolution ? " (Hilfe)" : ""));
    if (attemptParticipant != null)
      SoulweaverProgress.attempt(
          attemptPuzzle,
          activeRune,
          "code",
          attemptCode,
          attemptParticipant,
          new engine.tracking.AttemptDetails(
              attemptHintLevel, attemptAutomaticSolution, failureReasons));
  }

  private String currentChallenge() {
    return LoopPuzzle.challenges().get(controller.completedLoops());
  }

  static void showText(Entity who, String title, String message) {
    showText(who, title + "\n\n" + message);
  }

  static void showText(Entity who, String message) {
    var context =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.DIALOG_DIALOG)
            .put(DialogContextKeys.DIALOG, message)
            .build();
    var ui = DialogFactory.show(context, false, true, who.id());
    ui.registerCallback(DialogContextKeys.ON_CONFIRM, ignored -> UIUtils.closeDialog(ui));
  }

  private void text(Entity who, String message) {
    showText(who, "Nox · Steuerungsstatus", message);
  }
}
