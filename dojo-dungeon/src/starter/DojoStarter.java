package starter;

import core.Game;
import core.utils.components.path.SimpleIPath;

import java.io.IOException;

public class DojoStarter {
    public static void main(String[] args) throws IOException {
        Game.initBaseLogger();
        Game.loadConfig(new SimpleIPath("dungeon_config.json"), KeyboardConfig.class);
        Game.disableAudio(true);
        Game.frameRate(30);
        Game.windowTitle("Dojo-Dungeon");
        Game.run();
    }
}
