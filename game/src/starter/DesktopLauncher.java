package starter;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import tools.Constants;

/** A class to wrap the passed {@link Game} and start the dungeon. */
public final class DesktopLauncher {
    /**
     * Starts the dungeon and needs a {@link Game}.
     *
     * @param game the {@link Game} used to start the dungeon.
     */
    public static void run(Game game) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowSizeLimits(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT, 9999, 9999);
        // The third and fourth parameters ("maxWidth" and "maxHeight") affect the resizing behavior
        // of the window. If the window is enlarged or maximized, then it can assume these
        // dimensions at maximum. If you have a larger screen resolution than 9999x9999 pixels,
        // increase these parameters.
        config.setForegroundFPS(Constants.FRAME_RATE);
        config.setTitle(Constants.WINDOW_TITLE);
        config.setWindowIcon(Constants.LOGO_PATH);
        // config.disableAudio(true);
        // uncomment this if you wish no audio
        new Lwjgl3Application(new LibgdxSetup(game), config);
    }
}
