package ecs.components.skill;

import ecs.components.ManaComponent;
import ecs.components.MissingComponentException;
import ecs.components.stats.StatsComponent;
import ecs.components.stats.XPModifier;
import ecs.entities.Entity;
import tools.Constants;

public class Wisdom implements IDurationSkillFunction {

    private float durationInFrames, currentDurationInFrames = 0.0f, xpMultiplier = 1.0f, originalXPMultiplier;
    private int manaCost;
    private StatsComponent stats;

    /**
     * Creates a new Wisdom skill
     * <p/>
     * The Wisdom skill increases the XP wielder earns
     * 
     * @param durationIneconds the duration in seconds
     * @param xpMultiplier     the xp multiplier
     * @param entity           the entity that owns this skill
     * @param manaCost         the mana cost
     */
    public Wisdom(float durationIneconds, float xpMultiplier, Entity entity, int manaCost) {
        if (!entity.getComponent(StatsComponent.class).isPresent())
            throw new MissingComponentException("StatsComponent");
        durationInFrames = durationIneconds * Constants.FRAME_RATE;
        this.xpMultiplier = xpMultiplier > 1.0f ? xpMultiplier : 1.0f;
        stats = (StatsComponent) entity.getComponent(StatsComponent.class).get();
        originalXPMultiplier = stats.getXpModifier().xpMultiplier();
        this.manaCost = manaCost;
    }

    /**
     * Creates a new Wisdom skill
     * <p/>
     * The Wisdom skill increases the XP wielder earns
     * <p/>
     * The standard {@code manaCost} is {@code 100}
     * 
     * @param durationIneconds the duration in seconds
     * @param xpMultiplier     the xp multiplier
     * @param entity           the entity that owns this skill
     */
    public Wisdom(float durationIneconds, float xpMultiplier, Entity entity) {
        this(durationIneconds, xpMultiplier, entity, 100);
    }

    @Override
    public void execute(Entity entity) {
        if (!entity.getComponent(ManaComponent.class).isPresent())
            throw new MissingComponentException("ManaComponent");
        if (entity.getComponent(ManaComponent.class).map(ManaComponent.class::cast).get().spendMana(manaCost))
            stats.setXpModifier(new XPModifier(originalXPMultiplier * xpMultiplier));
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
            stats.setXpModifier(new XPModifier(originalXPMultiplier));
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
