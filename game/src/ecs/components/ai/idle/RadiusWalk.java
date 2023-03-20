package ecs.components.ai.idle;

import com.badlogic.gdx.ai.pfa.GraphPath;
import ecs.components.ai.AITools;
import ecs.entities.Entity;
import level.elements.tile.Tile;
import savegame.IFieldSerializing;
import tools.Constants;

public class RadiusWalk implements IIdleAI, IFieldSerializing {
    private final float radius;
    private GraphPath<Tile> path;
    private final int breakTime;
    private int currentBreak = 0;

    /**
     * Finds a point in the radius and then moves there. When the point has been reached, a new
     * point in the radius is searched for from there.
     *
     * @param radius Radius in which a target point is to be searched for
     * @param breakTimeInSeconds how long to wait (in seconds) before searching a new goal
     */
    public RadiusWalk(float radius, int breakTimeInSeconds) {
        this.radius = radius;
        this.breakTime = breakTimeInSeconds * Constants.FRAME_RATE;
    }

    @Override
    public void idle(Entity entity) {
        if (path == null || AITools.pathFinishedOrLeft(entity, path)) {
            if (currentBreak >= breakTime) {
                currentBreak = 0;
                path = AITools.calculatePathToRandomTileInRange(entity, radius);
                idle(entity);
            }

            currentBreak++;

        } else AITools.move(entity, path);
    }

    /*@Override
    public JsonValue serialize() {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        json.addChild("radius", new JsonValue(radius));
        json.addChild("breakTime", new JsonValue(breakTime));
        json.addChild("currentBreak", new JsonValue(currentBreak));
        json.addChild("path", GameSerialization.serializeGraphPath(path));
        return json;
    }

    @Override
    public void deserialize(JsonValue data) {
        Reflections.setFieldValue(this, "radius", data.getFloat("radius"));
        Reflections.setFieldValue(this, "breakTime", data.getInt("breakTime"));
        currentBreak = data.getInt("currentBreak");
        path = GameSerialization.deserializeGraphPath(data.get("path"));
    }*/
}
