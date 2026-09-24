package rooms.programming.modules.loops;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Small typed programs. Displayed source is generated from the executed instructions.
 *
 * @param type loop form
 * @param condition sensor checked by while and do-while
 * @param count iteration count for for-loops
 * @param body repeated instructions
 * @param after instructions executed once after the loop
 */
public record LoopProgram(
    LoopType type, Condition condition, int count, List<Action> body, List<Action> after) {
  /** Supported physical actions. */
  public enum Action {
    MOVE("schritt()"),
    LEFT("linksDrehen()"),
    RIGHT("rechtsDrehen()"),
    ATTACK("angreifen()"),
    JUMP("springen()");
    private final String code;

    Action(String code) {
      this.code = code;
    }

    /**
     * @return the source spelling of this action
     */
    public String code() {
      return code;
    }
  }

  /** Sensors evaluated against the current attempt state. */
  public enum Condition {
    FREE("bodenVoraus()"),
    NOT_GOAL("!amWegzeichen()"),
    AT_GOAL("amWegzeichen()"),
    ALWAYS("true");
    private final String code;

    Condition(String code) {
      this.code = code;
    }

    /**
     * @return the source spelling of this sensor
     */
    public String code() {
      return code;
    }
  }

  /** Copies the instruction lists so programs remain immutable. */
  public LoopProgram {
    body = List.copyOf(body);
    after = List.copyOf(after);
  }

  /**
   * @return source generated directly from the executable program
   */
  public String code() {
    String instructions =
        body.stream().map(a -> "    " + a.code() + ";").collect(Collectors.joining("\n"));
    String loop =
        switch (type) {
          case WHILE -> "while (" + condition.code() + ") {\n" + instructions + "\n}";
          case DO_WHILE -> "do {\n" + instructions + "\n} while (" + condition.code() + ");";
          case FOR -> "for (int i = 0; i < " + count + "; i++) {\n" + instructions + "\n}";
        };
    return loop + after.stream().map(a -> "\n" + a.code() + ";").collect(Collectors.joining());
  }
}
