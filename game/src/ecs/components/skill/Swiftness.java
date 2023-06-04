package ecs.components.skill;

import ecs.components.ManaComponent;
import ecs.components.MissingComponentException;
import ecs.components.VelocityComponent;
import ecs.entities.Entity;
import tools.Constants;

public class Swiftness implements IDurationSkillFunction {

    private float durationInFrames, currentDurationInFrames = 0.0f, xSpeedMultiplier = 0.01f, ySpeedMultiplier = 0.01f,
            originalXSpeed,
            originalYSpeed;
    private int manaCost;
    private VelocityComponent velocity;

    /**
     * Creates a new Wisdom skill
     * <p/>
     * The Wisdom skill increases the XP wielder earns
     * 
     * @param durationIneconds the duration in seconds
     * @param xSpeedMultiplier the speed multiplier on the x axis
     * @param ySpeedMultiplier the speed multiplier on the y axis
     * @param entity           the entity that owns this skill
     * @param manaCost         the mana cost
     */
    public Swiftness(float durationIneconds, float xSpeedMultiplier, float ySpeedMultiplier, Entity entity,
            int manaCost) {
        if (!entity.getComponent(VelocityComponent.class).isPresent())
            throw new MissingComponentException("VelocityComponent");
        durationInFrames = durationIneconds * Constants.FRAME_RATE;
        this.xSpeedMultiplier = xSpeedMultiplier > 1.0f ? xSpeedMultiplier : 1.0f;
        this.ySpeedMultiplier = ySpeedMultiplier > 1.0f ? ySpeedMultiplier : 1.0f;
        velocity = (VelocityComponent) entity.getComponent(VelocityComponent.class).get();
        originalXSpeed = velocity.getXVelocity();
        originalYSpeed = velocity.getYVelocity();
        this.manaCost = manaCost;
    }

    /**
     * Creates a new Wisdom skill
     * <p/>
     * The Wisdom skill increases the XP wielder earns
     * <p/>
     * The standard duration is {@code 30} seconds
     * <p/>
     * The standard speed multiplier is {@code 2.0f}
     * <p/>
     * The standard manaCost is {@code 20}
     * 
     * @param entity the entity that owns this skill
     */
    public Swiftness(Entity entity) {
        this(30, 2.0f, 2.0f, entity, 20);
    }

    @Override
    public void execute(Entity entity) {
        if (!entity.getComponent(ManaComponent.class).isPresent())
            throw new MissingComponentException("ManaComponent");
        if (entity.getComponent(ManaComponent.class).map(ManaComponent.class::cast).get().spendMana(manaCost)) {
            velocity.setXVelocity(originalXSpeed * xSpeedMultiplier);
            velocity.setYVelocity(originalYSpeed * ySpeedMultiplier);
        }
    }

    /**
     * Decreases the {@code currentDurationInFrames} and resets multiplier if the
     * time is over
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public void reduceDuration() {
        if (--currentDurationInFrames <= 0) {
            velocity.setXVelocity(originalXSpeed);
            velocity.setYVelocity(originalYSpeed);
        }
    }

    @Override
    public boolean isActive() {
        return currentDurationInFrames > 0;
    }

    @Override
    public void activateDuration() {
        currentDurationInFrames = durationInFrames;
    }

}
