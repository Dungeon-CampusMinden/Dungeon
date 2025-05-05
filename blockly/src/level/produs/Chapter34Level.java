package level.produs;

import client.Client;
import contrib.hud.DialogUtils;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.PitTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.MissingHeroException;
import core.utils.components.MissingComponentException;
import entities.BlocklyMonsterFactory;
import java.util.List;
import level.BlocklyLevel;
import level.LevelManagementUtils;
import server.Server;
import utils.Direction;

/** PRODUS LEVEL. */
public class Chapter34Level extends BlocklyLevel {
  private static boolean showText = true;
  private static final int TICK_TIMER = 60;
  private static final int ESCAPE_DISTANCE = 2;

  private PositionComponent heropc;
  private VelocityComponent herovc;

  private Entity boss;
  private PositionComponent bosspc;
  private int counter = 0;

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
    this.blockBlocklyElement(
        // MOVEMENT
        "goToExit",
        // Variable
        "get_number",
        // Kategorien
        // Kategorien
        "Sonstige");
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.fog(false);
    LevelManagementUtils.cameraFocusOn(new Coordinate(15, 8));
    LevelManagementUtils.centerHero();
    LevelManagementUtils.zoomDefault();
    LevelManagementUtils.heroViewDirection(PositionComponent.Direction.RIGHT);
    Entity hero = Game.hero().orElseThrow(MissingHeroException::new);
    heropc =
        hero.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));
    herovc =
        hero.fetch(VelocityComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, VelocityComponent.class));

    ((DoorTile) Game.randomTile(LevelElement.DOOR).orElseThrow()).close();
    Coordinate c = Game.randomTile(LevelElement.EXIT).orElseThrow().coordinate();
    c.x -= 1;
    boss = BlocklyMonsterFactory.knight(c, PositionComponent.Direction.RIGHT, entity -> {});
    bosspc =
        boss.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(boss, PositionComponent.class));
    Game.add(boss);
    Game.allTiles(LevelElement.PIT)
        .forEach(
            tile -> {
              ((PitTile) tile).timeToOpen(120);
              ((PitTile) tile).close();
            });

    if (showText) {
      DialogUtils.showTextPopup(
          "Von dir lass ich mich nicht besiegen! Meine Waffe erkennt jede Bewegung und vernichtet dich auf der Stelle!",
          "BOSS");
      showText = false;
    }
  }

  @Override
  protected void onTick() {
    if (boss == null) return;
    counter++;
    if (counter >= TICK_TIMER) {
      counter = 0;
      if (bosspc.viewDirection() == PositionComponent.Direction.LEFT) {
        Server.turnEntity(boss, Direction.RIGHT);
      } else {
        Server.turnEntity(boss, Direction.LEFT);
      }
    }
    // create save scone at stat of the level
    float x = heropc.position().x;
    float y = heropc.position().y;
    if (x > 6 || x > 3 && y >= 6 && y <= 8) {

      if (bosspc.viewDirection() == PositionComponent.Direction.LEFT) {
        if (herovc.currentXVelocity() > 0 || herovc.currentYVelocity() > 0) {
          DialogUtils.showTextPopup("HAB ICH DICH!", "GAME OVER!", Client::restart);
        }
      }
      if (x >= bosspc.position().x - ESCAPE_DISTANCE) {
        DialogUtils.showTextPopup("Mich kriegst du nie!", "BOSS");
        Game.remove(boss);
        boss = null;
      }
    }
  }
}
