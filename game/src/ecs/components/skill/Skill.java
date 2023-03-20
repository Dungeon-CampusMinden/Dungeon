package ecs.components.skill;

import ecs.entities.Entity;
import java.io.Serializable;
import tools.Constants;

public class Skill implements Serializable {

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

    /*@Override
    public JsonValue serialize() {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        json.addChild("coolDownInFrames", new JsonValue(coolDownInFrames));
        json.addChild("currentCoolDownInFrames", new JsonValue(currentCoolDownInFrames));
        json.addChild("skillFunction", GameSerialization.serialize(skillFunction));
        return json;
    }

    @Override
    public void deserialize(JsonValue data) {
        coolDownInFrames = data.getInt("coolDownInFrames");
        currentCoolDownInFrames = data.getInt("currentCoolDownInFrames");
        skillFunction = GameSerialization.deserialize(data.get("skillFunction"));
    }*/
}
