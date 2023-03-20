package ecs.components.ai.fight;

import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.utils.JsonValue;
import ecs.components.ai.AITools;
import ecs.entities.Entity;
import level.elements.tile.Tile;
import savegame.GameSerialization;
import savegame.Reflections;
import starter.Game;
import tools.Constants;

public class CollideAI implements IFightAI {
    private final int delay = Constants.FRAME_RATE;
    private float rushRange;
    private int timeSinceLastUpdate = delay;
    private GraphPath<Tile> path;

    /**
     * Attacks the player by colliding if he is within the given range. Otherwise, it will move
     * towards the player.
     *
     * @param rushRange Range in which the faster collide logic should be executed
     */
    public CollideAI(float rushRange) {
        this.rushRange = rushRange;
    }

    @Override
    public void fight(Entity entity) {
        if (AITools.playerInRange(entity, rushRange)) {
            // the faster pathing once a certain range is reached
            path = AITools.calculatePathToHero(entity);
            AITools.move(entity, path);
            timeSinceLastUpdate = delay;
        } else {
            // check if new pathing update
            if (timeSinceLastUpdate >= delay) {
                path = AITools.calculatePathToHero(entity);
                timeSinceLastUpdate = -1;
            }
            timeSinceLastUpdate++;
            AITools.move(entity, path);
        }
    }

    @Override
    public JsonValue serialize() {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        json.addChild("rushRange", new JsonValue(rushRange));
        json.addChild("timeSinceLastUpdate", new JsonValue(timeSinceLastUpdate));
        json.addChild("path", GameSerialization.serializeGraphPath(path));
        json.addChild("delay", new JsonValue(delay));
        return json;
    }

    @Override
    public void deserialize(JsonValue data) {
        Reflections.setFieldValue(this, "delay", data.getInt("delay"));
        timeSinceLastUpdate = data.getInt("timeSinceLastUpdate");
        rushRange = data.getFloat("rushRange");
        path = GameSerialization.deserializeGraphPath(data.get("path"));
    }
}
