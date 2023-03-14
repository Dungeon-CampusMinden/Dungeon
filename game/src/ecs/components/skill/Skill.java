package ecs.components.skill;

import com.badlogic.gdx.utils.JsonValue;
import ecs.entities.Entity;
import graphic.Animation;
import tools.Constants;
import savegame.GameSerialization;
import savegame.ISerializable;

public class Skill implements ISerializable {

    private ISkillFunction skillFunction;
    private int coolDownInFrames;
    private int currentCoolDownInFrames;

    /**
     * @param skillFunction Function of this skill
     */
    public Skill(ISkillFunction skillFunction, float coolDownInSeconds) {
        this.skillFunction = skillFunction;
        this.coolDownInFrames = (int) (coolDownInSeconds * Constants.FRAME_RATE);
        this.currentCoolDownInFrames = 0;
    }

    /**
     * Execute the method of this skill
     *
     * @param entity entity which uses the skill
     */
    public void execute(Entity entity) {
        if (!isOnCoolDown()) {
            skillFunction.execute(entity);
            activateCoolDown();
        }
    }

    /**
     * @return true if cool down is not 0, else false
     */
    public boolean isOnCoolDown() {
        return currentCoolDownInFrames > 0;
    }

    /** activate cool down */
    public void activateCoolDown() {
        currentCoolDownInFrames = coolDownInFrames;
    }

    /** reduces the current cool down by frame */
    public void reduceCoolDown() {
        currentCoolDownInFrames = Math.max(0, --currentCoolDownInFrames);
    }

    @Override
    public JsonValue serialize() {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        json.addChild("active", new JsonValue(active));
        json.addChild("animation", GameSerialization.serializeAnimation(animation));
        json.addChild("skillFunction", GameSerialization.serialize(skillFunction));
        return json;
    }

    @Override
    public void deserialize(JsonValue data) {
        active = data.get("active").asBoolean();
        animation = GameSerialization.deserializeAnimation(data.get("animation"));
        skillFunction = GameSerialization.deserialize(data.get("skillFunction"));
    }
}
