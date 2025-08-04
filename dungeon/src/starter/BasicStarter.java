package starter;

import contrib.entities.HeroFactory;
import contrib.entities.MonsterFactory;
import core.Entity;
import core.Game;
import core.configuration.KeyboardConfig;
import core.level.DungeonLevel;
import core.level.loader.DungeonLoader;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
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
    Game.windowTitle("Basic Dungeon");

    Game.userOnSetup(
        () -> {
          try {
            Entity hero = HeroFactory.newHero();
            Game.add(hero);

            Entity monster = MonsterFactory.randomMonster();
            Game.add(monster);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }

          DungeonLoader.loadLevel(0);
        });
    Game.run();
  }
}
