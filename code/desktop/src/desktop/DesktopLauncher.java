package desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import controller.MainController;
import controller.LibgdxSetup;
import tools.Constants;

public final class DesktopLauncher {
    public static void run(MainController mc) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = Constants.WINDOW_WIDTH;
        config.height = Constants.WINDOW_HEIGHT;
        config.foregroundFPS = Constants.FRAME_RATE;
        new LwjglApplication(new LibgdxSetup(mc), config);
    }

    public static void main(String[] args) {
        DesktopLauncher.run(new MainController());
    }
}
