package rooms.systemRecovery.riddles;

import java.util.List;
import java.util.regex.Pattern;
import rooms.systemRecovery.modules.interpreter.TerminalInterpreter;

/** Recognizes the direct sorted-array shortcut without treating it as a valid riddle solution. */
public final class SystemCoreSortShortcut {

  private static final Pattern SORTED_ARRAY_DECLARATION =
      Pattern.compile(
          "(?:int\\s*\\[\\s*]\\s*array|int\\s+array\\s*\\[\\s*])"
              + "\\s*=\\s*(?:new\\s+int\\s*\\[\\s*]\\s*)?"
              + "\\{\\s*4\\s*,\\s*8\\s*,\\s*15\\s*,\\s*16\\s*,\\s*23\\s*,\\s*42\\s*}\\s*;");

  private SystemCoreSortShortcut() {}

  /**
   * Returns true only when the complete submission is one direct declaration of the sorted R10
   * values. It remains a rejected terminal attempt; this method only identifies the achievement.
   *
   * @param source submitted terminal text
   * @return whether the source is exactly the direct sorted-array shortcut
   */
  public static boolean isDirectSortedArrayInitialization(String source) {
    List<String> statements = TerminalInterpreter.statementsForSource(source);
    return statements.size() == 1
        && SORTED_ARRAY_DECLARATION.matcher(statements.getFirst()).matches();
  }
}
