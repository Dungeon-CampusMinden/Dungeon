package rooms.lasthour.level;

import engine.Entity;
import engine.Game;
import engine.level.DungeonLevel;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.rendering.Atmosphere;
import engine.rendering.Light;
import engine.utils.Point;
import feature.utils.EntityUtils;
import java.util.List;
import java.util.Map;
import rooms.lasthour.modules.computer.ComputerDialog;
import rooms.lasthour.modules.computer.ComputerStateComponent;

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
  public Atmosphere atmosphere() {
    return LastHourLevel.lighting();
  }

  @Override
  public void lights(List<Light> out) {
    if (pc != null && keypad != null)
      LastHourLevel.addLights(EntityUtils.getPosition(pc), getPoint("timer"), keypad, out);
  }

  @Override
  protected void onTick() {
    findEntities();

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
