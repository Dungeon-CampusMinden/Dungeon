package rooms.programming.level;

import engine.Entity;
import engine.Game;
import engine.components.PositionComponent;
import engine.level.DungeonLevel;
import engine.utils.Point;
import feature.components.UIComponent;
import java.util.ArrayDeque;
import java.util.List;
import rooms.programming.modules.decisions.DecisionMaze;
import rooms.programming.modules.decisions.DecisionMaze.Side;
import rooms.programming.modules.decisions.DecisionMaze.Values;

/** Server-owned progression. A blocked segment is resumed from its actual physical position. */
final class ProgrammingDecisionRuntime {
  private final DungeonLevel level;
  private final Entity golem;
  private final ProgrammingGolemRuntime motion;
  private final ArrayDeque<Point> route = new ArrayDeque<>();
  private Values values = new Values(45, 70, 22);
  private int revision, junction, failures;
  private int driver = -1;
  private boolean active, moving, blocked, completed;
  private String feedback = "Sechs Entscheidungsrunen trennen Nox vom Herzfeuer.";
  private Runnable arrival = () -> {};

  ProgrammingDecisionRuntime(DungeonLevel level, Entity golem, ProgrammingGolemRuntime motion) {
    this.level = level;
    this.golem = golem;
    this.motion = motion;
    ProgrammingDecisions.reset();
    ProgrammingDecisionWorld.spawn(level);
    publish();
  }

  void start() {
    if (active) return;
    active = true;
    ProgrammingProgress.started(
        "decisions", "Verfolge die sechs Runen und führe Nox zum Herzfeuer.");
    feedback = "Nox betritt das Labyrinth. Steige mit E auf Nox auf.";
    var approach = motion.workshopPath(ProgrammingDecisionWorld.START);
    if (approach.isEmpty()
        && Point.calculateDistance(
                golem.fetch(PositionComponent.class).orElseThrow().position(),
                ProgrammingDecisionWorld.START)
            > .1f) {
      blocked = true;
      feedback = "Der Zugang ist blockiert. Räume den Weg frei und wähle Weiter.";
      publish();
      return;
    }
    var points = new java.util.ArrayList<>(approach);
    travel(
        points,
        () -> {
          feedback = "Steige mit E auf Nox auf und lies die erste Rune.";
          publish();
          if (driver >= 0) enter();
        });
  }

  void show(Entity who) {
    if (!active || !authorized(who) || Game.hud().blocksGameplayInput(who)) return;
    if (driver >= 0 && driver != who.id()) return;
    driver = who.id();
    publish();
    ProgrammingDecisions.open(who, state(), intent -> accept(who, intent));
    if (!moving
        && !blocked
        && !completed
        && Point.calculateDistance(
                golem.fetch(PositionComponent.class).orElseThrow().position(),
                ProgrammingDecisionWorld.START)
            < .1f) enter();
  }

  private void enter() {
    travel(
        List.of(ProgrammingDecisionWorld.junction(0)),
        () -> {
          feedback = "Lies von oben nach unten. Welcher Zweig f\u00fchrt zu LINKS oder RECHTS?";
          publish();
        });
  }

  private boolean authorized(Entity who) {
    return who != null
        && !Game.isMultiplayerClient()
        && Game.allPlayers().anyMatch(player -> player == who)
        && Game.levelEntities().anyMatch(entity -> entity == who)
        && who.fetch(PositionComponent.class)
            .map(
                at ->
                    driver == who.id()
                        || Point.calculateDistance(
                                at.position(),
                                golem.fetch(PositionComponent.class).orElseThrow().position())
                            <= 7f)
            .orElse(false);
  }

