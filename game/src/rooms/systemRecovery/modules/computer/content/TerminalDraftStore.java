package rooms.systemRecovery.modules.computer.content;

/** Client-process-local terminal draft, retained only while the current level remains loaded. */
public final class TerminalDraftStore {

  private static String code = "";

  private TerminalDraftStore() {}

  /**
   * @return the current client-local unsent terminal source
   */
  public static synchronized String code() {
    return code;
  }

  /**
   * @param source replacement source, or {@code null} to clear it
   */
  public static synchronized void code(String source) {
    code = source == null ? "" : source;
  }

  /** Clears unsent editor text when a new level/run is loaded. */
  public static synchronized void clear() {
    code = "";
  }
}
