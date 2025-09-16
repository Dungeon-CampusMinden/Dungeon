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
import core.utils.Direction;
import core.utils.MissingHeroException;
import core.utils.components.MissingComponentException;
import entities.MiscFactory;
import entities.monster.BlocklyMonster;
import java.util.List;
import level.BlocklyLevel;
import level.LevelManagementUtils;

/**
 * In this level, the crossword puzzle from the materials is used as a map. The player must navigate
 * over the unstable bridge according to the puzzle.
 */
public class Level021 extends BlocklyLevel {
  private static boolean showText = true;
  private static final int ESCAPE_DISTANCE = 2;

  private PositionComponent heroPC;

  private Entity boss;
  private PositionComponent bossPC;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Level021(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Level 21");
    this.blockBlocklyElement(
        // MOVEMENT
        "goToExit",
        // Variable
        "get_number",
        // Bedingung
        "logic_bossView_direction",
        // Kategorien
        "Sonstige");
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.fog(false);
    LevelManagementUtils.cameraFocusOn(new Coordinate(15, 11));
    LevelManagementUtils.centerHero();
    LevelManagementUtils.zoomDefault();
    LevelManagementUtils.zoomOut();
    LevelManagementUtils.heroViewDirection(Direction.DOWN);
    Game.randomTile(LevelElement.DOOR).ifPresent(d -> ((DoorTile) d).close());

    Game.allTiles(LevelElement.PIT)
        .forEach(
            tile -> {
              ((PitTile) tile).timeToOpen(30);
              ((PitTile) tile).close();
            });

    customPoints()
        .forEach(
            coordinate -> {
              Tile t = Game.tileAt(coordinate).orElse(null);
              if (t instanceof PitTile pt) {
                pt.timeToOpen(22000);
                pt.close();
              }
            });
    Game.add(MiscFactory.breadcrumb(new Coordinate(25, 14).toPoint()));
    Game.add(MiscFactory.breadcrumb(new Coordinate(12, 4).toPoint()));
    Game.add(MiscFactory.breadcrumb(new Coordinate(11, 11).toPoint()));
    Game.add(MiscFactory.breadcrumb(new Coordinate(11, 16).toPoint()));
    Game.add(MiscFactory.breadcrumb(new Coordinate(9, 17).toPoint()));
    Game.add(MiscFactory.breadcrumb(new Coordinate(20, 12).toPoint()));

    // BOSS
    boss =
        BlocklyMonster.BLACK_KNIGHT
            .builder()
            .attackRange(0)
            .addToGame()
            .viewDirection(Direction.LEFT)
            .build(
                Game.randomTile(LevelElement.EXIT)
                    .orElseThrow()
                    .coordinate()
                    .translate(Direction.LEFT));
    bossPC =
        boss.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(boss, PositionComponent.class));

    Entity hero = Game.hero().orElseThrow(MissingHeroException::new);
    heroPC =
        hero.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));

    if (showText) {
      DialogUtils.showTextPopup(
          "Ohne eine Karte wirst du hier nie durch kommen. Gib endlich auf!", "Boss");
      showText = false;
    }
  }

  @Override
  protected void onTick() {
    if (boss == null) return;
    float x = heroPC.position().x();
    if (x >= bossPC.position().x() - ESCAPE_DISTANCE) {
      DialogUtils.showTextPopup(
          "Wie hast du das geschafft? Nur die besten Programmierer hätten dieses Rätsel lösen können.",
          "BOSS");
      Game.remove(boss);
      boss = null;
    }
  }
}
