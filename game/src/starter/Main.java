package starter;

import contrib.entities.EntityFactory;
import contrib.systems.*;
import contrib.utils.components.Debugger;

import core.Game;
import core.level.utils.LevelSize;

import java.io.IOException;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) throws IOException {
        Game.initBaseLogger();
        Logger LOGGER = Logger.getLogger("Main");
        Debugger debugger = new Debugger();
        // start the game
        Game.hero(EntityFactory.newHero());
        Game.loadConfig(
                "dungeon_config.json",
                contrib.configuration.KeyboardConfig.class,
                core.configuration.KeyboardConfig.class);
        Game.frameRate(30);
        Game.disableAudio(true);
        Game.userOnLevelLoad(
                () -> {
                    try {
                        EntityFactory.newChest();
                        for (int i = 0; i < 5; i++) {
                            EntityFactory.randomMonster();
                        }
                    } catch (IOException e) {
                        LOGGER.warning("Could not create new Chest: " + e.getMessage());
                        throw new RuntimeException();
                    }
                    Game.levelSize(LevelSize.randomSize());
                });
        Game.userOnFrame(debugger::execute);
        Game.windowTitle("My Dungeon");
        Game.add(new AISystem());
        Game.add(new CollisionSystem());
        Game.add(new HealthSystem());
        Game.add(new XPSystem());
        Game.add(new ProjectileSystem());
        Game.add(new HealthbarSystem());
        Game.add(new HeroUISystem());
        // build and start game
        Game.run();
    }
}
