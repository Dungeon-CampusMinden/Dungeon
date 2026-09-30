package rooms.systemRecovery.modules.computer;

/** Callback keys emitted by the System Recovery computer UI. */
public final class SystemRecoveryComputerCallbacks {

  /** Sends the current terminal source text to the server-side interpreter. */
  public static final String TERMINAL_SEND = "terminalSend";

  /** Opens the server-authoritative Petri-net token inspection for local debugging. */
  public static final String DEBUG_PETRI_NET = "debugPetriNet";

  /** Gives an empty sort-program USB stick to the requesting player's inventory. */
  public static final String DEBUG_GIVE_USB = "debugGiveUsb";

  /** Stores the edited bubble-sort condition on the inserted sort-program stick. */
  public static final String SORT_PROGRAM_SAVE = "sortProgramSave";

  /** Saves the fill-in draft to the inserted sort stick and safely ejects it. */
  public static final String SORT_PROGRAM_EJECT = "sortProgramEject";

  /** Stores the edited search loop on the inserted locator chip. */
  public static final String SEARCH_PROGRAM_SAVE = "searchProgramSave";

  /** Saves the fill-in draft to the inserted locator chip and safely ejects it. */
  public static final String SEARCH_PROGRAM_EJECT = "searchProgramEject";

  /** Executes the access script on the inserted system-core module. */
  public static final String SYSTEM_CORE_SCRIPT_RUN = "systemCoreScriptRun";

  /** Submits the three values from the system-core result input mask. */
  public static final String SYSTEM_CORE_META_SUBMIT = "systemCoreMetaSubmit";

  private SystemRecoveryComputerCallbacks() {}
}
