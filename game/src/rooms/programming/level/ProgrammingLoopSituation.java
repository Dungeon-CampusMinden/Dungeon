package rooms.programming.level;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.level.DungeonLevel;
import engine.utils.Point;
import engine.utils.components.draw.animation.SpritesheetConfig;
import engine.utils.components.path.SimpleIPath;
import java.util.function.Predicate;
import rooms.programming.modules.loops.LoopType;

/** A straight passage blocked by Nox; runes move him toward a wall-side waymark. */
final class ProgrammingLoopSituation {
  private final String id;
  private final Point origin;
  private final Point end;
  private final int dx;
  private final int dy;

  ProgrammingLoopSituation(DungeonLevel level, String id) {
    this.id = id;
    origin = level.getPoint("loop-" + id);
    end = level.getPoint("loop-" + id + "-end");
    dx = Float.compare(end.x(), origin.x());
    dy = Float.compare(end.y(), origin.y());
    if ((dx == 0) == (dy == 0))
      throw new IllegalArgumentException("Loop passage must follow one cardinal direction: " + id);
    Entity landmark = new Entity("programming-" + id + "-waymark");
    PositionComponent at = new PositionComponent(level.getPoint("loop-" + id + "-landmark"));
    at.scale(0.7f);
    landmark.add(at);
    DrawComponent draw =
        new DrawComponent(
            new SimpleIPath("spritesheets/runes.png"), new SpritesheetConfig(0, 16, 1, 1));
    draw.tintColor(0xFFCC88FF);
    landmark.add(draw);
    Game.add(landmark);
  }

  Point origin() {
    return origin;
  }

  Point next(Point at) {
    return at.translate(dx, dy);
  }

  int count() {
    return switch (id) {
      case "forge-press", "cooling-channel" -> 6;
      case "bellows" -> 3;
      case "chain-lift", "heart-gate" -> 5;
      default -> throw new IllegalArgumentException("Unknown passage " + id);
    };
  }

  private SensorPredicate predicate(LoopType type) {
    if (type == LoopType.WHILE && (id.equals("forge-press") || id.equals("cooling-channel")))
      return SensorPredicate.BEFORE_WAYMARK;
    if (type == LoopType.DO_WHILE && (id.equals("bellows") || id.equals("heart-gate")))
      return SensorPredicate.BEFORE_WAYMARK;
    if (type == LoopType.DO_WHILE && id.equals("chain-lift")) return SensorPredicate.CLEAR;
    return SensorPredicate.AT_WAYMARK;
  }

  boolean condition(LoopType type, Point at, Predicate<Point> fits) {
    return switch (predicate(type)) {
      case BEFORE_WAYMARK -> !objective(at);
      case AT_WAYMARK -> objective(at);
      case CLEAR -> fits.test(next(at));
    };
  }

  boolean objective(Point at) {
    return Point.calculateDistance(end, at) < 0.1f;
  }

  String code(LoopType type) {
    return switch (type) {
      case WHILE -> "while (" + predicate(type).code + ") {\n    schritt();\n}";
      case DO_WHILE -> "do {\n    schritt();\n} while (" + predicate(type).code + ");";
      case FOR -> "for (int i = 0; i < " + count() + "; i++) {\n    schritt();\n}";
    };
  }

  String explanation() {
    return "Ziel: am Wegzeichen halten.\n\n"
        + "schritt(): ein Feld in Blickrichtung.\n"
        + "amWegzeichen(): auf Höhe des bernsteinfarbenen Zeichens.\n"
        + "wegFrei(): ausreichend Platz für den nächsten Schritt.";
  }

  private enum SensorPredicate {
    BEFORE_WAYMARK("!amWegzeichen()"),
    AT_WAYMARK("amWegzeichen()"),
    CLEAR("wegFrei()");

    private final String code;

    SensorPredicate(String code) {
      this.code = code;
    }
  }
}
