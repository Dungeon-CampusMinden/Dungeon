package starter;

/** Collection of functions for easy debugging */
public class DebugTools {

    /**
     * Zoom in or out
     *
     * @param amount Zoom length
     */
    public static void ZOOM_CAMERA(float amount) {
        Game.camera.zoom = Math.max(0.1f, Game.camera.zoom + amount);
    }
}
