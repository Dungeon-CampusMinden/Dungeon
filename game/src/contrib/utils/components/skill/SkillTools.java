package contrib.utils.components.skill;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import core.Game;
import core.components.PositionComponent;
import core.systems.CameraSystem;
import core.utils.MissingHeroException;
import core.utils.components.MissingComponentException;
import core.utils.position.Point;
import core.utils.position.Position;

/** SkillTools is a collection of helper methods used for skills. */
public class SkillTools {

    /**
     * A skill has a range in which it is effective. This is a calculation of the last position in
     * range of the skill.
     *
     * @param startPosition position to start the calculation
     * @param aimPosition target Position
     * @param range range from startPosition
     * @return last position in range if you follow the direction from startPosition to aimPosition
     */
    public static Position calculateLastPositionInRange(
            Position startPosition, Position aimPosition, float range) {

        // calculate distance from startPosition to aimPosition
        float dx = aimPosition.point().x - startPosition.point().x;
        float dy = aimPosition.point().y - startPosition.point().y;

        // vector from startPosition to aimPosition
        Vector2 scv = new Vector2(dx, dy);

        // normalize the vector (length of 1)
        scv.nor();

        // resize the vector to the length of the range
        scv.scl(range);

        return new Point(startPosition.point().x + scv.x, startPosition.point().y + scv.y);
    }

    /**
     * Calculates the velocity vector to move from the start point to the goal point with the given
     * speed.
     *
     * @param start the starting point
     * @param goal the goal point
     * @param speed the speed of movement
     * @return the velocity vector as a Position object
     */
    public static Position calculateVelocity(Position start, Position goal, float speed) {
        float x1 = start.point().x;
        float y1 = start.point().y;
        float x2 = goal.point().x;
        float y2 = goal.point().y;

        float dx = x2 - x1;
        float dy = y2 - y1;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        float velocityX = dx / distance * speed;
        float velocityY = dy / distance * speed;
        return new Point(velocityX, velocityY);
    }

    /**
     * Gets the current cursor position as Position. The cursor is used to aim.
     *
     * @return mouse cursor position as Position
     */
    public static Position cursorPositionAsPoint() {
        Vector3 mousePosition =
                CameraSystem.camera().unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        return new Point(mousePosition.x, mousePosition.y);
    }

    public static Position heroPositionAsPoint() {
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
