package rooms.programming.level;

import engine.Entity;
import engine.Game;
import engine.components.PositionComponent;
import engine.level.DungeonLevel;
import engine.utils.Point;
import feature.components.UIComponent;
import java.util.ArrayDeque;
import java.util.HashSet;
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
    ProgrammingDecisionWorld.spawn(level, this);
    publish();
  }

  void start() {
    if (active) return;
    active = true;
    ProgrammingProgress.started(
        "decisions", "Verfolge die sechs Runen und führe Nox zum Herzfeuer.");
    feedback = "Nox betritt das Labyrinth. Das Runenbuch steht am START.";
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
    points.add(ProgrammingDecisionWorld.junction(0));
    travel(
        points,
        () -> {
          feedback = "Lies von oben nach unten. Welcher Zweig führt zu LINKS oder RECHTS?";
          publish();
        });
  }

  void show(Entity who) {
    if (!active || !authorized(who)) return;
    if (driver < 0) {
      driver = who.id();
      publish();
    }
    ProgrammingDecisions.open(who, state(), intent -> accept(who, intent));
  }

  private boolean authorized(Entity who) {
    return who != null
        && !Game.isMultiplayerClient()
        && Game.allPlayers().anyMatch(p -> p == who)
        && who.fetch(PositionComponent.class)
            .map(
                at ->
                    viewing(who)
                        || Point.calculateDistance(
                                at.position(), level.getPoint("decisions-console"))
                            <= 5f
                        || Point.calculateDistance(
                                at.position(),
                                golem.fetch(PositionComponent.class).orElseThrow().position())
                            <= 7f
                        || completed
                            && Point.calculateDistance(
                                    at.position(), level.getPoint("decisions-heart"))
                                <= 6f)
            .orElse(false);
  }

  // A camera observer stays authorized after Nox walks away from the opening position.
  private boolean viewing(Entity who) {
    return Game.levelEntities()
        .flatMap(e -> e.fetch(UIComponent.class).stream())
        .filter(ui -> ui.dialogContext().dialogType().type().equals(ProgrammingDecisions.ID))
        .anyMatch(
            ui -> java.util.Arrays.stream(ui.targetEntityIds()).anyMatch(id -> id == who.id()));
  }

  void accept(Entity who, ProgrammingDecisions.Intent intent) {
    if (intent == null || !authorized(who) || driver != who.id() || revision != intent.revision())
      return;
    if ("RELEASE".equals(intent.operation())) {
      driver = -1;
      publish();
      return;
    }
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
    ProgrammingDecisionWorld.open(level, junction, side);
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
    var viewers = new HashSet<Integer>();
    Game.levelEntities()
        .flatMap(e -> e.fetch(UIComponent.class).stream())
        .filter(ui -> ui.dialogContext().dialogType().type().equals(ProgrammingDecisions.ID))
        .forEach(ui -> java.util.Arrays.stream(ui.targetEntityIds()).forEach(viewers::add));
    if (!viewers.contains(driver)
        || Game.allPlayers().filter(p -> p.id() == driver).noneMatch(this::authorized)) {
      int nextDriver =
          Game.allPlayers()
              .filter(p -> viewers.contains(p.id()))
              .filter(this::authorized)
              .mapToInt(Entity::id)
              .min()
              .orElse(-1);
      if (driver != nextDriver) {
        driver = nextDriver;
        publish();
      }
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
