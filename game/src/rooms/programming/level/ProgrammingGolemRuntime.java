package rooms.programming.level;

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
import feature.collision.CollisionUtils;
import feature.components.CollideComponent;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;
import feature.utils.EntityUtils;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import rooms.programming.ProgrammingAchievements;
import rooms.programming.ProgrammingRoomController;
import rooms.programming.PuzzleSubmissionResult;
import rooms.programming.modules.loops.LoopExecution;
import rooms.programming.modules.loops.LoopMaze;
import rooms.programming.modules.loops.LoopProgram;
import rooms.programming.modules.loops.LoopPuzzle;
import rooms.programming.modules.loops.TerminalState;
import rooms.programming.modules.variables.BindingState;
import rooms.programming.modules.variables.GolemProperty;
import rooms.programming.modules.variables.MagicalEssence;
import rooms.programming.modules.variables.SoulVessel;
import rooms.programming.modules.variables.VariablePuzzle;
import rooms.programming.state.ProgrammingPhase;
import rooms.programming.state.VariablePuzzleStage;

/** Server-owned assignment dialogs and collision-checked movement of the one shared golem. */
final class ProgrammingGolemRuntime {
  private static final float SPEED = 2.5f;
  private final DungeonLevel level;
  private final ProgrammingRoomController controller = new ProgrammingRoomController();
  private final PositionComponent position;
  private final VelocityComponent velocity;
  private final CollideComponent collision;
  private final Map<GolemProperty, SoulVessel> vessels = new EnumMap<>(GolemProperty.class);
  private final Map<GolemProperty, MagicalEssence> essences = new EnumMap<>(GolemProperty.class);
  private final ArrayDeque<Point> route = new ArrayDeque<>();
  private final Entity golem;
  private final ProgrammingCellarMachinery machinery;
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
  private boolean propertiesCollected;
  private boolean vesselsCollected;
  private int loopFailures;
  private int checkpointFailures;
  private String bindingFeedback =
      "Eigenschaftsrunen und Gefäße fehlen. Öffne die beiden Werkstattkisten.";

  ProgrammingGolemRuntime(DungeonLevel level, Entity golem) {
    this.level = level;
    this.golem = golem;
    position = golem.fetch(PositionComponent.class).orElseThrow();
    velocity = golem.fetch(VelocityComponent.class).orElseThrow();
    collision = golem.fetch(CollideComponent.class).orElseThrow();
    wall = ProgrammingProps.wall(level);
    machinery = new ProgrammingCellarMachinery(level, golem);
  }

  void show(Entity who) {
    if (!authorized(who, "variables-golem", 4.5f)) return;
    if (busy) {
      text(who, status);
    } else if (controller.phase() == ProgrammingPhase.VARIABLES) {
      ProgrammingBinding.open(who, this);
    } else {
      showTerminal(who);
    }
  }

  BindingState bindingState() {
    return new BindingState(
        controller.variableStage(),
        propertiesCollected,
        vesselsCollected,
        vessels,
        essences,
        bindingFeedback);
  }

  void collectBindingSupply(boolean properties, Entity who) {
    String marker = properties ? "variables-properties" : "variables-vessels";
    if (!authorized(who, marker, 3f)) return;
    if (properties ? propertiesCollected : vesselsCollected) {
      showText(who, "Die Kiste ist leer.");
      return;
    }
    if (properties) propertiesCollected = true;
    else vesselsCollected = true;
    bindingFeedback =
        propertiesCollected && vesselsCollected
            ? "Gefäße zuordnen und Essenzen einsetzen. Valerius' Bindungsplan liegt beim Golem."
            : propertiesCollected ? "Gefäßvorrat fehlt." : "Eigenschaftsrunen fehlen.";
    showText(
        who,
        properties
            ? "Eigenschaftsrunen eingepackt. Valerius' Bindungsplan liegt beim Golem."
            : "Seelengefäße und Essenzen eingepackt. Jedes Gefäß trägt eine Prägung für seinen Inhalt. Der Vorrat reicht für mehrere Fassungen.");
  }

