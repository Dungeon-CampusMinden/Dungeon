package portal;

import java.util.function.Supplier;
import portal.energyPellet.abstraction.EnergyPelletCatcherBehavior;
import portal.portals.abstraction.Calculations;

/**
 * Central registry for portal module configuration and runtime dependencies.
 *
 * <p>This class allows the host application (e.g. AdvancedDungeon) to inject implementations and
 * settings into the portal module
 */
public final class PortalRegistry {

  private static boolean debugMode = false;
  private static Supplier<Calculations> calculationsSupplier;
  private static Supplier<EnergyPelletCatcherBehavior> pelletCatcherSupplier;

  private PortalRegistry() {}

  /**
   * Enables or disables debug mode for the portal module.
   *
   * @param debug {@code true} to enable debug mode, {@code false} to disable it
   */
  public static void setDebugMode(boolean debug) {
    debugMode = debug;
  }

  /**
   * Returns whether debug mode is currently enabled.
   *
   * @return {@code true} if debug mode is active
   */
  public static boolean isDebugMode() {
    return debugMode;
  }

  /**
   * Registers the supplier for the {@link Calculations} implementation.
   *
   * @param supplier a supplier that provides a new {@link Calculations} instance
   */
  public static void registerCalculations(Supplier<Calculations> supplier) {
    calculationsSupplier = supplier;
  }

  /**
   * @return a {@link Calculations} instance
   * @throws IllegalStateException if no supplier has been registered
   */
  public static Calculations getCalculations() {
    if (calculationsSupplier == null)
      throw new IllegalStateException("No Calculations implementation registered");
    return calculationsSupplier.get();
  }

  /**
   * Registers the supplier for the {@link EnergyPelletCatcherBehavior} implementation.
   *
   * @param supplier a supplier that provides a new {@link EnergyPelletCatcherBehavior} instance
   */
  public static void registerPelletCatcherBehavior(Supplier<EnergyPelletCatcherBehavior> supplier) {
    pelletCatcherSupplier = supplier;
  }

  /**
   * Returns an {@link EnergyPelletCatcherBehavior} instance from the registered supplier.
   *
   * @return an {@link EnergyPelletCatcherBehavior} instance
   * @throws IllegalStateException if no supplier has been registered
   */
  public static EnergyPelletCatcherBehavior getPelletCatcherBehavior() {
    if (pelletCatcherSupplier == null)
      throw new IllegalStateException("No EnergyPelletCatcherBehavior registered");
    return pelletCatcherSupplier.get();
  }
}
