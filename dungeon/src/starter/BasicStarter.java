package starter;

import contrib.entities.HeroFactory;
import contrib.entities.MiscFactory;
import contrib.systems.*;
import core.Game;
import core.components.*;
import core.configuration.KeyboardConfig;
import core.level.DungeonLevel;
import core.level.loader.DungeonLoader;
import core.utils.*;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * @return WTF? .
 */
public class BasicStarter {

  /**
   * WTF? .
   *
   * @param args foo
   * @throws IOException foo
   */
  public static void main(String[] args) throws IOException {
    Game.initBaseLogger(Level.WARNING);
    DungeonLoader.addLevel(Tuple.of("maze", DungeonLevel.class));
    Game.loadConfig(new SimpleIPath("dungeon_config.json"), KeyboardConfig.class);
    Game.disableAudio(true);
    Game.frameRate(30);
    Game.add(new ProjectileSystem());
    Game.add(HeroFactory.newHero());
    Game.add(new LevelEditorSystem());
    Game.add(new CollisionSystem());
    Game.add(new EventScheduler());
    Game.add(new FallingSystem());
    Game.add(new HealthSystem());
    Game.userOnLevelLoad(
        new Consumer<Boolean>() {
          @Override
          public void accept(Boolean aBoolean) {
            Game.add(
                MiscFactory.catapult(
                    new Point(1, 1).toCenteredPoint(), new Point(5, 5).toCenteredPoint(), 10));
            Game.add(MiscFactory.marker(new Point(5, 5).toCenteredPoint()));
          }
        });
    Game.windowTitle("Basic Dungeon");
    Game.run();
  }
}
