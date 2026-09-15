package rooms.systemRecovery.util.tracking;

/** Stable identifiers and default event metadata for System Recovery riddles. */
public enum SystemRecoveryPuzzle {
  ENERGY("energy-array", "energy-lever", "lever"),
  MODULE_STORAGE("module-storage", "module-room", "interaction"),
  INVENTORY_SCANNER("inventory-scanner", "scanner-room", "interaction"),
  TRANSPORT_STORAGE("transport-storage", "transport-sequence", "sequence"),
  MANUAL_SORTING("manual-sorting", "sort-display", "choice"),
  BUBBLE_SORT("bubble-sort", "sort-machine", "choice"),
  DATA_ARCHIVE("data-archive", "archive-room", "interaction"),
  TWO_DIMENSIONAL_STORAGE("two-dimensional-storage", "storage-matrix", "interaction"),
  SEARCH_ROBOT("search-robot", "search-controller", "chip"),
  SYSTEM_CORE("system-core", "system-core", "interaction");

  private final String id;
  private final String defaultObjectId;
  private final String defaultAnswerKind;

  SystemRecoveryPuzzle(String id, String defaultObjectId, String defaultAnswerKind) {
    this.id = id;
    this.defaultObjectId = defaultObjectId;
    this.defaultAnswerKind = defaultAnswerKind;
  }

  /**
   * @return stable room-local puzzle identifier
   */
  public String id() {
    return id;
  }

  /**
   * @return default object identifier for physical riddle callbacks
   */
  public String defaultObjectId() {
    return defaultObjectId;
  }

  /**
   * @return default answer kind for physical riddle callbacks
   */
  public String defaultAnswerKind() {
    return defaultAnswerKind;
  }

  /**
   * Maps the shared terminal state to the riddle that owns it.
   *
   * @param state current terminal state
   * @return owning puzzle, or {@link #SYSTEM_CORE} for unknown final states
   */
  public static SystemRecoveryPuzzle fromTerminalState(int state) {
    return switch (state) {
      case 0, 1 -> ENERGY;
      case 2, 3, 4, 5 -> MODULE_STORAGE;
      case 6 -> INVENTORY_SCANNER;
      case 7, 8 -> TRANSPORT_STORAGE;
      case 9 -> DATA_ARCHIVE;
      case 10, 11 -> TWO_DIMENSIONAL_STORAGE;
      case 12 -> SEARCH_ROBOT;
      case 13, 14, 15, 16 -> SYSTEM_CORE;
      default -> SYSTEM_CORE;
    };
  }
}