  void assignBinding(Entity who, String propertyName, String value, boolean vessel) {
    if (!authorized(who, "variables-golem", 4.5f)
        || busy
        || controller.phase() != ProgrammingPhase.VARIABLES
        || !propertiesCollected
        || !vesselsCollected) return;
    if (bindingState().revealed()
        || vessel && controller.variableStage() != VariablePuzzleStage.VESSELS) return;
    try {
      GolemProperty property = GolemProperty.valueOf(propertyName);
      if (vessel) {
        SoulVessel selected = SoulVessel.valueOf(value);
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
          bindingFeedback = "Alle Gefäße eingesetzt. Fehlende Füllungen ergänzen.";
        }
      } else {
        MagicalEssence selected = MagicalEssence.valueOf(value);
        SoulVessel container = vessels.get(property);
        if (container == null || !VariablePuzzle.fits(container, selected)) {
          if (container != null) ProgrammingAchievements.WRONG_TYPE.unlock(who);
          bindingFeedback =
              selected.literal()
                  + " passt nicht in "
                  + (container == null
                      ? "diese Fassung."
                      : container.label() + ". " + container.capacity() + ".");
          return;
        }
        MagicalEssence previous = essences.put(property, selected);
        if (previous != null && previous != selected) ProgrammingAchievements.OVERWRITE.unlock(who);
        if (property == GolemProperty.ACTIVATED && selected == MagicalEssence.BOOLEAN_FALSE)
          ProgrammingAchievements.SLEEPY.unlock(who);
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
          ProgrammingAchievements.BOUND.unlock();
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
        || controller.phase() != ProgrammingPhase.VARIABLES) return;
    try {
      GolemProperty property = GolemProperty.valueOf(propertyName);
      if (bindingState().revealed()) return;
      if (essences.remove(property) == null
          && controller.variableStage() == VariablePuzzleStage.VESSELS) vessels.remove(property);
      bindingFeedback = property.label() + ": Fassung geleert.";
    } catch (IllegalArgumentException ignored) {
      // Ignore unknown property IDs.
    }
  }

