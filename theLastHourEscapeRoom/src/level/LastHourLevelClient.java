package level;

import static level.LastHourLevel.*;

import contrib.modules.worldTimer.WorldTimerSystem;
import contrib.utils.EntityUtils;
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
    Game.add(new WorldTimerSystem());
    setupLightingShader();
  }

  @Override
  protected void onTick() {
    checkInteractFeedback();
    findEntities();

    if (pc != null && keypad != null)
      updateLightingShader(EntityUtils.getPosition(pc), getPoint("timer"), keypad);

    if (ComputerStateComponent.getState().isPresent())
      ComputerDialog.getInstance()
          .ifPresent(
              cd -> {
                if (cd.sharedState() != ComputerStateComponent.getState().get()) {
                  cd.updateState(ComputerStateComponent.getState().get());
                }
              });
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
          .filter(e -> e.name().equals("keypad"))
          .findFirst()
          .ifPresent(e -> keypad = e);
    }
  }
}
