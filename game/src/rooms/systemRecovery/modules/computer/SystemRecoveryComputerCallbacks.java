package rooms.systemRecovery.modules.computer;

/** Callback keys emitted by the System Recovery computer UI. */
public final class SystemRecoveryComputerCallbacks {

  /** Sends the current terminal source text to the server-side interpreter. */
  public static final String TERMINAL_SEND = "terminalSend";

  /** Advances the terminal state through the server-side debug callback. */
  public static final String TERMINAL_NEXT_STEP = "terminalNextStep";

  private SystemRecoveryComputerCallbacks() {}
}
