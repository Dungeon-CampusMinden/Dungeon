package level;

import static level.LastHourLevel.*;

import core.Entity;
import core.Game;
import core.level.DungeonLevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import java.util.Map;
import modules.computer.*;

/** The Last Hour Room. */
public class LastHourLevelClient extends DungeonLevel {

  private static Entity keypad;
  private static Entity pc;

  /**
   * Creates a new Demo Level.
   *
   * @param layout The layout of the level.
   * @param designLabel The design label of the level.
   * @param namedPoints The custom points of the level.
   */
  public LastHourLevelClient(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "last-hour-1");
  }

  @Override
  protected void onFirstTick() {
    setupLightingShader();
  }

  @Override
  protected void onTick() {
    checkInteractFeedback();
    updateLightingShader(getPoint("timer"), getPoint("keypad-storage"));

    findEntities();
  }

  private void findEntities() {
    if (pc == null) {
      Game.levelEntities()
          .filter(e -> e.name().equals("pc-main"))
          .findFirst()
          .ifPresent(e -> pc = e);
    }
    if (keypad == null) {
      Game.levelEntities()
          .filter(e -> e.name().equals("keypad-main"))
          .findFirst()
          .ifPresent(e -> keypad = e);
    }
  }
}
