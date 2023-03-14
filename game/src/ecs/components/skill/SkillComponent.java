package ecs.components.skill;

import com.badlogic.gdx.utils.JsonValue;
import ecs.components.Component;
import ecs.entities.Entity;
import java.util.HashSet;
import java.util.Set;
import savegame.GameSerialization;
import savegame.ISerializable;

public class SkillComponent extends Component implements ISerializable {

    public static String name = "SkillComponent";

    private Set<Skill> skillSet;

    /**
     * @param entity associated entity
     */
    public SkillComponent(Entity entity) {
        super(entity);
        skillSet = new HashSet<>();
    }

    /**
     * Add a skill to this component
     *
     * @param skill to add
     */
    public void addSkill(Skill skill) {
        skillSet.add(skill);
    }

    /**
     * remove a skill from this component
     *
     * @param skill to remove
     */
    public void removeSkill(Skill skill) {
        skillSet.remove(skill);
    }

    /**
     * @return Set with all skills of this component
     */
    public Set<Skill> getSkillSet() {
        return skillSet;
    }

    /** reduces the cool down of each skill by 1 frame */
    public void reduceAllCoolDowns() {
        for (Skill skill : skillSet) skill.reduceCoolDown();
    }

    @Override
    public JsonValue serialize() {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        JsonValue skillSetJson = new JsonValue(JsonValue.ValueType.array);
        for (Skill skill : skillSet) {
            skillSetJson.addChild(GameSerialization.serialize(skill));
        }
        json.addChild("skillSet", skillSetJson);
        return json;
    }

    @Override
    public void deserialize(JsonValue data) {
        JsonValue skillSetJson = data.get("skillSet");
        for (JsonValue skillJson : skillSetJson) {
            Skill skill = GameSerialization.deserialize(skillJson);
            skillSet.add(skill);
        }
    }
}
