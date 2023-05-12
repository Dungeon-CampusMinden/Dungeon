package starter;

import configuration.Configuration;
import configuration.KeyboardConfig;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        // start the game
        try {
            Configuration.loadAndGetConfiguration("dungeon_config.json", KeyboardConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Game.LibgdxSetup.run(new Game());
    }
}
