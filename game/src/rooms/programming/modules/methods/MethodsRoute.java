package rooms.programming.modules.methods;

import java.util.ArrayList;
import java.util.List;

/** The workshop's original program. Both execution and the physical workshop use these blocks. */
public final class MethodsRoute {
  /** Reusable blocks introduced by the workshop stations. */
  public enum Kind {
    GATE,
    RUNE,
    COLLECT,
    ALTAR
  }

  /** Relative turn inside a method body. */
  public enum Direction {
    NONE,
    LEFT,
    RIGHT,
    BACK;

    /**
     * @return the direction as displayed in the learner's source code
     */
    public String label() {
      return switch (this) {
        case LEFT -> "LINKS";
        case RIGHT -> "RECHTS";
        case BACK -> "HINTEN";
        case NONE -> "?";
      };
    }
  }

  /** Physical instructions and the return/assignment steps shown during execution. */
  public enum Action {
    TURN,
    MOVE,
    OPEN_GATE,
    ACTIVATE_RUNE,
    COLLECT,
    PLACE,
    RETURN,
    CALL,
    ASSIGN
  }

  /**
   * One executable instruction.
   *
   * @param action operation to execute
   * @param amount movement distance, crystal count, or computed value
   * @param direction relative heading for a turn, otherwise NONE
   */
  public record Step(Action action, int amount, Direction direction) {
    /**
     * Creates an instruction without a turn.
     *
     * @param action operation to execute
     * @param amount distance, crystal count, or computed value
     * @return the instruction
     */
    public static Step action(Action action, int amount) {
      return new Step(action, amount, Direction.NONE);
    }

    /**
     * @param direction relative heading to turn toward
     * @return a turn instruction
     */
    public static Step turn(Direction direction) {
      return new Step(Action.TURN, 0, direction);
    }
  }

  /**
   * An ordered worksite and its original code block.
   *
   * @param index zero-based position in the workshop route
   * @param kind reusable block taught at this worksite
   * @param direction turn required by the original block
   * @param amount crystals collected or required at the worksite
   */
  public record Station(int index, Kind kind, Direction direction, int amount) {
    /**
     * @return the worksite's displayed name and inputs
     */
    public String label() {
      return switch (kind) {
        case GATE -> "Tor";
        case RUNE -> "Runenstein · " + direction.label();
        case COLLECT -> "Kristallfeld · " + amount + " Kristalle";
        case ALTAR -> "Altar · benötigt " + amount;
      };
    }

    /**
     * @return the original physical instructions before extraction
     */
    public List<Step> body() {
      return MethodsRoute.body(kind, direction, amount);
    }

    /**
     * @return original source including the caller's crystal assignment
     */
    public List<String> source() {
      var result = new ArrayList<>(body().stream().map(MethodsRoute::source).toList());
      if (kind == Kind.COLLECT) result.add("kristalle = kristalle + gesammelt;");
      if (kind == Kind.ALTAR) result.add("kristalle = kristalle - " + amount + ";");
      return List.copyOf(result);
    }
  }

  public static final List<Station> STATIONS =
      List.of(
          new Station(0, Kind.GATE, Direction.NONE, 0),
          new Station(1, Kind.GATE, Direction.NONE, 0),
          new Station(2, Kind.RUNE, Direction.RIGHT, 0),
          new Station(3, Kind.RUNE, Direction.LEFT, 0),
          new Station(4, Kind.COLLECT, Direction.NONE, 3),
          new Station(5, Kind.COLLECT, Direction.NONE, 5),
          new Station(6, Kind.ALTAR, Direction.NONE, 3),
          new Station(7, Kind.ALTAR, Direction.NONE, 5));

  private MethodsRoute() {}

  /**
   * Instantiates a block with concrete inputs for evaluation and world construction.
   *
   * @param kind block to instantiate
   * @param direction relative turn for rune activation
   * @param amount crystals to collect or place
   * @return the block's physical instructions
   */
  public static List<Step> body(Kind kind, Direction direction, int amount) {
    return switch (kind) {
      case GATE -> List.of(Step.action(Action.OPEN_GATE, 0), Step.action(Action.MOVE, 4));
      case RUNE ->
          List.of(
              Step.action(Action.MOVE, 1),
              Step.turn(direction),
              Step.action(Action.MOVE, 3),
              Step.action(Action.ACTIVATE_RUNE, 0));
      case COLLECT ->
          List.of(
              Step.action(Action.MOVE, 1),
              Step.action(Action.COLLECT, amount),
              Step.action(Action.MOVE, 1));
      case ALTAR ->
          List.of(
              Step.action(Action.MOVE, 1),
              Step.action(Action.PLACE, amount),
              Step.action(Action.MOVE, 1));
    };
  }

  /**
   * @param step instruction to display
   * @return its spelling in the workshop pseudocode
   */
  public static String source(Step step) {
    return switch (step.action()) {
      case TURN -> "DREHE(" + step.direction().label() + ");";
      case MOVE -> "GEHE(" + step.amount() + ");";
      case OPEN_GATE -> "ÖFFNE();";
      case ACTIVATE_RUNE -> "AKTIVIERE();";
      case COLLECT -> "gesammelt = SAMMLE_ALLE();";
      case PLACE -> "LEGE_AB(" + step.amount() + ");";
      case RETURN -> "GIB_ZURÜCK " + step.amount() + ";";
      case CALL -> "AUFRUF();";
      case ASSIGN -> "kristalle = " + step.amount() + ";";
    };
  }
}
