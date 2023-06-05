package starter;

import contrib.configuration.KeyboardConfig;
import contrib.entities.EntityFactory;
import contrib.systems.*;

import core.Game;

import java.io.IOException;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) throws IOException {
        Logger LOGGER = Logger.getLogger("Main");
        // start the game
        Game.setHero(EntityFactory.getHero());
        Game.loadConfig("dungeon_config.json", KeyboardConfig.class);
        Game.frameRate(60);
        Game.disableAudio(true);
        Game.userOnLevelLoad(
                () -> {
                    try {
                        EntityFactory.getChest();
                    } catch (IOException e) {
                        // will be moved to MAIN in
                        // https://github.com/Programmiermethoden/Dungeon/pull/688
                        LOGGER.warning("Could not create new Chest: " + e.getMessage());
                        throw new RuntimeException();
                    }
                });

        // or use the static attributes
        Game.WINDOW_TITLE = "My Dungeon";

        // explicit
        Game.addSystem(new AISystem());
        Game.addSystem(new CollisionSystem());
        Game.addSystem(new HealthSystem());
        // implicit
        new XPSystem();
        new ProjectileSystem();

        // build and start game
        Game.run();
    }
}
