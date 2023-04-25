package game.src.ecs.components.ai.idle;

import com.badlogic.gdx.ai.pfa.GraphPath;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.components.ai.AITools;
import ecs.entities.Entity;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import level.elements.tile.Tile;
import level.elements.ILevel;
import starter.Game;
import tools.Constants;
import tools.Point;
import ecs.components.ai.idle.IIdleAI;

public class CircleWalk implements IIdleAI {

    private int breakTime;
    private float radius;
    private int breakTimer;
    private int counter;

    /**
     * Wanders in a "circle" (square)  down, left, up and right 
     * (Often parts will be left out due to collisions with the walls)
     * 
     * @param radius distance that will be traveled each time
     * @param breakTime how long to wait (in seconds) before wandering again
     */

    public CircleWalk(float radius, int breakTime) {
        this.radius = radius;
        this.breakTime = breakTime * Constants.FRAME_RATE;
    }

    @Override
    public void idle(Entity entity) {
        if (breakTimer < breakTime) {
            breakTimer++;
            return;
        }
        Point current = ((PositionComponent) entity.getComponent(PositionComponent.class).get()).getPosition();
        switch (counter) {
            case 0:
                try {
                    AITools.move(entity, AITools.calculatePath(current, new Point(current.x - radius, current.y)));
                    breakTimer = 0;
                } catch (Exception e) {
                    // TODO: handle exception
                }
                counter++;
                break;

            case 1:
                try {
                    AITools.move(entity, AITools.calculatePath(current, new Point(current.x, current.y - radius)));
                    breakTimer = 0;
                } catch (Exception e) {
                    // TODO: handle exception
                }
                counter++;
                break;

            case 2:
                try {
                    AITools.move(entity, AITools.calculatePath(current, new Point(current.x + radius, current.y)));
                    breakTimer = 0;
                } catch (Exception e) {
                    // TODO: handle exception
                }
                counter++;
                break;

            case 3:
                try {
                    AITools.move(entity, AITools.calculatePath(current, new Point(current.x, current.y + radius)));
                    breakTimer = 0;
                } catch (Exception e) {
                    // TODO: handle exception
                }
                counter = 0;
                break;
        
            default:
                break;
        }
    }

}
