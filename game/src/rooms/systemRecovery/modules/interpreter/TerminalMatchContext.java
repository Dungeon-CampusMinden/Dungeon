package rooms.systemRecovery.modules.interpreter;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores regex captures shared across multiple terminal code lines.
 *
 * <p>The use case is flexible but consistent variable naming. A loop may capture the chosen index
 * variable and array variable, for example {@code index} and {@code modules}. Later code lines can
 * capture groups with the same names and are only accepted when they resolve to those same values.
 * This lets users choose names freely while still requiring {@code modules[index]} after {@code for
 * (int index = 0; index < modules.length; index++)}.
 */
final class TerminalMatchContext {

  private final Map<String, String> captures = new HashMap<>();

  TerminalMatchContext() {}

  private TerminalMatchContext(TerminalMatchContext context) {
    captures.putAll(context.captures);
  }

  TerminalMatchContext copy() {
    return new TerminalMatchContext(this);
  }

  boolean bind(String name, String value) {
    String capturedValue = captures.get(name);
    if (capturedValue == null) {
      captures.put(name, value);
      return true;
    }
    return capturedValue.equals(value);
  }

  void clear() {
    captures.clear();
  }

  void replaceWith(TerminalMatchContext context) {
    captures.clear();
    captures.putAll(context.captures);
  }
}
