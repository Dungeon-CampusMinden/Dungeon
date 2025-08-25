package mushroomDungeon.level;

import contrib.components.AIComponent;
import contrib.components.CollideComponent;
import contrib.components.HealthComponent;
import contrib.components.SpikyComponent;
import contrib.entities.MiscFactory;
import contrib.item.concreteItem.ItemHammer;
import contrib.item.concreteItem.ItemKey;
import contrib.utils.components.ai.fight.AIChaseBehaviour;
import contrib.utils.components.ai.idle.RadiusWalk;
import contrib.utils.components.ai.transition.RangeTransition;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.DungeonLevel;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.components.draw.Animation;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class Level01 extends DungeonLevel {
  public static final SimpleIPath WATER_TEXTURE = new SimpleIPath("tile/water.png");
  // CUSTOM POINTS //
  private Coordinate cp_npc = customPoints.get(0);
  private Coordinate cp_cookingpot = customPoints.get(1);

  private Coordinate cp_tent = customPoints.get(2);
  private Coordinate cp_wolf = customPoints.get(3);
  private Coordinate cp_hiddenchest = customPoints.get(4);
  private Coordinate cp_key = customPoints.get(5);
  private Coordinate cp_keyBlock1 = customPoints.get(6);
  private Coordinate cp_keyBlock2 = customPoints.get(7);
  private Coordinate cp_keyBlock3 = customPoints.get(8);
  private Coordinate cp_keyBlock4 = customPoints.get(9);
  private Coordinate cp_stick = customPoints.get(10);

  public Level01(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Mushroom Adventure");
  }

  @Override
  public void onFirstTick() {
    makePitsWater();
    try {
      setupChestRiddle();
      setupWolf();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void setupWolf() throws IOException {
    Entity wolf = new Entity("Wolf");
    wolf.add(new PositionComponent(cp_wolf.toCenteredPoint()));
    wolf.add(new VelocityComponent(4));
    wolf.add(
        new HealthComponent(
            10,
            new Consumer<Entity>() {
              @Override
              public void accept(Entity entity) {
                // TODO SPAWN BOOK PAGE
                Game.remove(entity);
              }
            }));
    wolf.add(new CollideComponent());
    wolf.add(new SpikyComponent(3, DamageType.PHYSICAL, 2 * Game.frameRate()));
    DrawComponent dc = new DrawComponent(new SimpleIPath("character/monster/ogre"));
    wolf.add(dc);
    wolf.add(
        new AIComponent(new AIChaseBehaviour(5), new RadiusWalk(9, 2), new RangeTransition(9)));

    Game.add(wolf);
  }

  private void setupChestRiddle() throws IOException {
    new ItemHammer().drop(cp_stick.toCenteredPoint());
    new ItemKey().drop(cp_key.toCenteredPoint());
    Game.add(MiscFactory.newStone(cp_keyBlock1.toCenteredPoint(), 0f));
    Game.add(MiscFactory.newStone(cp_keyBlock2.toCenteredPoint(), 0f));
    Game.add(MiscFactory.newStone(cp_keyBlock3.toCenteredPoint(), 0f));
    Game.add(MiscFactory.newStone(cp_keyBlock4.toCenteredPoint(), 0f));
    // TODO add Book page
    Game.add(MiscFactory.newLockedChest(Set.of(), cp_hiddenchest.toCenteredPoint(), ItemKey.class));
  }

  private void makePitsWater() {
    Game.allTiles(LevelElement.PIT)
        .forEach(
            tile -> {
              Entity waterTile = new Entity("Water");
              waterTile.add(new PositionComponent(tile.coordinate().toCenteredPoint()));
              waterTile.add(new DrawComponent(Animation.fromSingleImage(WATER_TEXTURE)));
              Game.add(waterTile);
            });
  }

  @Override
  public void onTick() {}
}
