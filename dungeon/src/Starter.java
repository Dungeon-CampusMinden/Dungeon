import contrib.entities.EntityFactory;
import contrib.systems.*;
import contrib.utils.components.Debugger;

import core.Game;
import core.level.utils.LevelSize;

import dslToGame.DslFileLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.logging.Logger;

public class Starter {
    public static void main(String[] args) throws IOException {
        setupBasicGame();
        Set<Path> path = DslFileLoader.processArguments(args);
        path.forEach(p -> System.out.println(p));
        Game.run();
    }

    private static void setupBasicGame() throws IOException {
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
        Game.windowTitle("DSL Dungeon");
        Game.addSystem(new AISystem());
        Game.addSystem(new CollisionSystem());
        Game.addSystem(new HealthSystem());
        Game.addSystem(new XPSystem());
        Game.addSystem(new ProjectileSystem());
        Game.addSystem(new HealthbarSystem());
        Game.addSystem(new HeroUISystem());
        // build and start game
    }
}
