package starter;

import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.components.skill.SkillTools;
import tools.Point;

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

    /** Teleport the Hero to the current cursor Position */
    public static void TELEPORT_TO_CURSOR() {
        PositionComponent pc =
                (PositionComponent)
                        Game.hero
                                .getComponent(PositionComponent.class)
                                .orElseThrow(
                                        () ->
                                                new MissingComponentException(
                                                        "Hero is missing PositionComponent"));
        Point cP = SkillTools.getCursorPositionAsPoint();
        if (Game.currentLevel.getTileAt(cP.toCoordinate()).isAccessible())
            pc.setPosition(SkillTools.getCursorPositionAsPoint());
    }
}
