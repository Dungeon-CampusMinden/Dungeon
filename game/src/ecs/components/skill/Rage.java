package ecs.components.skill;

import ecs.components.MissingComponentException;
import ecs.components.stats.StatsComponent;
import ecs.damage.DamageType;
import ecs.entities.Entity;
import tools.Constants;

public class Rage implements IDurationSkillFunction {

    private float durationInFrames, currentDurationInFrames = 0.0f, damageMultiplier = 1.0f, originalDamageMultiplier;
    private StatsComponent stats;

    /**
     * Creates a new Rage skill
     * <p/>
     * The Rage skill increases the Physical damage the wielder deals
     * 
     * @param durationIneconds time how long the skill lasts in seconds
     * @param damageMultiplier multiplier to increase the physical damage
     * @param entity           the entity that owns this Skill
     * 
     * @throws MissngComponentException if the specified entity has no
     *                                  StatsComponent
     */
    public Rage(float durationIneconds, float damageMultiplier, Entity entity) {
        if (!entity.getComponent(StatsComponent.class).isPresent())
            throw new MissingComponentException("StatsComponent");
        durationInFrames = durationIneconds * Constants.FRAME_RATE;
        this.damageMultiplier = damageMultiplier > 1.0f ? damageMultiplier : 1.0f;
        stats = (StatsComponent) entity.getComponent(StatsComponent.class).get();
        originalDamageMultiplier = stats.getDamageModifiers().getMultiplier(DamageType.PHYSICAL);
    }

    @Override
    public void execute(Entity entity) {
        stats.getDamageModifiers().setMultiplier(DamageType.PHYSICAL, originalDamageMultiplier * damageMultiplier);
    }

    /**
     * Decreases the {@code currentDurationInFrames} and resets multiplier if the
     * time is over
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public void reduceDuration() {
        if (--currentDurationInFrames <= 0)
            stats.getDamageModifiers().setMultiplier(DamageType.PHYSICAL, originalDamageMultiplier);
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
