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
        Game.hero(EntityFactory.newHero());
        Game.loadConfig("dungeon_config.json", KeyboardConfig.class);
        Game.frameRate(30);
        Game.disableAudio(true);
        Game.userOnLevelLoad(
                () -> {
                    try {
                        EntityFactory.newChest();
                    } catch (IOException e) {
                        LOGGER.warning("Could not create new Chest: " + e.getMessage());
                        throw new RuntimeException();
                    }
                });

        Game.windowTitle("My Dungeon");
        Game.addSystem(new AISystem());
        Game.addSystem(new CollisionSystem());
        Game.addSystem(new HealthSystem());
        Game.addSystem(new XPSystem());
        Game.addSystem(new ProjectileSystem());
        Game.addSystem(new HealthbarSystem());
        // build and start game
        Game.run();
    }
}
