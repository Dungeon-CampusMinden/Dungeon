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
import core.utils.IVec2;
import core.utils.MissingHeroException;
import core.utils.components.MissingComponentException;
import entities.BlocklyMonster;
import entities.MiscFactory;
import java.util.List;
import level.BlocklyLevel;
import level.LevelManagementUtils;

/** PRODUS LEVEL. */
public class Chapter35Level extends BlocklyLevel {
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
  public Chapter35Level(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Kapitel 3: Level 5");
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
    LevelManagementUtils.heroViewDirection(PositionComponent.Direction.DOWN);
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
              Tile t = Game.tileAT(coordinate);
              if (t instanceof PitTile pt) {
                pt.timeToOpen(22000);
                pt.close();
              }
            });
    Game.add(MiscFactory.breadcrumb(new Coordinate(25, 14).toCenteredPoint()));
    Game.add(MiscFactory.breadcrumb(new Coordinate(12, 4).toCenteredPoint()));
    Game.add(MiscFactory.breadcrumb(new Coordinate(11, 11).toCenteredPoint()));
    Game.add(MiscFactory.breadcrumb(new Coordinate(11, 16).toCenteredPoint()));
    Game.add(MiscFactory.breadcrumb(new Coordinate(9, 17).toCenteredPoint()));
    Game.add(MiscFactory.breadcrumb(new Coordinate(20, 12).toCenteredPoint()));

    // BOSS
    Coordinate c = Game.randomTile(LevelElement.EXIT).orElseThrow().coordinate().add(IVec2.DOWN);

    BlocklyMonster.BlocklyMonsterBuilder bossBuilder = BlocklyMonster.BLACK_KNIGHT.builder();
    bossBuilder.range(0);
    bossBuilder.addToGame();
    bossBuilder.viewDirection(PositionComponent.Direction.LEFT);
    bossBuilder.spawnPoint(c.toCenteredPoint());
    boss = bossBuilder.build().orElseThrow();
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
