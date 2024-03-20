package level.devlevel.riddleHandler;

import core.level.TileLevel;
import core.level.utils.Coordinate;
import java.util.List;
import level.utils.ITickable;

public class IllusionRiddleHandler implements ITickable {

  private final TileLevel level;
  private final int laps = 1;
  private Coordinate[][] lapCheckpoints; // [location][3 tiles wide]
  private final Coordinate[] riddleRoomBounds; // TopLeft, BottomRight
  private final Coordinate
      riddleRewardSpawn; // The spawn point of the reward for solving the riddle
  private boolean rewardGiven = false;

  public IllusionRiddleHandler(List<Coordinate> customPoints, TileLevel level) {
    this.riddleRoomBounds = new Coordinate[] {new Coordinate(0, 0), new Coordinate(0, 0)};
    this.riddleRewardSpawn = new Coordinate(0, 0);

    this.level = level;
  }

  @Override
  public void onTick(boolean isFirstTick) {
    if (isFirstTick) {
      this.handleFirstTick();
    }
  }

  private void giveReward() {

    this.rewardGiven = true;
  }

  private void handleFirstTick() {
    if (this.level.tileAt(this.riddleRewardSpawn) != null)
      this.level.tileAt(this.riddleRewardSpawn).tintColor(0x22FF22FF);
  }
}
