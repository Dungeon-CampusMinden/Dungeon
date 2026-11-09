package rooms.soulweaver.modules.loops;

import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;
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
    MOVE,
    LEFT,
    RIGHT,
    ATTACK,
    JUMP;

    /**
     * @param word renders a key below the {@code soulweaver} namespace, as text for this client or
     *     as a transport key on the server
     * @return the source spelling of this action
     */
    public String code(UnaryOperator<String> word) {
      return word.apply("loops.code." + name().toLowerCase(Locale.ROOT)) + "()";
    }
  }

  /** Sensors evaluated against the current attempt state. */
  public enum Condition {
    FREE,
    NOT_GOAL,
    AT_GOAL,
    ALWAYS;

    /**
     * @param word renders a key below the {@code soulweaver} namespace
     * @return the source spelling of this sensor
     */
    public String code(UnaryOperator<String> word) {
      return switch (this) {
        case FREE -> word.apply("loops.code.free") + "()";
        case NOT_GOAL -> "!" + word.apply("loops.code.goal") + "()";
        case AT_GOAL -> word.apply("loops.code.goal") + "()";
        case ALWAYS -> "true";
      };
    }
  }

  /** Copies the instruction lists so programs remain immutable. */
  public LoopProgram {
    body = List.copyOf(body);
    after = List.copyOf(after);
  }

  /**
   * @param word renders a key below the {@code soulweaver} namespace, as text for this client or as
   *     a transport key on the server
   * @return source generated directly from the executable program
   */
  public String code(UnaryOperator<String> word) {
    String instructions =
        body.stream().map(a -> "    " + a.code(word) + ";").collect(Collectors.joining("\n"));
    String loop =
        switch (type) {
          case WHILE -> "while (" + condition.code(word) + ") {\n" + instructions + "\n}";
          case DO_WHILE -> "do {\n" + instructions + "\n} while (" + condition.code(word) + ");";
          case FOR -> "for (int i = 0; i < " + count + "; i++) {\n" + instructions + "\n}";
        };
    return loop + after.stream().map(a -> "\n" + a.code(word) + ";").collect(Collectors.joining());
  }
}
