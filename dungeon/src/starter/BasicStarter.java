package starter;

import contrib.components.CollideComponent;
import contrib.components.KineticComponent;
import contrib.entities.HeroFactory;
import contrib.entities.MiscFactory;
import contrib.systems.CollisionSystem;
import contrib.systems.LevelEditorSystem;
import contrib.systems.ProjectileSystem;
import core.Entity;
import core.Game;
import core.components.VelocityComponent;
import core.configuration.KeyboardConfig;
import core.level.DungeonLevel;
import core.level.loader.DungeonLoader;
import core.utils.Tuple;
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
    DungeonLoader.addLevel(Tuple.of("playground", DungeonLevel.class));
    Game.loadConfig(new SimpleIPath("dungeon_config.json"), KeyboardConfig.class);
    Game.disableAudio(true);
    Game.frameRate(30);
    Game.windowTitle("Playground Dungeon");
    Game.add(HeroFactory.newHero());
    Game.add(new CollisionSystem());
    Game.add(new LevelEditorSystem());
    Game.add(new ProjectileSystem());
    Game.userOnLevelLoad(
        new Consumer<Boolean>() {
          @Override
          public void accept(Boolean aBoolean) {
            try {
              Entity chest = MiscFactory.newChest();
              chest.add(new KineticComponent());
              chest.add(new VelocityComponent(30));
              chest.add(new CollideComponent());
              Game.add(chest);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          }
        });
    Game.run();
  }
}
