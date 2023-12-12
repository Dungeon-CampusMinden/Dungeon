package core;

public class Assets {

    /** Private Constructor to prevent instantiation */
    private Assets() {}

    public enum Images implements Asset {
        DEBUG_IMAGE("/debug/debug.png"),
        HUD_BUTTON_HOVER("/hud/button/button_hover.png"),
        HUD_BUTTON_IDLE("/hud/button/button_idle.png"),
        HUD_BUTTON_PRESS("/hud/button/button_press.png");

        private final String path;

        Images(String path) {
            this.path = path;
        }

        @Override
        public String path() {
            return this.path;
        }
    }

    public enum Fonts implements Asset {
        OPENSANS_REGULAR("/fonts/opensans_regular.ttf"),
        OPENSANS_VARIABLE("/fonts/opensans_variable.ttf");

        private final String path;

        Fonts(String path) {
            this.path = path;
        }

        @Override
        public String path() {
            return this.path;
        }
    }

    public interface Asset {
        String path = "";

        String path();
    }
}
