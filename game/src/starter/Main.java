package starter;

import contrib.configuration.KeyboardConfig;
import contrib.entities.EntityFactory;
import contrib.systems.*;

import core.Game;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        // start the game
        Game.setHero(EntityFactory.getHero());
        Game.loadConfig("dungeon_config.json", KeyboardConfig.class);
        Game.frameRate(60);
        Game.disableAudio(true);
        Game.userOnLevelLoad(EntityFactory::getChest);

        // or use the static attributes
        Game.WINDOW_TITLE = "My Dungeon";

        // explicit
        Game.addSystem(new AISystem());
        Game.addSystem(new CollisionSystem());
        Game.addSystem(new HealthSystem());
        // implicit
        new XPSystem();
        new SkillSystem();
        new ProjectileSystem();

        // build and start game
        Game.run();
    }
}
