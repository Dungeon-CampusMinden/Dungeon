package rooms.programming.level;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.level.DungeonLevel;
import engine.utils.Direction;
import engine.utils.Point;
import feature.systems.PositionSync;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import rooms.programming.ProgrammingAchievements;
import rooms.programming.modules.loops.LoopMaze;
import rooms.programming.modules.methods.MethodsRoute;
import rooms.programming.modules.methods.MethodsWorkshop;

/** Owns workshop execution while the shared golem retains its collision-checked movement. */
final class ProgrammingWorkshopRuntime {
  private final DungeonLevel level;
  private final Entity golem;
  private final ProgrammingGolemRuntime motion;
  private final MethodsWorkshop workshop = new MethodsWorkshop();
  private final Set<Integer> introduced = new HashSet<>();
  private boolean executing;
  private int inventory;
  private Direction direction;
  private float pause;
  private boolean arrived;
  private boolean moving;
  private boolean awarded;
  private boolean sluiceClosed;
  private boolean assisted;
  private java.util.UUID runParticipant;
  private String runCode = "";
  private boolean runPending;
  private int runHintLevel;
  private boolean runAutomaticSolution;
  private static final tools.jackson.databind.json.JsonMapper JSON =
      tools.jackson.databind.json.JsonMapper.builder().build();

  ProgrammingWorkshopRuntime(DungeonLevel level, Entity golem, ProgrammingGolemRuntime motion) {
    this.level = level;
    this.golem = golem;
    this.motion = motion;
    ProgrammingMethods.reset();
    publish();
  }

  void arrive() {
    arrived = true;
    ProgrammingProgress.started(
        "methods", "Kürze das Werkstattprogramm mit Methoden und erledige alle Arbeitsstellen.");
    if (direction == null) face(ProgrammingWorkshopWorld.startFacing(0));
    publish();
  }

  void show(Entity player) {
    if (!motion.methodsActive() || !authorized(player)) return;
    if (!arrived && !workshop.state().busy()) {
      ProgrammingGolemRuntime.showText(player, "Nox ist auf dem Weg zur ersten Arbeitsstelle.");
      return;
    }
    if (claimEditor(player)) publish();
    ProgrammingMethods.open(player, workshop.state(), intent -> accept(player, intent));
  }

  private boolean authorized(Entity player) {
    return player != null
        && !Game.isMultiplayerClient()
        && Game.allPlayers().anyMatch(p -> p == player)
        && player
            .fetch(PositionComponent.class)
            .map(
                at ->
                    Point.calculateDistance(at.position(), level.getPoint("methods-console"))
                        <= 4.5f)
            .orElse(false);
  }

  /**
   * Opening players share one editor slot; an open observer takes over when it is free.
   *
   * @param player player eligible to claim the editor
   * @return whether the player acquired the free slot
   */
  private boolean claimEditor(Entity player) {
    var state = workshop.state();
    return state.editorId() < 0
        && !state.completed()
        && workshop.apply(
            player.id(),
            new MethodsWorkshop.Intent(
                state.revision(), state.stage(), MethodsWorkshop.Operation.CLAIM, ""));
  }

  private void accept(Entity player, MethodsWorkshop.Intent intent) {
    accept(player, intent, false);
  }

  private void accept(Entity player, MethodsWorkshop.Intent intent, boolean automaticSolution) {
    if (intent == null || !arrived || !motion.methodsActive() || !authorized(player)) return;
    if (intent.operation() == MethodsWorkshop.Operation.STOP) {
      if (workshop.stop(player.id(), intent)) {
        motion.stopWorkshopMovement();
        executing = false;
        moving = false;
        pause = 0;
        ProgrammingProgress.interaction("methods", "execute-stop", player);
        recordOutcome();
        golem.fetch(DrawComponent.class).ifPresent(draw -> draw.tintColor(-1));
      }
      publish();
      return;
    }
    if (intent.operation() != MethodsWorkshop.Operation.EXECUTE) {
      if (workshop.apply(player.id(), intent))
        ProgrammingProgress.interaction(
            "methods", intent.operation().name().toLowerCase(java.util.Locale.ROOT), player);
      publish();
      return;
    }
    var submitted = workshop.state();
    if (!workshop.execute(player.id(), intent)) return;
    runParticipant = ProgrammingProgress.participant(player).orElse(null);
    runHintLevel = motion.help().level("methods");
    runAutomaticSolution = automaticSolution;
    runCode =
        JSON.writeValueAsString(
            java.util.Map.of(
                "main",
                submitted.main(),
                "draft",
                submitted.draft(),
                "definitions",
                submitted.definitions()));
    runPending = true;
    ProgrammingProgress.interaction("methods", "execute", player);
    motion.stopWorkshopMovement();
    ProgrammingWorkshopWorld.resetAll();
    golem.fetch(PositionComponent.class).orElseThrow().position(ProgrammingWorkshopWorld.start(0));
    PositionSync.syncPosition(golem);
    face(ProgrammingWorkshopWorld.startFacing(0));
    inventory = 0;
    moving = false;
    pause = 0;
    executing = true;
    advance();
  }

