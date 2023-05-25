package starter;

import contrib.configuration.KeyboardConfig;
import contrib.entities.EntityFactory;

import core.Game;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        // start the game
        Game.setHero(EntityFactory.getHero());
        Game.loadConfig("dungeon_config.json", KeyboardConfig.class);
        Game.run(); // hier ist loop
    }
}
