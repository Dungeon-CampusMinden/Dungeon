package level;

import contrib.hud.DialogUtils;
import contrib.utils.components.skill.SkillTools;
import core.level.Tile;
import core.level.TileLevel;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.MissingHeroException;
import core.utils.Point;

public class MazeLevel extends TileLevel implements ITickable {
  private Coordinate lastHeroCoords = new Coordinate(0, 0);

  public boolean finished = false;


  public MazeLevel(LevelElement[][] layout, DesignLabel designLabel, Point heroPos) {
    super(layout, designLabel);
    // Set Hero Position
    Tile heroTile = this.tileAt(heroPos);
    if (heroTile == null) {
      throw new RuntimeException("Invalid Hero Position: " + heroPos);
    }
    this.startTile(heroTile);
  }

  @Override
  public void onTick(boolean isFirstTick) {
    if (isFirstTick) {
      DialogUtils.showTextPopup("Finde des Ausgang des Labyrinths!", "Ziel");
    }
    if (lastHeroCoords != null && !lastHeroCoords.equals(getHeroCoords())) {
      return;
    }
    this.lastHeroCoords = getHeroCoords();
  }

  public static Coordinate getHeroCoords() {
    try {
      return SkillTools.heroPositionAsPoint().toCoordinate();
    } catch (MissingHeroException e) {
      return null;
    }
  }
}
