package desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public final class DesktopLauncher {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = 640;
        config.height = 480;
        config.foregroundFPS = 30;
        new LwjglApplication(
                new Game() {
                    public SpriteBatch batch;

                    @Override
                    public void create() {
                        batch = new SpriteBatch();
                    }

                    @Override
                    public void dispose() {
                        batch.dispose();
                    }
                },
                config);
    }
}
