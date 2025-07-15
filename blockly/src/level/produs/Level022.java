package level.produs;

import contrib.components.HealthComponent;
import contrib.hud.DialogUtils;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.ExitTile;
import core.level.elements.tile.PitTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.MissingHeroException;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;
import entities.BlocklyMonster;
import java.util.List;
import level.BlocklyLevel;
import level.LevelManagementUtils;

/**
 * In this final level, the boss mirrors the hero's movements. The player must cleverly lure the
 * boss into the abyss to win.
 */
public class Level022 extends BlocklyLevel {
  private static boolean showText = true;
  private VelocityComponent heroVC;
  boolean openExit = true;

  private Entity boss;
  private VelocityComponent bossVC;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Level022(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Level 22");
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
    LevelManagementUtils.cameraFocusOn(new Coordinate(15, 8));
    LevelManagementUtils.centerHero();
    LevelManagementUtils.zoomDefault();
    LevelManagementUtils.heroViewDirection(PositionComponent.Direction.RIGHT);
    Game.randomTile(LevelElement.DOOR).ifPresent(d -> ((DoorTile) d).close());
    Game.randomTile(LevelElement.EXIT).ifPresent(d -> ((ExitTile) d).close());

    BlocklyMonster.BlocklyMonsterBuilder bossBuilder = BlocklyMonster.BLACK_KNIGHT.builder();
    bossBuilder.range(0);
    bossBuilder.addToGame();
    bossBuilder.viewDirection(PositionComponent.Direction.LEFT);
    bossBuilder.spawnPoint(customPoints().get(0).toCenteredPoint());

    boss = bossBuilder.build().orElseThrow();
    boss.fetch(HealthComponent.class)
        .orElseThrow()
        .onDeath(
            entity -> {
              DialogUtils.showTextPopup("NEEEEEEEEEEEEEEEEIN! ICH WERDE MICH RÄCHEN!", "SIEG!");
              boss = null;
            });
    bossVC =
        boss.fetch(VelocityComponent.class)
            .orElseThrow(() -> MissingComponentException.build(boss, VelocityComponent.class));

    Entity hero = Game.hero().orElseThrow(MissingHeroException::new);
    heroVC =
        hero.fetch(VelocityComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, VelocityComponent.class));
    bossVC.velocity(heroVC.velocity());
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
    if (boss != null) {
      bossVC.currentVelocity(Vector2.of(heroVC.currentVelocity().scale(-1).scale(14)));
    } else if (openExit) Game.randomTile(LevelElement.EXIT).ifPresent(d -> ((ExitTile) d).open());
  }
}
