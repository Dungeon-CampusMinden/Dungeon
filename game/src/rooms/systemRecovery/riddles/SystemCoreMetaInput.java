package rooms.systemRecovery.riddles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Parses the compact payload sent by the System Core result input mask.
 *
 * @param sortedEnergy sorted energy values submitted by the player
 * @param activeModules number of non-null module entries
 * @param scannedModules number of modules visited by the central search
 */
public record SystemCoreMetaInput(
    List<Integer> sortedEnergy, int activeModules, int scannedModules) {

  private static final String FIELD_SEPARATOR = "\\|";
  private static final String ENERGY_SEPARATOR = ",";
  private static final int ENERGY_VALUE_COUNT = 5;

  /** Keeps the parsed energy values immutable for server-side validation. */
  public SystemCoreMetaInput {
    sortedEnergy = List.copyOf(sortedEnergy);
  }

  /**
   * Parses the wire format used by the input mask: {@code energy0,...,energy4|modules|scanned}.
   *
   * @param payload serialized values from the client input mask
   * @return parsed values, or empty when the payload is malformed
   */
  public static Optional<SystemCoreMetaInput> parse(String payload) {
    if (payload == null) return Optional.empty();

    String[] fields = payload.split(FIELD_SEPARATOR, -1);
    if (fields.length != 3) return Optional.empty();

    String[] energyFields = fields[0].split(ENERGY_SEPARATOR, -1);
    if (energyFields.length != ENERGY_VALUE_COUNT) return Optional.empty();

    try {
      List<Integer> energy =
          Arrays.stream(energyFields).map(String::trim).map(Integer::parseInt).toList();
      return Optional.of(
          new SystemCoreMetaInput(
              energy, Integer.parseInt(fields[1].trim()), Integer.parseInt(fields[2].trim())));
    } catch (NumberFormatException exception) {
      return Optional.empty();
    }
  }

  /**
   * Checks the parsed values against the authoritative results of the three core tasks.
   *
   * @param expectedEnergy sorted energy result
   * @param expectedModules occupied module count
   * @param expectedScannedModules number of modules that must be visited
   * @return whether all three results match exactly
   */
  public boolean matches(
      List<Integer> expectedEnergy, int expectedModules, int expectedScannedModules) {
    return sortedEnergy.equals(expectedEnergy)
        && activeModules == expectedModules
        && scannedModules == expectedScannedModules;
  }
}
