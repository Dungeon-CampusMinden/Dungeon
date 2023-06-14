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
        Game.FRAME_RATE = 60;
        Game.DISABLE_AUDIO = true;
        Game.WINDOW_TITLE = "My Dungeon";
        // spawn a chest
        Game.userOnLevelLoad = EntityFactory::getChest;
        new AISystem();
        new CollisionSystem();
        new HealthSystem();
        new XPSystem();
        new SkillSystem();
        new ProjectileSystem();
        // start game
        Game.run();
    }
}
