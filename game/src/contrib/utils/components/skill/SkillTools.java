package contrib.utils.components.skill;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import core.Game;
import core.components.PositionComponent;
import core.systems.CameraSystem;
import core.utils.MissingHeroException;
import core.utils.Point;
import core.utils.components.MissingComponentException;

/** SkillTools is a collection of helper methods used for skills. */
public class SkillTools {

    /**
     * A skill has a range in which it is effective. This is a calculation of the last position in
     * range of the skill.
     *
     * @param startPoint position to start the calculation
     * @param aimPoint target Point
     * @param range range from startPoint
     * @return last position in range if you follow the direction from startPoint to aimPoint
     */
    public static Point calculateLastPositionInRange(
            Point startPoint, Point aimPoint, float range) {

        // calculate distance from startPoint to aimPoint
        float dx = aimPoint.x - startPoint.x;
        float dy = aimPoint.y - startPoint.y;

        // vector from startPoint to aimPoint
        Vector2 scv = new Vector2(dx, dy);

        // normalize the vector (length of 1)
        scv.nor();

        // resize the vector to the length of the range
        scv.scl(range);

        return new Point(startPoint.x + scv.x, startPoint.y + scv.y);
    }

    /**
     * Calculates the velocity vector to move from the start point to the goal point with the given
     * speed.
     *
     * @param start the starting point
     * @param goal the goal point
     * @param speed the speed of movement
     * @return the velocity vector as a Point object
     */
    public static Point calculateVelocity(Point start, Point goal, float speed) {
        float x1 = start.x;
        float y1 = start.y;
        float x2 = goal.x;
        float y2 = goal.y;

        float dx = x2 - x1;
        float dy = y2 - y1;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        float velocityX = dx / distance * speed;
        float velocityY = dy / distance * speed;
        return new Point(velocityX, velocityY);
    }

    /**
     * Gets the current cursor position as Point. The cursor is used to aim.
     *
     * @return mouse cursor position as Point
     */
    public static Point cursorPositionAsPoint() {
        Vector3 mousePosition =
                CameraSystem.camera().unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        return new Point(mousePosition.x, mousePosition.y);
    }

    /**
     * Gets the current hero position as Point.
     *
     * @return the current hero position as Point
     */
    public static Point heroPositionAsPoint() {
        PositionComponent pc =
                Game.hero()
                        .orElseThrow(
                                () -> new MissingHeroException("Can't fetch position of hero."))
                        .fetch(PositionComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                Game.hero().get(), PositionComponent.class));
        return pc.position();
    }
}
