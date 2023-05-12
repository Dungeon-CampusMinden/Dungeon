package starter;

import configuration.Configuration;
import configuration.KeyboardConfig;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        // start the game
        Game.loadConfig("dungeon_config.json",KeyboardConfig.class);
        Game.onLevelLoad(()->xyz);
        Game.registerSystem(new MyStudiSystem());




        Game.run(); //hier ist loop
    }
}
