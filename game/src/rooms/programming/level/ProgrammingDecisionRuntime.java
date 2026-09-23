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
  final ProgrammingEnding ending;
  private final ArrayDeque<Point> route = new ArrayDeque<>();
  private Values values = DecisionMaze.start(0);
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
    ending = new ProgrammingEnding(ProgrammingDecisionWorld.spawn(level), () -> completed);
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
    if (completed) {
      ProgrammingGolemRuntime.showText(who, feedback);
      return;
    }
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
    if (!choosing()) return;
    Side side;
    try {
      side = Side.valueOf(intent.operation());
    } catch (IllegalArgumentException | NullPointerException ignored) {
      return;
    }
    choose(who, side, false);
  }

  /**
   * @return whether Nox waits at a junction for the next decision
   */
  boolean choosing() {
    return active && !moving && !blocked && !completed;
  }

  /**
   * @param who player requesting help
   * @return whether the player currently rides Nox and may use its help
   */
  boolean helpAuthorized(Entity who) {
    return driver >= 0 && driver == who.id() && authorized(who);
  }

  /**
   * Takes the executed branch at the current junction after a confirmed help request.
   *
   * @param who riding player
   */
  void solve(Entity who) {
    if (helpAuthorized(who) && choosing())
      choose(who, DecisionMaze.evaluate(junction, values), true);
  }

  private void choose(Entity who, Side side, boolean assisted) {
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
                        motion.help().level("decisions"),
                        assisted,
                        correct
                            ? List.of()
                            : List.of("Ausgeführter Zweig führt zur anderen Tür."))));
    ProgrammingProgress.log(
        "decisions",
        "Versuch " + (failures + 1),
        "Kreuzung "
            + (junction + 1)
            + " · K"
            + values.kraft()
            + " E"
            + values.energie()
            + " T"
            + values.temperatur()
            + " · "
            + (side == Side.LEFT ? "LINKS" : "RECHTS")
            + (correct ? " · richtig" : " · falsch, zurück zum START")
            + (assisted ? " (Hilfe)" : ""));
    ProgrammingDecisionWorld.open(level, junction, side, correct);
    feedback =
        correct
            ? "Die Tür führt weiter. Nox folgt dem ausgeführten Zweig."
            : "Diese Tür führt zurück. Nox läuft durch den Rückgang zum START.";
    travel(
        ProgrammingDecisionWorld.route(junction, side, correct),
        () -> {
          if (correct) {
            var delta = DecisionMaze.delta(junction++);
            values = values.plus(delta.kraft(), delta.energie(), delta.temperatur());
            feedback = "";
            if (junction == 6) {
              completed = true;
              motion.completeDecisions();
              feedback =
                  "Das Herzfeuer ist erreicht. Lies die Schriftrolle vor dem Feuer, "
                      + "um die Opfergabe darzubringen und den Raum abzuschließen.";
              ProgrammingProgress.solved("decisions", feedback);
              // The ride ends at the goal; the rider walks to the inscription on foot.
              int rider = driver;
              dismount();
              Game.findEntityById(rider)
                  .ifPresent(player -> ProgrammingGolemRuntime.showText(player, feedback));
            }
          } else {
            ProgrammingDecisionWorld.reset(level);
            failures++;
            junction = 0;
            values = DecisionMaze.start(failures);
            feedback = "Zurück am START. Nox trägt neue Startwerte.";
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
      dismount();
      publish();
    }
  }

  /** Frees the seat and closes the former rider's controls; the caller publishes the change. */
  private void dismount() {
    int previousDriver = driver;
    driver = -1;
    Game.levelEntities()
        .flatMap(entity -> entity.fetch(UIComponent.class).stream())
        .filter(ui -> ui.dialogContext().dialogType().type().equals(ProgrammingDecisions.ID))
        .filter(
            ui ->
                java.util.Arrays.stream(ui.targetEntityIds()).anyMatch(id -> id == previousDriver))
        .toList()
        .forEach(ui -> feature.hud.UIUtils.closeDialog(ui, true));
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
