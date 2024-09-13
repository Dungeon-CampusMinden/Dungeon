package level;

import core.level.Tile;
import core.level.TileLevel;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import level.utils.ITickable;

import java.util.List;

public class BlocklyLevel  {

  public LevelElement[][] layout;
  public DesignLabel designLabel;
  public Point heroPos;
  public List<Coordinate> customPoints;

  public BlocklyLevel(LevelElement[][] layout, DesignLabel designLabel, Point heroPos, List<Coordinate> customPoints) {
    this.layout = layout;
    this.designLabel = designLabel;
    this.heroPos = heroPos;
    this.customPoints = customPoints;
  }
}