  void accept(Entity who, ProgrammingDecisions.Intent intent) {
    if (intent == null || !authorized(who) || driver != who.id() || revision != intent.revision())
      return;
    if ("RESUME".equals(intent.operation()) && blocked) {
      blocked = false;
      if (route.isEmpty()) {
        var approach = motion.workshopPath(ProgrammingDecisionWorld.START);
        if (approach.isEmpty()
            && Point.calculateDistance(
                    golem.fetch(PositionComponent.class).orElseThrow().position(),
                    ProgrammingDecisionWorld.START)
                > .1f) {
          blocked = true;
          publish();
          return;
        }
        feedback = "Nox setzt seinen Weg fort.";
        var points = new java.util.ArrayList<>(approach);
        points.add(ProgrammingDecisionWorld.junction(0));
        travel(
            points,
            () -> {
              feedback = "Die erste Rune wartet.";
              publish();
            });
      } else {
        feedback = "Nox setzt seinen Weg fort.";
        moving = true;
        publish();
        next();
      }
      return;
    }
    if (moving || blocked || completed) return;
    Side side;
    try {
      side = Side.valueOf(intent.operation());
    } catch (IllegalArgumentException | NullPointerException ignored) {
      return;
    }
    boolean correct = side == DecisionMaze.evaluate(junction, values);
    ProgrammingProgress.interaction("decisions", "junction-" + (junction + 1) + "-" + side, who);
    ProgrammingProgress.participant(who)
        .ifPresent(
            participant ->
                ProgrammingProgress.attempt(
                    "decisions",
                    "junction-" + (junction + 1),
                    "direction",
                    side.name(),
                    participant,
                    new engine.tracking.AttemptDetails(
                        0,
                        false,
                        correct
                            ? List.of()
                            : List.of("Ausgeführter Zweig führt zur anderen Tür."))));
    ProgrammingDecisionWorld.open(level, junction, side, correct);
    feedback =
        correct
            ? "Die Tür führt weiter. Nox folgt dem ausgeführten Zweig."
            : "Diese Tür führt zurück. Nox läuft durch den Rückgang zum START.";
    travel(
        ProgrammingDecisionWorld.route(junction, side, correct),
        () -> {
          if (correct) {
            var delta = DecisionMaze.delta(junction);
            values = values.plus(delta.kraft(), delta.energie(), delta.temperatur());
            feedback = DecisionMaze.event(junction++);
            if (junction == 6) {
              completed = true;
              motion.completeDecisions();
              feedback = "Das Herzfeuer brennt. Jede Entscheidung hat einen Zweig ausgeführt.";
              ProgrammingProgress.solved("decisions", feedback);
            }
          } else {
            ProgrammingDecisionWorld.reset(level);
            failures++;
            junction = 0;
            // Events accumulate on the actual carried values, including previously crossed runes.
            if (failures % 2 == 1) {
              values = values.plus(15, -15, 5);
              feedback =
                  "START · Kraftquelle KRAFT +15 · Rückweg ENERGIE -15 · Feuerrune TEMPERATUR +5";
            } else {
              values = values.plus(0, 10, -5);
              feedback = "START · Kristallquelle ENERGIE +10 · Eisrune TEMPERATUR -5";
            }
          }
          publish();
          if (!correct) {
            blocked = true;
            route.add(ProgrammingDecisionWorld.junction(0));
            arrival =
                () -> {
                  feedback = "Neue Werte, neue Entscheidung. Lies die Rune erneut.";
                  publish();
                };
            publish();
          }
        });
  }

  private void travel(List<Point> points, Runnable done) {
    route.clear();
    route.addAll(points);
    arrival = done;
    moving = true;
    blocked = false;
    publish();
    next();
  }

  private void next() {
    if (route.isEmpty()) {
      moving = false;
      arrival.run();
      return;
    }
    motion.moveWorkshop(
        List.of(route.peekFirst()),
        () -> {
          route.removeFirst();
          next();
        },
        reason -> {
          moving = false;
          blocked = true;
          feedback = reason + " Räume den Weg frei und wähle Weiter.";
          publish();
        });
  }

  void tick() {
    if (driver >= 0
        && Game.allPlayers().filter(player -> player.id() == driver).noneMatch(this::authorized)) {
      int previousDriver = driver;
      driver = -1;
      Game.levelEntities()
          .flatMap(entity -> entity.fetch(UIComponent.class).stream())
          .filter(ui -> ui.dialogContext().dialogType().type().equals(ProgrammingDecisions.ID))
          .filter(
              ui ->
                  java.util.Arrays.stream(ui.targetEntityIds())
                      .anyMatch(id -> id == previousDriver))
          .toList()
          .forEach(ui -> feature.hud.UIUtils.closeDialog(ui, true));
      publish();
    }
  }

  boolean active() {
    return active;
  }

  String feedback() {
    return feedback;
  }

  ProgrammingDecisions.State state() {
    return new ProgrammingDecisions.State(
        revision,
        golem.id(),
        driver,
        junction,
        failures,
        values,
        active,
        moving,
        blocked,
        completed,
        feedback);
  }

  private void publish() {
    revision++;
    ProgrammingDecisions.publish(state());
  }
}
