package level.produs;

import contrib.hud.DialogUtils;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.PitTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.MissingHeroException;
import core.utils.components.MissingComponentException;
import entities.BlocklyMonsterFactory;
import java.util.List;
import java.util.function.Consumer;
import level.BlocklyLevel;
import level.LevelManagementUtils;

public class Chapter35Level extends BlocklyLevel {
  private static boolean showText = true;
  private static final int ESCAPE_DISTANCE = 2;

  private PositionComponent heropc;
  private VelocityComponent herovc;

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
            new Consumer<Coordinate>() {
              @Override
              public void accept(Coordinate coordinate) {
                Tile t = Game.tileAT(coordinate);
                if (t instanceof PitTile pt) {
                  pt.timeToOpen(5000);
                  pt.close();
                }
              }
            });

    // BOSS
    Coordinate c = Game.randomTile(LevelElement.EXIT).get().coordinate();
    c.x -= 1;
    boss = BlocklyMonsterFactory.knight(c, PositionComponent.Direction.LEFT, entity -> {});
    Game.add(boss);
    bosspc = boss.fetch(PositionComponent.class).get();

    Entity hero = Game.hero().orElseThrow(() -> new MissingHeroException());
    heropc =
        hero.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));
    herovc =
        hero.fetch(VelocityComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, VelocityComponent.class));

    if (showText) {
      DialogUtils.showTextPopup(
          "Ohne eine Karte wirst du hier nie durch kommen. Gib endlich auf!", "Boss");
      showText = false;
    }
  }

  @Override
  protected void onTick() {
    if (boss == null) return;
    // create save scone at stat of the level
    float x = heropc.position().x;
    float y = heropc.position().y;
    if (x >= bosspc.position().x - ESCAPE_DISTANCE) {
      DialogUtils.showTextPopup(
          "Wie hast du das geschafft? Nur die besten Programmierer hätten dieses Rätsel lösen können.",
          "BOSS");
      Game.remove(boss);
      boss = null;
    }
  }
}
