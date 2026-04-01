package portal;

import java.util.function.Supplier;
import portal.energyPellet.abstraction.EnergyPelletCatcherBehavior;
import portal.portals.abstraction.Calculations;

public final class PortalRegistry {

  private static boolean debugMode = false;
  private static Supplier<Calculations> calculationsSupplier;
  private static Supplier<EnergyPelletCatcherBehavior> pelletCatcherSupplier;

  private PortalRegistry() {}

  public static void setDebugMode(boolean debug) { debugMode = debug; }
  public static boolean isDebugMode() { return debugMode; }

  public static void registerCalculations(Supplier<Calculations> supplier) {
    calculationsSupplier = supplier;
  }

  public static Calculations getCalculations() {
    if (calculationsSupplier == null)
      throw new IllegalStateException("No Calculations implementation registered");
    return calculationsSupplier.get();
  }

  public static void registerPelletCatcherBehavior(Supplier<EnergyPelletCatcherBehavior> supplier) {
    pelletCatcherSupplier = supplier;
  }

  public static EnergyPelletCatcherBehavior getPelletCatcherBehavior() {
    if (pelletCatcherSupplier == null)
      throw new IllegalStateException("No EnergyPelletCatcherBehavior registered");
    return pelletCatcherSupplier.get();
  }
}
