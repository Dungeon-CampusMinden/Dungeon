package rooms.lasthour.util;

/** Stable tracking identifiers for puzzles in The Last Hour. */
public enum LastHourPuzzle {
  POWER("power"),
  LOGIN("login"),
  STORAGE_ACCESS("storage-access"),
  STORAGE_RECOVERY("storage-recovery"),
  BLUE_USB("blue-usb"),
  VENTILATION("ventilation"),
  EXIT_CODE_ASSEMBLY("exit-code-assembly"),
  EXIT("exit"),
  VIRUS_NEUTRALIZATION("virus-neutralization");

  private final String id;

  LastHourPuzzle(String id) {
    this.id = id;
  }

  /**
   * Returns the stable identifier written to tracking events.
   *
   * @return stable room-local puzzle identifier
   */
  public String id() {
    return id;
  }
}
