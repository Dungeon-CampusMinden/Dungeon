package level.sandbox;

import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import level.BlocklyLevel;
import level.LevelManagementUtils;

import java.util.Map;

/**
 * An empty sandbox level used during development and integration testing.
 *
 * <h2>Sandbox guarantees</h2>
 *
 * <ul>
 *   <li>No introductory popups – the hero is placed immediately without any blocking dialogs.
 *   <li>All Blockly blocks are unlocked – every category and direction is available.
 *   <li>Fog of war is disabled so the whole arena is visible.
 *   <li>The camera follows the hero and starts at the default zoom level.
 * </ul>
 *
 * <p>The level is loaded automatically when {@link client.Client} is started with the {@code
 * --sandbox} command-line argument.
 */
public class SandboxLevel extends BlocklyLevel {

  /**
   * Creates a new SandboxLevel.
   *
   * @param layout 2D array containing the tile layout (parsed from {@code sandbox_1.level}).
   * @param designLabel The design label for the level tiles.
   * @param namedPoints Any named points defined in the level file.
   */
  public SandboxLevel(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Sandbox");
    // No blocks are restricted – everything is available for testing.
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.fog(false);
    LevelManagementUtils.cameraFocusHero();
    LevelManagementUtils.centerHero();
    LevelManagementUtils.playerViewDirection(Direction.DOWN);
    LevelManagementUtils.zoomDefault();
    // Intentionally no popups – sandbox must never block test execution.
  }
}
