package level.produs;

import contrib.hud.DialogUtils;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.PitTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import entities.BlocklyMonsterFactory;
import level.BlocklyLevel;
import level.LevelManagementUtils;
import server.Server;
import utils.Direction;

import java.util.List;
import java.util.function.Consumer;

public class Chapter35Level extends BlocklyLevel {
  private static boolean showText = true;
  private Entity boss;
  private PositionComponent bosspc;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Chapter35Level(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Kapitel 3: Level 5");
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.cameraFocusOn(new Coordinate(15, 8));
    LevelManagementUtils.centerHero();
    LevelManagementUtils.zoomDefault();
    LevelManagementUtils.heroViewDiretion(PositionComponent.Direction.RIGHT);
   // boss = BlocklyMonsterFactory.knight(c, PositionComponent.Direction.LEFT, entity -> {});
   // bosspc = boss.fetch(PositionComponent.class).get();
   // Game.add(boss);
    Game.allTiles(LevelElement.PIT)
        .forEach(
            new Consumer<Tile>() {
              @Override
              public void accept(Tile tile) {
                ((PitTile) tile).timeToOpen(120);

                ((PitTile) tile).close();
              }
            });

    if (showText) {
      DialogUtils.showTextPopup(
          "Pass auf. Das Monster da vorne kann nur Bewegung wahrnehmen.", "Kapitel 3: Rache");
      showText = false;
    }
  }

  @Override
  protected void onTick() {
  }
}
