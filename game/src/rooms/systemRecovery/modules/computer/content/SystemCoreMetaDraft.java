package rooms.systemRecovery.modules.computer.content;

import java.util.Arrays;

/**
 * Client-local draft for the final System Core result form.
 *
 * <p>The draft is deliberately not sent to the server. It only preserves unfinished input while a
 * player closes and reopens the local computer dialog. The server still receives and validates the
 * complete payload only when the player submits it.
 */
public final class SystemCoreMetaDraft {

  /** Number of values required by the final energy result. */
  public static final int ENERGY_SLOT_COUNT = 5;

  private static final String[] energyValues = new String[ENERGY_SLOT_COUNT];
  private static String moduleCount = "";
  private static String scannedModuleCount = "";

  static {
    clear();
  }

  private SystemCoreMetaDraft() {}

  /**
   * Returns the unfinished value for one energy slot.
   *
   * @param index zero-based energy slot index
   * @return unfinished value for the slot
   */
  public static synchronized String energyValue(int index) {
    checkEnergyIndex(index);
    return energyValues[index];
  }

  /**
   * Stores the unfinished value for one energy slot.
   *
   * @param index zero-based energy slot index
   * @param value unfinished value, or {@code null} to store an empty value
   */
  public static synchronized void energyValue(int index, String value) {
    checkEnergyIndex(index);
    energyValues[index] = value == null ? "" : value;
  }

  /**
   * @return the unfinished occupied-module count
   */
  public static synchronized String moduleCount() {
    return moduleCount;
  }

  /**
   * Stores the unfinished occupied-module count.
   *
   * @param value unfinished count, or {@code null} to store an empty value
   */
  public static synchronized void moduleCount(String value) {
    moduleCount = value == null ? "" : value;
  }

  /**
   * @return the unfinished scanned-module count
   */
  public static synchronized String scannedModuleCount() {
    return scannedModuleCount;
  }

  /**
   * Stores the unfinished scanned-module count.
   *
   * @param value unfinished count, or {@code null} to store an empty value
   */
  public static synchronized void scannedModuleCount(String value) {
    scannedModuleCount = value == null ? "" : value;
  }

  /** Clears all unfinished final-form input. */
  public static synchronized void clear() {
    Arrays.fill(energyValues, "");
    moduleCount = "";
    scannedModuleCount = "";
  }

  private static void checkEnergyIndex(int index) {
    if (index < 0 || index >= ENERGY_SLOT_COUNT) {
      throw new IndexOutOfBoundsException("Energy slot: " + index);
    }
  }
}
