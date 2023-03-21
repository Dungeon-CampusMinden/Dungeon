package starter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.components.skill.SkillTools;
import ecs.systems.ECS_System;
import tools.Point;

/** Collection of functions for easy debugging */
public class Debugger extends ECS_System {

    @Override
    public void update() {
        // DEBUGGER
        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) Debugger.ZOOM_CAMERA(-1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.K)) Debugger.ZOOM_CAMERA(1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.O)) Debugger.TELEPORT_TO_CURSOR();
    }

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
