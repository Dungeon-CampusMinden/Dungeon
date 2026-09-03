package rooms.systemRecovery.util.interpreter;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.utils.Point;
import feature.entities.MiscFactory;
import feature.systems.PositionSync;
import java.util.Arrays;

/** Applies room-side effects for terminal interpretation outcomes. */
public final class InterpretationCallbacks {

  private static final int ENERGY_CRATE_COUNT = 5;
  private static final int CORRECT_TINT = 0x40CC60FF;
  private static final float CRATE_ROW_OFFSET_Y = 2f;
  private static final Entity[] energyCrates = new Entity[ENERGY_CRATE_COUNT];

  private InterpretationCallbacks() {}

  /** Resets all callback-owned room state for a fresh level run. */
  public static void reset() {
    Arrays.fill(energyCrates, null);
  }

  /**
   * Spawns the energy crates after successful array initialization.
   *
   * @param terminalPoint point used to place the energy crates
   */
  public static void spawnEnergieCrates(Point terminalPoint) {
    float startX = terminalPoint.x() - (ENERGY_CRATE_COUNT / 2);
    float y = terminalPoint.y() + CRATE_ROW_OFFSET_Y;
    for (int index = 0; index < ENERGY_CRATE_COUNT; index++) {
      Entity crate = MiscFactory.crate(new Point(startX + index, y));
      crate.name("energie-" + index);
      energyCrates[index] = crate;
      Game.add(crate);
      PositionSync.syncPosition(crate);
    }
  }

  /** Colors every energy crate green after the complete assignment succeeds. */
  public static void markEnergyCratesCorrect() {
    for (int index = 0; index < ENERGY_CRATE_COUNT; index++) {
      tintEnergyCrate(index, CORRECT_TINT);
    }
  }

  private static void tintEnergyCrate(int index, int tintColor) {
    if (index < 0 || index >= energyCrates.length || energyCrates[index] == null) {
      return;
    }
    energyCrates[index].fetch(DrawComponent.class).ifPresent(draw -> draw.tintColor(tintColor));
  }
}
