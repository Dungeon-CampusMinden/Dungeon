package level.level2;

import core.level.TileLevel;
import core.level.utils.Coordinate;
import java.util.List;

public class DevLevel02Riddle {

  private final TileLevel level;

  public DevLevel02Riddle(List<Coordinate> customPoints, DevLevel02 devLevel02) {

    this.level = devLevel02;
  }

  public void onTick(boolean isFirstTick) {}
}
