package ecs.components.skill;

import ecs.components.HealthComponent;
import ecs.entities.Entity;

/** The HealSkill heals the hero by 15% on use */
public class HealSkill extends MagicSkill {

    @Override
    public void execute(Entity entity) {
        if (entity.getComponent(HealthComponent.class).isPresent()) {
            HealthComponent hCp =
                    (HealthComponent) entity.getComponent(HealthComponent.class).get();

            int current = hCp.getCurrentHealthpoints();
            float healedByPercentage = 0.15f;
            int healedBy = ((int) (current * healedByPercentage));
            int newHP = ((int) (current + healedBy));
            System.out.println(
                    "Entity had: " + current + "HP and got healed by: " + healedBy + ".");
            hCp.setCurrentHealthpoints(newHP);
            System.out.println("Current HP is: " + newHP);
        }
    }
}