  void tick() {
    Set<Integer> viewers = new HashSet<>();
    Game.levelEntities()
        .flatMap(entity -> entity.fetch(feature.components.UIComponent.class).stream())
        .filter(ui -> ui.dialogContext().dialogType().type().equals(ProgrammingMethods.ID))
        .forEach(ui -> java.util.Arrays.stream(ui.targetEntityIds()).forEach(viewers::add));
    int editor = workshop.state().editorId();
    boolean changed = false;
    if (editor >= 0
        && (!viewers.contains(editor)
            || Game.allPlayers().filter(p -> p.id() == editor).noneMatch(this::authorized)))
      changed = workshop.releaseEditor(editor);
    if (motion.methodsActive() && workshop.state().editorId() < 0)
      changed |=
          Game.allPlayers()
              .filter(player -> viewers.contains(player.id()))
              .filter(this::authorized)
              .min(java.util.Comparator.comparingInt(Entity::id))
              .map(this::claimEditor)
              .orElse(false);
    if (changed) publish();
    if (motion.methodsActive()) {
      Game.allPlayers()
          .filter(
              p ->
                  p.fetch(PositionComponent.class)
                      .map(at -> at.position().y() >= level.getPoint("methods-entry").y())
                      .orElse(false))
          .filter(p -> introduced.add(p.id()))
          .forEach(
              p ->
                  ProgrammingGolemRuntime.showText(
                      p,
                      ProgrammingStory.workshop()
                          + "\n\nNox: "
                          + ProgrammingStory.workshopArrival()));
    }
    if (!arrived) return;
    if (!sluiceClosed) {
      // Closing a remote sluice must never strand a player on its far side.
      boolean occupied =
          Game.allPlayers()
              .anyMatch(
                  p ->
                      p.fetch(PositionComponent.class)
                          .map(
                              at ->
                                  at.position().x()
                                          >= level.getPoint("departure-gate-start").x() - 1
                                      && at.position().y() < 20)
                          .orElse(false));
      if (!occupied) {
        ProgrammingGates.departure(level, false);
        sluiceClosed = true;
      }
    }
    if (executing && !moving && pause > 0) {
      pause -= 1f / Game.frameRate();
      if (pause <= 0) advance();
    }
  }

