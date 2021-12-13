package client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import controller.MainController;
import controller.LibgdxSetup;
import tools.Constants;

public final class HtmlLauncher extends GwtApplication {
    @Override
    public GwtApplicationConfiguration getConfig() {
        // Resizable application, uses available space in browser
        // return new GwtApplicationConfiguration(true);
        // Fixed size application:
        GwtApplicationConfiguration config =
                new GwtApplicationConfiguration(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        config.disableAudio = true;
        return config;
    }

    @Override
    public ApplicationListener createApplicationListener() {
        return new LibgdxSetup(new MainController());
    }
}
