package rooms.systemRecovery.level;

import engine.level.DungeonLevel;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.utils.Point;
import engine.utils.Tuple;
import feature.entities.deco.Deco;
import java.util.List;
import java.util.Map;

/** Client-side level shell for System Recovery. */
public class SystemRecoveryClientLevel extends DungeonLevel {

  private static final String LEVEL_NAME = "system-recovery-1";

  /**
   * Creates the System Recovery client level.
   *
   * @param layout the tile layout loaded from the server level state
   * @param designLabel the visual design for the tiles
   * @param namedPoints named points loaded from the level state
   * @param decorations static decorations loaded from the level state
   */
  public SystemRecoveryClientLevel(
      LevelElement[][] layout,
      DesignLabel designLabel,
      Map<String, Point> namedPoints,
      List<Tuple<Deco, Point>> decorations) {
    super(layout, designLabel, namedPoints, decorations, LEVEL_NAME);
  }

  /**
   * Creates the System Recovery client level.
   *
   * @param layout the tile layout loaded from the server level state
   * @param designLabel the visual design for the tiles
   * @param namedPoints named points loaded from the level state
   */
  public SystemRecoveryClientLevel(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, LEVEL_NAME);
  }
}