  void showBindingBook(Entity who) {
    if (authorized(who, "variables-translation", 3f)) ProgrammingBindingBook.open(who);
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
    if (controller.activateGolem() != PuzzleSubmissionResult.ACCEPTED) {
      breakingGate = false;
      return;
    }
    status = "Aktivierung läuft.";
    ProgrammingBinding.closeAll();
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
                                  player
                                      .fetch(CollideComponent.class)
                                      .ifPresent(body -> body.collider().position(at.position()));
                                }
                              }));
          ProgrammingGates.departure(level, false);
          breakingGate = false;
          position.position(
              LoopMaze.world(
                  level.getPoint("maze-origin"), LoopMaze.checkpoints().getFirst().start()));
          collision.collider().position(position.position());
          mazeReady = true;
          Game.levelEntities()
              .filter(entity -> entity.name().equals("programming-loop-monitor"))
              .flatMap(entity -> entity.fetch(DrawComponent.class).stream())
              .forEach(draw -> draw.stateMachine().setState("active", null));
          face(LoopMaze.Direction.EAST);
          status = "Keller erreicht. Räumauftrag bereit.";
        });
  }

  TerminalState terminalState() {
    Point origin = level.getPoint("maze-origin");
    int checkpoint = controller.completedLoopChallenges().size();
    return new TerminalState(
        LoopPuzzle.runes().stream()
            .map(rune -> rune.id())
            .filter(controller.collectedLoopRunes()::contains)
            .toList(),
        checkpoint,
        golem.id(),
        Math.round((position.position().x() - origin.x()) / LoopMaze.CELL_WIDTH),
        Math.round((position.position().y() - origin.y()) / LoopMaze.CELL_HEIGHT),
        facing.name(),
        busy,
        mazeReady,
        activeRune,
        status,
        checkpoint,
        controller.phase() == ProgrammingPhase.METHODS);
  }

  void showTerminal(Entity who) {
    if (authorized(who, "loop-terminal", 3f)) ProgrammingTerminal.open(who, this);
  }

  void showObservation(Entity who) {
    if (mazeReady && authorized(who, "loop-monitor", 3f)) {
      ProgrammingObservation.open(who, this);
      ProgrammingAchievements.OBSERVER.unlock(who);
    }
  }

  boolean collectRune(String runeId, Entity who) {
    if (!authorized(who, "rune-" + runeId, 3f)) return false;
    if (controller.collectLoopRune(runeId) != PuzzleSubmissionResult.ACCEPTED) return false;
    int collected = controller.collectedLoopRunes().size();
    if (collected == 1) ProgrammingAchievements.FIRST_RUNE.unlock();
    if (collected == LoopPuzzle.runes().size()) ProgrammingAchievements.ARCHIVIST.unlock();
    return true;
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
    if (!authorized(who, "loop-terminal", 3f)
        || busy
        || !mazeReady
        || controller.phase() != ProgrammingPhase.LOOPS
        || !controller.collectedLoopRunes().contains(runeId)) return;
    var rune = LoopPuzzle.rune(runeId);
    if (rune.isEmpty()) return;
    int checkpoint = controller.completedLoopChallenges().size();
    if (Point.calculateDistance(
                position.position(),
                LoopMaze.world(
                    level.getPoint("maze-origin"), LoopMaze.checkpoints().get(checkpoint).start()))
            > .1f
        || facing != LoopMaze.checkpoints().get(checkpoint).facing()) return;
    attempt = new LoopExecution(checkpoint, rune.orElseThrow(), monsterAlive);
    activeRune = runeId;
    busy = true;
    advanceAction();
  }

  void removeRune(String runeId, Entity who) {
    if (!authorized(who, "loop-terminal", 3f) || busy || !activeRune.equals(runeId)) return;
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
      if (monsterAlive && !attempt.monsterAlive()) {
        monsterAlive = false;
        Game.levelEntities()
            .filter(entity -> entity.name().equals("programming-maze-monster"))
            .findFirst()
            .ifPresent(
                entity -> {
                  monster = entity;
                  Game.remove(entity);
                });
      }
    }
    if (instruction.from().equals(instruction.to())) {
      busy = true;
      pause = .45f;
      arrived = this::advanceAction;
    } else {
      jumping = instruction.action() == LoopProgram.Action.JUMP;
      jumpStart = position.position();
      actionTime = 0;
      move(
          List.of(LoopMaze.world(level.getPoint("maze-origin"), instruction.to())),
          () -> {
            jumping = false;
            golem.fetch(DrawComponent.class).ifPresent(draw -> draw.tintColor(-1));
            advanceAction();
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
                controller.completedLoopChallenges().size(),
                () -> {
                  controller.completeExecutedLoop(challenge);
                  if (controller.completedLoopChallenges().size() == 1)
                    ProgrammingAchievements.FIRST_ROUTE.unlock();
                  if (checkpointFailures > 0) ProgrammingAchievements.SECOND_TRY.unlock();
                  checkpointFailures = 0;
                  if (controller.phase() == ProgrammingPhase.METHODS) {
                    ProgrammingAchievements.CELLAR_CLEAR.unlock();
                    if (loopFailures == 0) ProgrammingAchievements.CLEAN_RUN.unlock();
                  }
                  busy = false;
                  status =
                      controller.phase() == ProgrammingPhase.METHODS
                          ? "Torwinde: Halterung gebrochen. Antrieb stillgelegt."
                          : "Räumauftrag erledigt. Nächste Arbeitsposition bereit.";
                });
          };
    } else {
      loopFailures++;
      checkpointFailures++;
      returnFeedback =
          reason.isEmpty()
              ? "Programm beendet. "
                  + (finished
                          .cell()
                          .equals(
                              LoopMaze.checkpoints()
                                  .get(controller.completedLoopChallenges().size())
                                  .goal())
                      ? "Blickrichtung falsch."
                      : "Zielmarke nicht erreicht.")
              : "Programm gestoppt. " + reason;
      status = returnFeedback + " Nox kehrt zurück.";
      returning = true;
      busy = true;
      pause = 1.5f;
      List<Point> retrace = new ArrayList<>();
      List<LoopMaze.Cell> history = finished.history();
      // Start with the last reached anchor: a physical collision may have interrupted a step.
      for (int i = history.size() - 1; i >= 0; i--)
        retrace.add(LoopMaze.world(level.getPoint("maze-origin"), history.get(i)));
      arrived = () -> move(retrace, this::resetAttempt);
    }
  }

  private void resetAttempt() {
    jumping = false;
    golem.fetch(DrawComponent.class).ifPresent(draw -> draw.tintColor(-1));
    int checkpoint = controller.completedLoopChallenges().size();
    Point home =
        LoopMaze.world(
            level.getPoint("maze-origin"), LoopMaze.checkpoints().get(checkpoint).start());
    position.position(home);
    collision.collider().position(home);
    face(LoopMaze.checkpoints().get(checkpoint).facing());
    if (checkpoint <= 2 && !monsterAlive) {
      monsterAlive = true;
      if (monster != null) Game.add(monster);
    }
    route.clear();
    busy = false;
    returning = false;
    status = returnFeedback + " Nox ist zurück an der Arbeitsposition.";
    if (activeRune.equals("archive-spin")) ProgrammingAchievements.SPIN.unlock();
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
    // LevelTick runs before VelocitySystem and MoveSystem. The latter owns physical movement.
    velocity.clearForces();
    velocity.currentVelocity(Vector2.ZERO);
    if (machinery.working()) {
      machinery.tick(1f / Game.frameRate());
      return;
    }
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
      if (!fits(from, airborne, false)) {
        if (returning) resetAttempt();
        else finishAttempt(false, "Sprungbahn blockiert.");
        return;
      }
      position.position(airborne);
      collision.collider().position(airborne);
      if (progress == 1) {
        position.position(target);
        collision.collider().position(target);
      }
      return;
    }
    stalled = Point.calculateDistance(from, lastPosition) < 0.001f ? stalled + delta : 0;
    lastPosition = from;
    if (stalled > 2f) {
      if (breakingGate) {
        status = "Nox wartet auf einen freien Weg zur Schleuse.";
        stalled = 0;
        pause = .5f;
        return;
      }
      route.clear();
      busy = false;
      status = "Bewegung unterbrochen. Laufweg blockiert.";
      if (attempt != null) finishAttempt(false, status);
      else if (returning) resetAttempt();
      return;
    }
    float distance = Point.calculateDistance(from, target);
    float fraction = distance <= SPEED * delta ? 1 : SPEED * delta / distance;
    Point next =
        from.translate((target.x() - from.x()) * fraction, (target.y() - from.y()) * fraction);
    if (breakingGate && touchesDeparture(next)) ProgrammingGates.departure(level, true);
    if (breakingGate && !wallBroken && touchesGate(next, 1)) {
      ProgrammingGates.open(level, 1);
      wallBroken = true;
      wallBreakTime = 0.6f;
      wall.forEach(
          draw -> {
            draw.tintColor(-1);
            draw.stateMachine().setState("breaking", null);
          });
    }
    if (!fits(from, next, breakingGate)) {
      if (breakingGate) {
        status = "Nox wartet auf einen freien Weg zur Schleuse.";
        pause = .5f;
        return;
      }
      route.clear();
      busy = false;
      status = "Bewegung unterbrochen. Nächster Schritt blockiert.";
      if (attempt != null) finishAttempt(false, status);
      else if (returning) resetAttempt();
      return;
    }
    if (fraction == 1) {
      // Settle the last sub-tick distance exactly. VelocitySystem discards small velocities.
      // Check the swept tile rectangle and the destination's solids before settling.
      if (fits(from, target, breakingGate)
          && !CollisionUtils.isCollidingWithOtherSolids(collision.collider(), target)) {
        position.position(target);
        collision.collider().position(target);
      }
      return;
    }
    velocity.currentVelocity(from.vectorTo(next).scale(1f / delta));
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
        if (allowGate && (inGate(x, y, 1) || inDeparture(x, y))) continue;
        if (!level.tileAt(new Coordinate(x, y)).map(Tile::isAccessible).orElse(false)) return false;
      }
    }
    // Static furniture belongs in route planning; players remain movable physical obstacles.
    return Game.levelEntities(Set.of(CollideComponent.class, PositionComponent.class))
        .noneMatch(
            entity -> {
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

  private boolean touchesGate(Point p, int act) {
    Rectangle bounds = footprint(p, p);
    for (int y = (int) Math.floor(bounds.y());
        y <= Math.floor(bounds.y() + bounds.height() - 0.001f);
        y++)
      for (int x = (int) Math.floor(bounds.x());
          x <= Math.floor(bounds.x() + bounds.width() - 0.001f);
          x++) if (inGate(x, y, act)) return true;
    return false;
  }

  private boolean touchesDeparture(Point point) {
    Rectangle bounds = footprint(point, point);
    for (int y = (int) Math.floor(bounds.y()); y <= Math.floor(bounds.y() + bounds.height()); y++)
      for (int x = (int) Math.floor(bounds.x()); x <= Math.floor(bounds.x() + bounds.width()); x++)
        if (inDeparture(x, y)) return true;
    return false;
  }

  private boolean inDeparture(int x, int y) {
    Point a = level.namedPoints().get("departure-gate-start");
    Point b = level.namedPoints().get("departure-gate-end");
    return a != null
        && b != null
        && x >= Math.min(a.x(), b.x())
        && x <= Math.max(a.x(), b.x())
        && y >= Math.min(a.y(), b.y())
        && y <= Math.max(a.y(), b.y());
  }

  private boolean inGate(int x, int y, int act) {
    Point a = level.namedPoints().get("act" + act + "-gate-start");
    Point b = level.namedPoints().get("act" + act + "-gate-end");
    return a != null
        && b != null
        && x >= Math.min(a.x(), b.x())
        && x <= Math.max(a.x(), b.x())
        && y >= Math.min(a.y(), b.y())
        && y <= Math.max(a.y(), b.y());
  }

  private String currentChallenge() {
    var completed = controller.completedLoopChallenges();
    return LoopPuzzle.challenges().stream()
        .filter(challenge -> !completed.contains(challenge))
        .findFirst()
        .orElseThrow();
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
