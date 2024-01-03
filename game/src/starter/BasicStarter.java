package starter;

import core.Game;
import core.configuration.KeyboardConfig;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;

public class BasicStarter {

    public static void main(String[] args) throws IOException {
        Game.initBaseLogger();
        Game.loadConfig(new SimpleIPath("dungeon_config.json"), KeyboardConfig.class);
        Game.disableAudio(true);
        Game.frameRate(30);
        Game.windowTitle("Basic Dungeon");
        Game.run();
    }
}
