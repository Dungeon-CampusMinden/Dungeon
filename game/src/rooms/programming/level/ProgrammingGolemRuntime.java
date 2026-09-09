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
import rooms.programming.modules.loops.LoopPuzzle;
import rooms.programming.modules.loops.LoopType;
import rooms.programming.modules.variables.GolemProperty;
import rooms.programming.modules.variables.MagicalEssence;
import rooms.programming.modules.variables.SoulVessel;
import rooms.programming.modules.variables.VariablePuzzle;
import rooms.programming.state.ProgrammingPhase;
import rooms.programming.state.VariablePuzzleStage;

/** Server-owned assignment dialogs and collision-checked movement of the one shared golem. */
final class ProgrammingGolemRuntime {
  private static final int ITERATION_LIMIT = 24;
  private static final float SPEED = 2.5f;
  private final DungeonLevel level;
  private final ProgrammingRoomController controller = new ProgrammingRoomController();
  private final PositionComponent position;
  private final VelocityComponent velocity;
  private final CollideComponent collision;
  private final Map<GolemProperty, SoulVessel> vessels = new EnumMap<>(GolemProperty.class);
  private final Map<GolemProperty, MagicalEssence> essences = new EnumMap<>(GolemProperty.class);
  private final ArrayDeque<Point> route = new ArrayDeque<>();
  private final Map<String, ProgrammingLoopSituation> situations = new HashMap<>();
  private ProgrammingLoopSituation attempt;
  private LoopType executor;
  private Entity operator;
  private int iterations;
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
    position = golem.fetch(PositionComponent.class).orElseThrow();
    velocity = golem.fetch(VelocityComponent.class).orElseThrow();
    collision = golem.fetch(CollideComponent.class).orElseThrow();
    wall = ProgrammingProps.wall(level);
    LoopPuzzle.challenges()
        .forEach(
            challenge -> situations.put(challenge, new ProgrammingLoopSituation(level, challenge)));
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
    } else if (controller.phase() == ProgrammingPhase.LOOPS) {
      String id = currentChallenge();
      Point origin = level.getPoint("loop-" + id);
      if (Point.calculateDistance(position.position(), origin) > 0.1f) {
        choose(
            who,
            "Positionierung ausstehend.",
            List.of(ChoiceOption.of("Fortsetzen", "next")),
            ignored -> {
              if (busy
                  || controller.phase() != ProgrammingPhase.LOOPS
                  || !id.equals(currentChallenge())) return;
              travel(origin, who, () -> show(who));
            });
        return;
      }
      var collected = controller.collectedLoopRunes();
      var runes =
          LoopPuzzle.runes(id).stream().filter(rune -> collected.contains(rune.id())).toList();
      if (runes.isEmpty()) {
        text(
            who,
            "Steuerung unvollständig.\nRhythmusrune benötigt.\n\n"
                + passageName(id)
                + ": keine passende Rune verfügbar.");
        return;
      }
      choose(
          who,
          passageName(id)
              + "\n\n"
              + runes.size()
              + " von 3 Rhythmusrunen verfügbar.\nProgramm auswählen.",
          runes.stream()
              .map(
                  rune ->
                      ChoiceOption.of(
                          rune.type().name().toLowerCase().replace('_', '-'), rune.type().name()))
              .toList(),
          answer -> {
            if (busy
                || controller.phase() != ProgrammingPhase.LOOPS
                || !id.equals(currentChallenge())) return;
            try {
              preview(id, LoopType.valueOf(answer), who);
            } catch (IllegalArgumentException exception) {
              text(who, "Rune nicht erkannt.");
            }
          });
    } else {
      Point exit = level.getPoint("loop-exit");
      if (Point.calculateDistance(position.position(), exit) > 0.1f) {
        choose(
            who,
            "Positionierung ausstehend.",
            List.of(ChoiceOption.of("Fortsetzen", "next")),
            ignored -> {
              if (!busy) travel(exit, who, () -> {});
            });
      } else showText(who, "Ende der spielbaren Fassung. Akt 3 und 4 sind noch nicht verfügbar.");
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
    Point destination = level.getPoint("loop-" + LoopPuzzle.challenges().getFirst());
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
    move(path, () -> breakingGate = false);
  }

  private void preview(String id, LoopType type, Entity who) {
    if (!canExecute(id, type)) return;
    ProgrammingLoopSituation situation = situations.get(id);
    choose(
        who,
        situation.code(type) + "\n\n" + situation.explanation(),
        List.of(ChoiceOption.of("Ausführen", "run"), ChoiceOption.of("Andere Rune", "back")),
        answer -> {
          if (busy
              || controller.phase() != ProgrammingPhase.LOOPS
              || !id.equals(currentChallenge())) return;
          if (answer.equals("back")) show(who);
          else if (answer.equals("run")) execute(id, type, who);
        });
  }

  boolean collectRune(String runeId, Entity who) {
    if (controller.collectLoopRune(runeId) != PuzzleSubmissionResult.ACCEPTED) return false;
    showRune(runeId, who);
    return true;
  }

  private void showRune(String runeId, Entity who) {
    var rune = LoopPuzzle.rune(runeId).orElseThrow();
    showText(
        who,
        "Rhythmusrune aufgenommen",
        passageName(rune.challengeId())
            + " · "
            + rune.type().name().toLowerCase().replace('_', '-')
            + "\n\n"
            + situations.get(rune.challengeId()).code(rune.type())
            + "\n\nIm Runenvorrat verfügbar.");
  }

  private boolean canExecute(String id, LoopType type) {
    return !Game.isMultiplayerClient()
        && !busy
        && controller.phase() == ProgrammingPhase.LOOPS
        && id.equals(currentChallenge())
        && LoopPuzzle.runes(id).stream()
            .anyMatch(
                rune -> rune.type() == type && controller.collectedLoopRunes().contains(rune.id()));
  }

  private void execute(String id, LoopType type, Entity who) {
    if (!canExecute(id, type)) return;
    if (Point.calculateDistance(position.position(), situations.get(id).origin()) > 0.1f) {
      show(who);
      return;
    }
    attempt = situations.get(id);
    movementVersion++;
    executor = type;
    operator = who;
    iterations = 0;
    advanceIteration();
  }

  /** Evaluates only the next iteration against the world left by the previous action. */
  private void advanceIteration() {
    Point at = position.position();
    boolean condition = attempt.condition(executor, at, point -> fits(at, point, false));
    boolean act =
        switch (executor) {
          case WHILE -> condition;
          case DO_WHILE -> iterations == 0 || condition;
          case FOR -> iterations < attempt.count();
        };
    if (!act) {
      finishAttempt(attempt.objective(at), "Programm beendet. Schritte: " + iterations + ".");
      return;
    }
    if (iterations >= ITERATION_LIMIT) {
      finishAttempt(false, "Programm abgebrochen. Kein Halt erreicht.");
      return;
    }
    Point next = attempt.next(at);
    if (!fits(at, next, false)) {
      finishAttempt(false, "Programm abgebrochen. Nächster Schritt blockiert.");
      return;
    }
    status =
        "Programm läuft: "
            + executor.name().toLowerCase().replace('_', '-')
            + ". Schritt "
            + (iterations + 1)
            + ".";
    move(
        List.of(next),
        () -> {
          iterations++;
          advanceIteration();
        });
  }

  private void finishAttempt(boolean success, String reason) {
    ProgrammingLoopSituation finished = attempt;
    attempt = null;
    Entity who = operator;
    route.clear();
    if (success) {
      String id = currentChallenge();
      if (controller.completeExecutedLoop(id) != PuzzleSubmissionResult.ACCEPTED) {
        busy = false;
        return;
      }
      text(who, reason + "\nZielposition erreicht.");
      if (controller.phase() == ProgrammingPhase.METHODS) {
        travel(level.getPoint("loop-exit"), who, () -> status = "Wegprogramm abgeschlossen.");
      } else travel(level.getPoint("loop-" + currentChallenge()), who, () -> {});
    } else {
      text(
          who,
          reason
              + "\nZielposition nicht erreicht. Rücklauf zur Ausgangsposition.\nRune weiterhin verfügbar.");
      status = "Rücklauf zur Ausgangsposition.";
      busy = true;
      pause = 1.5f;
      arrived =
          () -> {
            travel(finished.origin(), who, () -> {});
          };
    }
  }

  private void travel(Point target, Entity who, Runnable then) {
    List<Point> path = path(position.position(), target);
    if (path.isEmpty() && Point.calculateDistance(position.position(), target) > 0.1f) {
      busy = false;
      text(who, "Positionierung unterbrochen. Laufweg blockiert.");
      return;
    }
    status = "Positionierung läuft.";
    move(path, then);
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
    if (pause > 0) {
      pause -= delta;
      return;
    }
    if (!route.isEmpty() && position.position().equals(route.peekFirst())) route.removeFirst();
    if (route.isEmpty()) {
      busy = false;
      Runnable callback = arrived;
      arrived = () -> {};
      callback.run();
      return;
    }
    Point target = route.peekFirst();
    Point from = position.position();
    stalled = Point.calculateDistance(from, lastPosition) < 0.001f ? stalled + delta : 0;
    lastPosition = from;
    if (stalled > 2f) {
      route.clear();
      busy = false;
      status = "Bewegung unterbrochen. Laufweg blockiert.";
      if (attempt != null) finishAttempt(false, status);
      return;
    }
    float distance = Point.calculateDistance(from, target);
    float fraction = distance <= SPEED * delta ? 1 : SPEED * delta / distance;
    Point next =
        from.translate((target.x() - from.x()) * fraction, (target.y() - from.y()) * fraction);
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
    if (!fits(from, next, false)) {
      route.clear();
      busy = false;
      status = "Bewegung unterbrochen. Nächster Schritt blockiert.";
      if (attempt != null) finishAttempt(false, status);
      return;
    }
    if (fraction == 1) {
      // Settle the last sub-tick distance exactly. VelocitySystem discards small velocities.
      // Check the swept tile rectangle and the destination's solids before settling.
      if (fits(from, target, false)
          && !CollisionUtils.isCollidingWithOtherSolids(collision.collider(), target)) {
        position.position(target);
        collision.collider().position(target);
      }
      return;
    }
    velocity.currentVelocity(from.vectorTo(next).scale(1f / delta));
  }

  /** Breadth-first search tests the swept floor footprint between grid anchors. */
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
        if (allowGate && inGate(x, y, 1)) continue;
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

  /** Derive all movement and gate bounds from the actual collider and sprite scale. */
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

  static String passageName(String id) {
    return switch (id) {
      case "forge-press" -> "Werkstattgang";
      case "bellows" -> "Aufstieg";
      case "chain-lift" -> "Westgang";
      case "cooling-channel" -> "Steinerne Kehre";
      case "heart-gate" -> "Ausgang";
      default -> throw new IllegalArgumentException("Unknown loop " + id);
    };
  }
}
