package rooms.programming.level;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.components.VelocityComponent;
import engine.level.DungeonLevel;
import engine.level.Tile;
import engine.level.utils.Coordinate;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.utils.Point;
import engine.utils.Rectangle;
import engine.utils.Vector2;
import feature.collision.CollisionUtils;
import feature.components.CollideComponent;
import feature.hud.UIUtils;
import feature.hud.dialogs.ChoiceOption;
import feature.hud.dialogs.ChoiceOptions;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import rooms.programming.ProgrammingRoomController;
import rooms.programming.PuzzleSubmissionResult;
import rooms.programming.modules.loops.LoopExecution;
import rooms.programming.modules.loops.LoopMaze;
import rooms.programming.modules.loops.LoopProgram;
import rooms.programming.modules.loops.LoopPuzzle;
import rooms.programming.modules.loops.TerminalState;
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
  private long movementVersion;
  private boolean breakingGate;
  private float pause;
  private String status = "Seelenbindung unvollständig.";

  ProgrammingGolemRuntime(DungeonLevel level, Entity golem) {
    this.level = level;
    this.golem = golem;
    position = golem.fetch(PositionComponent.class).orElseThrow();
    velocity = golem.fetch(VelocityComponent.class).orElseThrow();
    collision = golem.fetch(CollideComponent.class).orElseThrow();
    wall = ProgrammingProps.wall(level);
  }

  void show(Entity who) {
    if (busy) {
      text(who, status);
      return;
    }
    if (controller.phase() == ProgrammingPhase.VARIABLES) {
      if (controller.variableStage() == VariablePuzzleStage.REVEAL) {
        choose(
            who,
            "Seelenbindung vollständig.\n\n" + translation(),
            List.of(ChoiceOption.of("Aktivieren", "start")),
            ignored -> activate(who));
      } else {
        showProperties(who, ProgrammingStory.golem());
      }
    } else {
      showTerminal(who);
    }
  }

  private void showProperties(Entity who, String feedback) {
    if (controller.phase() != ProgrammingPhase.VARIABLES
        || controller.variableStage() == VariablePuzzleStage.REVEAL) {
      show(who);
      return;
    }
    VariablePuzzleStage stage = controller.variableStage();
    boolean vesselStage = stage == VariablePuzzleStage.VESSELS;
    List<ChoiceOption> options = new ArrayList<>();
    for (GolemProperty property : GolemProperty.values()) {
      String assigned =
          vesselStage
              ? (vessels.containsKey(property) ? vesselLabel(vessels.get(property)) : "offen")
              : (essences.containsKey(property) ? essences.get(property).literal() : "offen");
      options.add(ChoiceOption.of(propertyLabel(property) + ": " + assigned, property.name()));
    }
    choose(
        who,
        feedback
            + (vesselStage
                ? "\nEigenschaft auswählen und ein Gefäß zuordnen."
                : "\nGefäße zugeordnet. Essenzen einsetzen.\n\nEssenzvorrat: 125, 17, 3.5, true, false, \"Nox\", 'O'"),
        options,
        answer -> {
          if (controller.variableStage() != stage) {
            show(who);
            return;
          }
          try {
            showAssignment(who, GolemProperty.valueOf(answer), vesselStage);
          } catch (IllegalArgumentException exception) {
            text(who, "Unbekannte Eigenschaftsrune.");
          }
        });
  }

  private void showAssignment(Entity who, GolemProperty property, boolean vesselStage) {
    List<ChoiceOption> options = new ArrayList<>();
    if (vesselStage) {
      for (SoulVessel vessel : SoulVessel.values())
        options.add(ChoiceOption.of(vesselLabel(vessel), vessel.name()));
    } else {
      for (MagicalEssence essence : MagicalEssence.values())
        options.add(ChoiceOption.of(essence.literal(), essence.name()));
    }
    choose(
        who,
        propertyLabel(property)
            + (vesselStage ? "\nWähle ein Seelengefäß." : "\nWähle eine Essenz."),
        options,
        answer -> {
          VariablePuzzleStage expected =
              vesselStage ? VariablePuzzleStage.VESSELS : VariablePuzzleStage.ESSENCES;
          if (busy
              || controller.phase() != ProgrammingPhase.VARIABLES
              || controller.variableStage() != expected) {
            show(who);
            return;
          }
          boolean correct;
          try {
            if (vesselStage) {
              SoulVessel selected = SoulVessel.valueOf(answer);
              correct = VariablePuzzle.vesselSolution().get(property) == selected;
              if (correct) vessels.put(property, selected);
              if (vessels.size() == GolemProperty.values().length)
                controller.submitVessels(vessels);
            } else {
              MagicalEssence selected = MagicalEssence.valueOf(answer);
              correct = VariablePuzzle.essenceSolution().get(property) == selected;
              if (correct) essences.put(property, selected);
              if (essences.size() == GolemProperty.values().length)
                controller.submitEssences(essences);
            }
          } catch (IllegalArgumentException exception) {
            text(who, "Diese Zuordnung ist unbekannt.");
            return;
          }
          showProperties(
              who, correct ? "Zuordnung übernommen." : "Unpassende Zuordnung. Nicht übernommen.");
        });
  }

  void showTranslation(Entity who) {
    showText(
        who,
        (controller.variableStage() == VariablePuzzleStage.REVEAL
                || controller.variableStage() == VariablePuzzleStage.COMPLETE)
            ? "Valerius' Gefäßverzeichnis\n\n" + translation()
            : "Gefäßverzeichnis\n\nDie Fachbezeichnungen lassen sich erst an einer vollständigen Seelenbindung ablesen.\n\nValerius");
  }

  private String translation() {
    return "Eisenkiste → int\nKristallflasche → double\nPergament → String\nRunenstein → char\nLichtkugel → boolean"
        + "\n\nNox: Lebensenergie 125, Mana 3.5, Aktiviert true, Blickrichtung 'O', Schritte 17.";
  }

  private void activate(Entity who) {
    if (Game.isMultiplayerClient()
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
          face(LoopMaze.Direction.EAST);
          status = "Terminal bereit. Rune einsetzen und ausführen.";
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
    if (mazeReady && authorized(who, "loop-monitor", 3f)) ProgrammingObservation.open(who, this);
  }

  boolean collectRune(String runeId, Entity who) {
    if (!authorized(who, "rune-" + runeId, 3f)) return false;
    return controller.collectLoopRune(runeId) == PuzzleSubmissionResult.ACCEPTED;
  }

  private boolean authorized(Entity who, String marker, float range) {
    if (Game.isMultiplayerClient()
        || who == null
        || Game.allPlayers().noneMatch(player -> player == who)) return false;
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
    movementVersion++;
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
      controller.completeExecutedLoop(currentChallenge());
      busy = false;
      status = "Wegzeichen erreicht. Nächster Abschnitt bereit.";
      if (controller.phase() == ProgrammingPhase.METHODS) {
        ProgrammingGates.open(level, 2);
        status = "Labyrinth abgeschlossen. Das Tor zu Akt 3 ist offen.";
      }
    } else {
      status =
          "Rune ungültig. "
              + (reason.isEmpty() ? "Zielposition oder Blickrichtung nicht erreicht." : reason)
              + " Nox kehrt zurück.";
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
    status = "Rune ungültig. Nox ist zurück am Wegzeichen.";
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
    movementVersion++;
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

  private void choose(
      Entity who, String question, List<ChoiceOption> options, Consumer<String> accept) {
    long version = movementVersion;
    choose(
        who,
        "Nox · Seelenkern",
        question,
        options,
        true,
        answer -> {
          if (version == movementVersion) accept.accept(answer);
        });
  }

  private static void choose(
      Entity who,
      String title,
      String question,
      List<ChoiceOption> options,
      boolean canCancel,
      Consumer<String> accept) {
    var context =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.MULTIPLE_CHOICE)
            .put(DialogContextKeys.TITLE, title)
            .put(DialogContextKeys.DIALOG, question)
            .put(DialogContextKeys.OPTIONS, new ChoiceOptions(options))
            .put(DialogContextKeys.CAN_CANCEL, canCancel)
            .build();
    // Reading a rune must not suspend the shared simulation or Nox's return timer.
    var ui = DialogFactory.show(context, false, true, who.id());
    if (canCancel)
      ui.registerCallback(DialogContextKeys.ON_CANCEL, ignored -> UIUtils.closeDialog(ui));
    ui.registerCallback(
        DialogContextKeys.ON_OPTION_SELECTED,
        payload -> {
          if (payload instanceof DialogResponseMessage.StringValue(String value)
              && options.stream().anyMatch(option -> option.value().equals(value))) {
            UIUtils.closeDialog(ui);
            accept.accept(value);
          }
        });
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

  private static String propertyLabel(GolemProperty property) {
    return switch (property) {
      case NAME -> "Name";
      case LIFE_ENERGY -> "Lebensenergie";
      case MANA -> "Mana";
      case ACTIVATED -> "Aktiviert";
      case VIEW_DIRECTION -> "Blickrichtung";
      case STEPS -> "Schritte";
    };
  }

  private static String vesselLabel(SoulVessel vessel) {
    return switch (vessel) {
      case IRON_CHEST -> "Eisenkiste";
      case CRYSTAL_BOTTLE -> "Kristallflasche";
      case PARCHMENT -> "Pergament";
      case RUNE_STONE -> "Runenstein";
      case LIGHT_ORB -> "Lichtkugel";
    };
  }
}
