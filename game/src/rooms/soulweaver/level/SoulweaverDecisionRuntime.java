package rooms.soulweaver.level;

import engine.Entity;
import engine.Game;
import engine.components.PositionComponent;
import engine.level.DungeonLevel;
import engine.utils.Point;
import feature.components.UIComponent;
import java.util.ArrayDeque;
import java.util.List;
import rooms.soulweaver.modules.decisions.DecisionMaze;
import rooms.soulweaver.modules.decisions.DecisionMaze.Side;
import rooms.soulweaver.modules.decisions.DecisionMaze.Values;
import rooms.soulweaver.util.SoulweaverText;

/** Server-owned progression. A blocked segment is resumed from its actual physical position. */
final class SoulweaverDecisionRuntime {
  private final DungeonLevel level;
  private final Entity golem;
  private final SoulweaverGolemRuntime motion;
  final SoulweaverEnding ending;
  private final ArrayDeque<Point> route = new ArrayDeque<>();
  private Values values = DecisionMaze.start(0);
  private int revision, junction, failures;
  private int driver = -1;
  private boolean active, moving, blocked, completed;
  private String feedback = SoulweaverText.key("decisions.feedback.intro");
  private Runnable arrival = () -> {};

  SoulweaverDecisionRuntime(DungeonLevel level, Entity golem, SoulweaverGolemRuntime motion) {
    this.level = level;
    this.golem = golem;
    this.motion = motion;
    SoulweaverDecisions.reset();
    ending = new SoulweaverEnding(SoulweaverDecisionWorld.spawn(level), () -> completed);
    publish();
  }

  void start() {
    if (active) return;
    active = true;
    SoulweaverProgress.started("decisions", SoulweaverText.key("progress.task.decisions"));
    feedback = SoulweaverText.key("decisions.feedback.enter");
    var approach = motion.workshopPath(SoulweaverDecisionWorld.START);
    if (approachBlocked(approach)) {
      blocked = true;
      feedback = SoulweaverText.key("decisions.feedback.access-blocked");
      publish();
      return;
    }
    var points = new java.util.ArrayList<>(approach);
    travel(
        points,
        () -> {
          feedback = SoulweaverText.key("decisions.feedback.mount");
          publish();
          if (driver >= 0) enter();
        });
  }

  /**
   * @param approach planned route to START
   * @return whether Nox cannot reach START although it is not already there
   */
  private boolean approachBlocked(List<Point> approach) {
    return approach.isEmpty()
        && Point.calculateDistance(
                golem.fetch(PositionComponent.class).orElseThrow().position(),
                SoulweaverDecisionWorld.START)
            > .1f;
  }

  void show(Entity who) {
    if (!active || !authorized(who) || Game.hud().blocksGameplayInput(who)) return;
    if (completed) {
      SoulweaverGolemRuntime.showText(who, feedback);
      return;
    }
    if (driver >= 0 && driver != who.id()) {
      SoulweaverGolemRuntime.showText(who, SoulweaverText.key("decisions.occupied"));
      return;
    }
    driver = who.id();
    publish();
    SoulweaverDecisions.open(who, state(), intent -> accept(who, intent));
    if (!moving
        && !blocked
        && !completed
        && Point.calculateDistance(
                golem.fetch(PositionComponent.class).orElseThrow().position(),
                SoulweaverDecisionWorld.START)
            < .1f) enter();
  }

  private void enter() {
    travel(
        List.of(SoulweaverDecisionWorld.junction(0)),
        () -> {
          feedback = SoulweaverText.key("decisions.feedback.read");
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

  void accept(Entity who, SoulweaverDecisions.Intent intent) {
    if (intent == null || !authorized(who) || driver != who.id() || revision != intent.revision())
      return;
    if ("RESUME".equals(intent.operation()) && blocked) {
      blocked = false;
      if (route.isEmpty()) {
        var approach = motion.workshopPath(SoulweaverDecisionWorld.START);
        if (approachBlocked(approach)) {
          blocked = true;
          publish();
          return;
        }
        feedback = SoulweaverText.key("decisions.feedback.continue");
        var points = new java.util.ArrayList<>(approach);
        points.add(SoulweaverDecisionWorld.junction(0));
        travel(
            points,
            () -> {
              feedback = SoulweaverText.key("decisions.feedback.first-rune");
              publish();
            });
      } else {
        feedback = SoulweaverText.key("decisions.feedback.continue");
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
    SoulweaverProgress.interaction("decisions", "junction-" + (junction + 1) + "-" + side, who);
    SoulweaverProgress.participant(who)
        .ifPresent(
            participant ->
                SoulweaverProgress.attempt(
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
                            : List.of(
                                SoulweaverText.german(
                                    SoulweaverText.key("decisions.failure.other-door"))))));
    SoulweaverProgress.log(
        "decisions",
        SoulweaverText.key("decisions.log.attempt", failures + 1),
        SoulweaverText.key(
                "decisions.log.junction",
                junction + 1,
                values.kraft(),
                values.energie(),
                values.temperatur())
            + " · "
            + SoulweaverText.key(side == Side.LEFT ? "decisions.code.left" : "decisions.code.right")
            + " · "
            + SoulweaverText.key(correct ? "decisions.log.correct" : "decisions.log.wrong")
            + (assisted ? " " + SoulweaverText.key("progress.assisted") : ""));
    SoulweaverDecisionWorld.open(level, junction, side, correct);
    feedback =
        correct
            ? SoulweaverText.key("decisions.feedback.door-onward")
            : SoulweaverText.key("decisions.feedback.door-back");
    travel(
        SoulweaverDecisionWorld.route(junction, side, correct),
        () -> {
          if (correct) {
            var delta = DecisionMaze.delta(junction++);
            values = values.plus(delta.kraft(), delta.energie(), delta.temperatur());
            feedback = "";
            if (junction == 6) {
              completed = true;
              motion.completeDecisions();
              feedback = SoulweaverText.key("decisions.feedback.heartfire");
              SoulweaverProgress.solved("decisions", feedback);
              // The ride ends at the goal; the rider walks to the inscription on foot.
              int rider = driver;
              dismount();
              Game.findEntityById(rider)
                  .ifPresent(player -> SoulweaverGolemRuntime.showText(player, feedback));
            }
          } else {
            SoulweaverDecisionWorld.reset(level);
            failures++;
            junction = 0;
            values = DecisionMaze.start(failures);
            feedback = SoulweaverText.key("decisions.feedback.back-at-start");
          }
          publish();
          if (!correct) {
            blocked = true;
            route.add(SoulweaverDecisionWorld.junction(0));
            arrival =
                () -> {
                  feedback = SoulweaverText.key("decisions.feedback.new-values");
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
          feedback = reason + " " + SoulweaverText.key("decisions.feedback.clear-way");
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
        .filter(ui -> ui.dialogContext().dialogType().type().equals(SoulweaverDecisions.ID))
        .filter(
            ui ->
                java.util.Arrays.stream(ui.targetEntityIds()).anyMatch(id -> id == previousDriver))
        .toList()
        .forEach(ui -> feature.hud.UIUtils.closeDialog(ui, true));
  }

  boolean active() {
    return active;
  }

  /**
   * @return why tips are unavailable, or empty while Nox waits at a junction
   */
  String helpStatus() {
    if (completed) return SoulweaverText.key("decisions.help.completed");
    if (moving) return SoulweaverText.key("decisions.help.moving");
    if (blocked) return SoulweaverText.key("decisions.help.blocked");
    return "";
  }

  SoulweaverDecisions.State state() {
    return new SoulweaverDecisions.State(
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
    SoulweaverDecisions.publish(state());
  }
}
