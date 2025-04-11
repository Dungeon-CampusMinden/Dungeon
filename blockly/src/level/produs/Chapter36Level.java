package level.produs;

import contrib.hud.DialogUtils;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.ExitTile;
import core.level.elements.tile.PitTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.MissingHeroException;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import entities.BlocklyMonsterFactory;
import java.util.List;
import level.BlocklyLevel;
import level.LevelManagementUtils;

public class Chapter36Level extends BlocklyLevel {
  private static boolean showText = true;
  private PositionComponent heropc;
  private VelocityComponent herovc;
  boolean openExit = true;

  private Entity boss;
  private PositionComponent bosspc;
  private VelocityComponent bossvc;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Chapter36Level(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Kapitel 3: Level 6");
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.fog(false);
    LevelManagementUtils.cameraFocusOn(new Coordinate(15, 8));
    LevelManagementUtils.centerHero();
    LevelManagementUtils.zoomDefault();
    LevelManagementUtils.heroViewDirection(PositionComponent.Direction.RIGHT);
    Game.randomTile(LevelElement.DOOR).ifPresent(d -> ((DoorTile) d).close());
    Game.randomTile(LevelElement.EXIT).ifPresent(d -> ((ExitTile) d).close());

    boss =
        BlocklyMonsterFactory.knight(
            customPoints().get(0),
            PositionComponent.Direction.LEFT,
            entity -> {
              DialogUtils.showTextPopup("NEEEEEEEEEEEEEEEEIN! ICH WERDE MICH RÄCHEN!", "SIEG!");
              boss = null;
            });
    Game.add(boss);
    bosspc = boss.fetch(PositionComponent.class).get();
    bossvc = boss.fetch(VelocityComponent.class).get();

    Entity hero = Game.hero().orElseThrow(() -> new MissingHeroException());
    heropc =
        hero.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));
    herovc =
        hero.fetch(VelocityComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, VelocityComponent.class));
    bossvc.xVelocity(herovc.xVelocity());
    bossvc.yVelocity(herovc.yVelocity());
    Game.allTiles(LevelElement.PIT)
        .forEach(
            tile -> {
              ((PitTile) tile).timeToOpen(60);
              ((PitTile) tile).close();
            });

    if (showText) {
      DialogUtils.showTextPopup(
          "Jetzt ist Schluss mit lustig. Ich kopiere jede deiner Bewegungen, dann besiegst du mich nie.",
          "BOSS");
      showText = false;
    }
  }

  @Override
  protected void onTick() {
    Tile t1 = Game.randomTile(LevelElement.FLOOR).get();
    Point t1Center = t1.position().toCoordinate().toCenteredPoint();
    Tile t2 = Game.tileAT(new Point(t1Center.x - 1, t1Center.y));
    Point t2Center = t2.position().toCoordinate().toCenteredPoint();
    System.out.println(t2Center.x - t1Center.x);

    if (boss != null) {
      bossvc.currentXVelocity(herovc.currentXVelocity() * -1 * 16);
      bossvc.currentYVelocity(herovc.currentYVelocity() * -1 * 16);
    } else if (openExit) Game.randomTile(LevelElement.EXIT).ifPresent(d -> ((ExitTile) d).open());
  }
}
