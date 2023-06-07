package ecs.components.skill;

import ecs.components.ManaComponent;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import ecs.entities.Entity;
import tools.Point;

/**
 * Teleportation Skill
 */
public class Blink implements ISkillFunction {

    private ITargetSelection targetSelection;
    private float range;
    private int manaCost;

    /**
     * Creates a new Blink skill with the given parameters
     * 
     * @param targetSelection the target selection
     * @param range           the range
     * @param manaCost        the mana cost
     */
    public Blink(ITargetSelection targetSelection, float range, int manaCost) {
        this.targetSelection = targetSelection;
        this.range = range;
        this.manaCost = manaCost;
    }

    @Override
    public void execute(Entity entity) {
        if (!entity.getComponent(ManaComponent.class).isPresent())
            throw new MissingComponentException("ManaComponent");
        if (entity.getComponent(ManaComponent.class).map(ManaComponent.class::cast).get().spendMana(manaCost)) {
            VelocityComponent velocity = (VelocityComponent) entity.getComponent(VelocityComponent.class).get();
            PositionComponent position = (PositionComponent) entity.getComponent(PositionComponent.class).get();
            Point lastPoint = SkillTools.calculateLastPositionInRange(position.getPosition(),
                    targetSelection.selectTargetPoint(), range);
            float xDistance = targetSelection.selectTargetPoint().x - position.getPosition().x;
            xDistance = Math.abs(xDistance) > range ? lastPoint.x - position.getPosition().x : xDistance;
            float yDistance = targetSelection.selectTargetPoint().y - position.getPosition().y;
            yDistance = Math.abs(yDistance) > range ? lastPoint.y - position.getPosition().y : yDistance;
            velocity.setCurrentXVelocity(xDistance);
            velocity.setCurrentYVelocity(yDistance);
        }
    }

}
