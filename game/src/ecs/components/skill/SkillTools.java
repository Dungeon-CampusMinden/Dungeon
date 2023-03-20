package ecs.components.skill;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import starter.Game;
import tools.Point;

public class SkillTools {

    /**
     * Calculates the last position in range regardless of cursor position
     *
     * @param startPoint start point
     * @param goalPoint goal point
     * @param range range from start point
     * @param speed speed
     * @return closest Point to goalPoint given the range
     */
    public static Point calculateLastPositionInRange(
            Point startPoint, Point goalPoint, float range, float speed) {
        // TODO fix bug where return point is wrong if goalPoint is out of radius

        // calculate the distance between start point and goal point
        float tmpdx = startPoint.x - goalPoint.x;
        float tmpdy = startPoint.y - goalPoint.y;
        double distance = Math.sqrt(tmpdx * tmpdx + tmpdy * tmpdy);

        // calculate the time it takes to travel the distance at the given speed
        double time = distance / speed;

        // calculate the direction vector from the start point to the goal point
        double dx = (goalPoint.x - startPoint.x) / distance;
        double dy = (goalPoint.y - startPoint.y) / distance;

        // calculate the final position based on the time and speed
        double finalX = startPoint.x + time * speed * dx;
        double finalY = startPoint.y + time * speed * dy;

        // check if the final position is within the range of the start point
        double dx2 = startPoint.x - finalX;
        double dy2 = startPoint.y - finalY;
        double finalDistance = Math.sqrt(dx2 * dx2 + dy2 * dy2);
        if (finalDistance <= range) {
            double closestX = startPoint.x + (finalX - startPoint.x) * range / finalDistance;
            double closestY = startPoint.y + (finalY - startPoint.y) * range / finalDistance;
            return new Point((int) closestX, (int) closestY);
        }

        // calculate the direction and distance from the final position back to the start point
        double backDx = (startPoint.x - finalX) / finalDistance;
        double backDy = (startPoint.y - finalY) / finalDistance;
        double backDistance = range;

        // calculate the position within the range of the start point
        double rangeX = finalX + backDistance * backDx;
        double rangeY = finalY + backDistance * backDy;

        return new Point((float) rangeX, (float) rangeY);
    }

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
     * gets the current cursor position as Point
     *
     * @return mouse cursor position as Point
     */
    public static Point getCursorPositionAsPoint() {
        Vector3 mousePosition =
                Game.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        return new Point(mousePosition.x, mousePosition.y);
    }
}