  private void advance() {
    if (!executing) return;
    var next = workshop.next();
    if (next.isEmpty()) {
      if (workshop.exhausted()) workshop.finish(ProgrammingWorkshopWorld.solved(), inventory);
      finish();
      return;
    }
    golem.fetch(DrawComponent.class).ifPresent(draw -> draw.tintColor(-1));
    var instruction = next.orElseThrow();
    switch (instruction.action()) {
      case TURN -> {
        face(ProgrammingWorkshopWorld.turn(direction, instruction.direction()));
        workshop.actionResult(true, 0, "");
        pause = .4f;
      }
      case MOVE -> {
        float distance = instruction.amount() * ProgrammingWorkshopWorld.TILE_STEP;
        Point destination =
            position().translate(direction.x() * distance, direction.y() * distance);
        moving = true;
        motion.moveWorkshop(
            List.of(destination),
            () -> {
              moving = false;
              workshop.actionResult(true, 0, "");
              advance();
            },
            reason -> {
              moving = false;
              workshop.actionResult(false, 0, reason);
              finish();
            });
      }
      case OPEN_GATE, ACTIVATE_RUNE, COLLECT, PLACE -> {
        if (instruction.action() == MethodsRoute.Action.PLACE
            && (instruction.amount() < 0 || instruction.amount() > inventory)) {
          workshop.actionResult(false, 0, "Nox hat nicht genug Kristalle für diese Ablage.");
          finish();
          return;
        }
        var result =
            ProgrammingWorkshopWorld.perform(
                instruction.action(), position(), instruction.amount());
        if (result.success()) {
          if (instruction.action() == MethodsRoute.Action.COLLECT) inventory += result.value();
          if (instruction.action() == MethodsRoute.Action.PLACE) inventory -= result.value();
          golem.fetch(DrawComponent.class).ifPresent(draw -> draw.tintColor(0xFFE2A8FF));
        }
        workshop.actionResult(result.success(), result.value(), result.reason());
        if (!workshop.state().busy()) {
          finish();
          return;
        }
        pause = .65f;
      }
      default -> throw new IllegalStateException("Interpreter returned a nonphysical action");
    }
    publish();
  }

  private void finish() {
    executing = false;
    moving = false;
    pause = 0;
    golem.fetch(DrawComponent.class).ifPresent(draw -> draw.tintColor(-1));
    recordOutcome();
    if (workshop.state().completed()) {
      ProgrammingProgress.solved(
          "methods",
          "Alle Arbeitsstellen und Programmprüfungen sind erfüllt. Der Nebenausgang ist offen.");
      ProgrammingWorkshopWorld.openExit();
      ProgrammingGates.open(level, 3);
      if (!awarded && !assisted) {
        awarded = true;
        (workshop.state().errors() == 0
                ? ProgrammingAchievements.METHODS_FLAWLESS
                : ProgrammingAchievements.METHODS_PERSISTENT)
            .unlock();
      }
    }
    publish();
  }

  boolean helpReady() {
    return arrived && !workshop.state().busy() && !workshop.state().completed();
  }

  String helpStatus() {
    return !arrived ? "Nox ist auf dem Weg zur Werkstatt." : workshop.state().feedback();
  }

  boolean helpAuthorized(Entity player) {
    return authorized(player);
  }

  boolean helpSolveAuthorized(Entity player) {
    return helpReady() && authorized(player) && workshop.state().editorId() == player.id();
  }

  void solveHelp(Entity player) {
    if (!helpSolveAuthorized(player) || !workshop.loadHelpSolution(player.id())) return;
    assisted = true;
    var state = workshop.state();
    accept(
        player,
        new MethodsWorkshop.Intent(
            state.revision(), state.stage(), MethodsWorkshop.Operation.EXECUTE, ""),
        true);
  }

  private void recordOutcome() {
    if (!runPending) return;
    runPending = false;
    if (runParticipant != null) {
      var state = workshop.state();
      List<String> reasons =
          state.completed()
              ? List.of()
              : state.runState() == MethodsWorkshop.RunState.FINISHED
                  ? state.checks().stream()
                      .filter(check -> check.status() == MethodsWorkshop.CheckStatus.FAILED)
                      .map(MethodsWorkshop.Check::message)
                      .toList()
                  : List.of(state.feedback());
      ProgrammingProgress.attempt(
          "methods",
          "workshop-program",
          "code",
          runCode,
          runParticipant,
          new engine.tracking.AttemptDetails(runHintLevel, runAutomaticSolution, reasons));
    }
  }

  private void face(Direction facing) {
    direction = facing;
    motion.faceWorkshop(
        switch (direction) {
          case DOWN -> LoopMaze.Direction.SOUTH;
          case LEFT -> LoopMaze.Direction.WEST;
          case UP -> LoopMaze.Direction.NORTH;
          case RIGHT -> LoopMaze.Direction.EAST;
          case NONE -> throw new IllegalArgumentException("Workshop movement needs a direction");
        });
  }

  private Point position() {
    return golem.fetch(PositionComponent.class).orElseThrow().position();
  }

  private void publish() {
    var state = workshop.state();
    ProgrammingMethods.publish(state);
  }
}
