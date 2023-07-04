package starter;

import contrib.configuration.ItemConfig;
import contrib.entities.EntityFactory;
import contrib.systems.*;
import contrib.utils.components.Debugger;

import core.Game;

import java.io.IOException;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) throws IOException {
        Logger LOGGER = Logger.getLogger("Main");
        Debugger debugger = new Debugger();
        // start the game
        Game.hero(EntityFactory.newHero());
        Game.loadConfig(
                "dungeon_config.json",
                contrib.configuration.KeyboardConfig.class,
                core.configuration.KeyboardConfig.class,
                ItemConfig.class);
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
                });
        Game.userOnFrame(debugger::execute);
        Game.windowTitle("My Dungeon");
        Game.addSystem(new AISystem());
        Game.addSystem(new CollisionSystem());
        Game.addSystem(new HealthSystem());
        Game.addSystem(new XPSystem());
        Game.addSystem(new ProjectileSystem());

        // build and start game
        Game.run();
    }
}
