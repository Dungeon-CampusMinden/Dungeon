package ecs.components.ai.idle;

import static ecs.components.ai.AITools.getRandomAccessibleTileCoordinateInRange;

import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.utils.JsonValue;
import ecs.components.PositionComponent;
import ecs.components.ai.AITools;
import ecs.entities.Entity;
import level.elements.tile.Tile;
import savegame.GameSerialization;
import savegame.Reflections;
import tools.Constants;
import tools.Point;

public class StaticRadiusWalk implements IIdleAI {
    private final float radius;
    private GraphPath<Tile> path;
    private final int breakTime;
    private int currentBreak = 0;
    private Point center;
    private Point currentPosition;
    private Point newEndTile;

    /**
     * Finds a point in the radius and then moves there. When the point has been reached, a new
     * point in the radius is searched for from the center.
     *
     * @param radius Radius in which a target point is to be searched for
     * @param breakTimeInSeconds how long to wait (in seconds) before searching a new goal
     */
    public StaticRadiusWalk(float radius, int breakTimeInSeconds) {
        this.radius = radius;
        this.breakTime = breakTimeInSeconds * Constants.FRAME_RATE;
    }

    @Override
    public void idle(Entity entity) {
        if (path == null || AITools.pathFinishedOrLeft(entity, path)) {
            if (center == null) {
                PositionComponent pc =
                        (PositionComponent)
                                entity.getComponent(PositionComponent.class).orElseThrow();
                center = pc.getPosition();
            }

            if (currentBreak >= breakTime) {
                currentBreak = 0;
                PositionComponent pc2 =
                        (PositionComponent)
                                entity.getComponent(PositionComponent.class).orElseThrow();
                currentPosition = pc2.getPosition();
                newEndTile = getRandomAccessibleTileCoordinateInRange(center, radius).toPoint();
                path = AITools.calculatePath(currentPosition, newEndTile);
                idle(entity);
            }
            currentBreak++;

        } else AITools.move(entity, path);
    }

    @Override
    public JsonValue serialize() {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        json.addChild("radius", new JsonValue(radius));
        json.addChild("breakTime", new JsonValue(breakTime));
        json.addChild("currentBreak", new JsonValue(currentBreak));
        json.addChild("center", GameSerialization.serializePoint(center));
        json.addChild("currentPosition", GameSerialization.serializePoint(currentPosition));
        json.addChild("newEndTile", GameSerialization.serializePoint(newEndTile));
        json.addChild("path", GameSerialization.serializeGraphPath(path));
        return json;
    }

    @Override
    public void deserialize(JsonValue data) {
        Reflections.setFieldValue(this, "radius", data.getFloat("radius"));
        Reflections.setFieldValue(this, "breakTime", data.getInt("breakTime"));
        currentBreak = data.getInt("currentBreak");
        center = GameSerialization.deserializePoint(data.get("center"));
        currentPosition = GameSerialization.deserializePoint(data.get("currentPosition"));
        newEndTile = GameSerialization.deserializePoint(data.get("newEndTile"));
        path = GameSerialization.deserializeGraphPath(data.get("path"));
    }
}
