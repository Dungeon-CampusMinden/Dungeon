package level.produs;

import contrib.hud.DialogUtils;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.PitTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import java.util.List;
import java.util.function.Consumer;

import entities.BlocklyMonsterFactory;
import level.BlocklyLevel;
import level.LevelManagementUtils;
import server.Server;
import utils.Direction;

public class Chapter34Level extends BlocklyLevel {
  private static boolean showText=true;
  private final int TICK_TIMER =60;
  private Entity boss;
  private PositionComponent bosspc;
  private int counter=0;
  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Chapter34Level(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Kapitel 3: Level 4");
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.cameraFocusOn(new Coordinate(15,8));
    LevelManagementUtils.centerHero();
    LevelManagementUtils.zoomDefault();
    LevelManagementUtils.heroViewDiretion(PositionComponent.Direction.RIGHT);
    ((DoorTile) Game.randomTile(LevelElement.DOOR).get()).close();
    Coordinate c = Game.randomTile(LevelElement.EXIT).get().coordinate();
    c.x-=1;
    boss=BlocklyMonsterFactory.knight(c, PositionComponent.Direction.LEFT, entity -> {});
    bosspc=boss.fetch(PositionComponent.class).get();
    Game.add(boss);
    Game.allTiles(LevelElement.PIT).forEach(new Consumer<Tile>() {
      @Override
      public void accept(Tile tile) {
        ((PitTile) tile).timeToOpen(120);

        ((PitTile) tile).close();
      }
    });

    if (showText) {
      DialogUtils.showTextPopup(
              "Pass auf. Das Monster da vorne kann nur Bewegung wahrnehmen.",
              "Kapitel 3: Rache");
      showText = false;
    }
  }

  @Override
  protected void onTick() {
    counter++;
    if(counter>=TICK_TIMER){
      counter=0;
      if(bosspc.viewDirection()== PositionComponent.Direction.LEFT){
        System.out.println("TURN RIGHT");
        Server.turnEntity(boss, Direction.RIGHT);
        //TODO CHECK IF HERO MOVES IF YES KILL HERO
      }
      else {
        Server.turnEntity(boss, Direction.LEFT);
        System.out.println("TURN LEFT");
      }
    }
  }
}
