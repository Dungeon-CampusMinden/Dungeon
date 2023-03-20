package ecs.components.ai.fight;

import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.utils.JsonValue;
import ecs.components.ai.AITools;
import ecs.components.skill.Skill;
import ecs.entities.Entity;
import level.elements.tile.Tile;
import savegame.GameSerialization;
import savegame.Reflections;
import starter.Game;
import tools.Constants;

public class MeleeAI implements IFightAI {
    private final float attackRange;
    private final int delay = Constants.FRAME_RATE;
    private int timeSinceLastUpdate = 0;
    private final Skill fightSkill;
    private GraphPath<Tile> path;

    /**
     * Attacks the player if he is within the given range. Otherwise, it will move towards the
     * player.
     *
     * @param attackRange Range in which the attack skill should be executed
     * @param fightSkill Skill to be used when an attack is performed
     */
    public MeleeAI(float attackRange, Skill fightSkill) {
        this.attackRange = attackRange;
        this.fightSkill = fightSkill;
    }

    @Override
    public void fight(Entity entity) {
        if (AITools.playerInRange(entity, attackRange)) {
            fightSkill.execute(entity);
        } else {
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
        json.addChild("attackRange", new JsonValue(attackRange));
        json.addChild("fightSkill", GameSerialization.serialize(fightSkill));
        json.addChild("timeSinceLastUpdate", new JsonValue(timeSinceLastUpdate));
        json.addChild("path", GameSerialization.serialize(path));
        return json;
    }

    @Override
    public void deserialize(JsonValue data) {
        Reflections.setFieldValue(
                this, "fightSkill", GameSerialization.deserialize(data.get("fightSkill")));
        Reflections.setFieldValue(this, "attackRange", data.getFloat("attackRange"));
        timeSinceLastUpdate = data.getInt("timeSinceLastUpdate");
        path = GameSerialization.deserialize(data.get("path"));
    }
}
