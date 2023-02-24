package ecs.components.skill;

import ecs.components.Component;
import ecs.entities.Entity;
import java.util.HashSet;
import java.util.Set;

public class SkillComponent extends Component {

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
}
