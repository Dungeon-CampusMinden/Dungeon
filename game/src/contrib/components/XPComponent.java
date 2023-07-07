package contrib.components;

import core.Component;
import core.Entity;

import java.util.function.Consumer;

/**
 * Allow associated entity to collect Experience Points (XP) and level up if enough XP is collected.
 *
 * <p>The component stores the current character level, the amount of XP this entity has collected,
 * and the amount of XP this entity will drop if it dies. To drop XP, a {@link HealthComponent} is
 * needed.
 *
 * <p>The callback {@link #callbackLevelUp} can be used to trigger specific behavior when the
 * associated entity reaches a new character level.
 *
 * <p>Use {@link #addXP(long)} to gain XP for the component. The {@link
 * contrib.systems.HealthSystem} will use this to increase the amount of XP in this component if the
 * associated entity has killed another entity with an {@link XPComponent} by the amount of {@link
 * #lootXP()} from the killed entity.
 *
 * <p>The {@link contrib.systems.XPSystem} will check if the component has collected enough XP to
 * reach the next level using {@link #xpToNextCharacterLevel()} and will then update the character
 * level and trigger the {@link #levelUp()} callback function.
 *
 * <p>The amount of XP needed for a level up is calculated based on the following formula: {@code
 * FORMULA_SLOPE * (currentLevel + 1)^2 + LEVEL_1_XP}. It's a quadratic function with a slope of
 * {@code FORMULA_SLOPE} and a y-intercept of {@code LEVEL_1_XP}.
 */
public final class XPComponent extends Component {
    private static final double NEEDED_XP_FOR_LEVEL_ONE = 100;
    private static final double FORMULA_SLOPE = 0.5;
    private static final Consumer<Entity> DEFAULT_LEVEL_UP = entity1 -> {};
    private final Consumer<Entity> callbackLevelUp;
    private long characterLevel;
    private long currentXP;
    private final long lootXP;

    /**
     * Create a new XPComponent and add it to the associated entity.
     *
     * @param entity the associated entity
     * @param levelUp the callback for when the entity levels up
     * @param lootXP the amount of XP this entity drops if it dies (requires a {@link
     *     HealthComponent})
     */
    public XPComponent(final Entity entity, final Consumer<Entity> levelUp, int lootXP) {
        super(entity);
        this.callbackLevelUp = levelUp;
        this.lootXP = lootXP;
    }

    /**
     * Create a new XPComponent and add it to the associated entity.
     *
     * <p>Useful for entities that should collect XP to level up, such as the player character.
     *
     * <p>The {@link #lootXP} will always be half of {@link #currentXP}, dynamically adjusting as
     * this component collects more XP.
     *
     * @param entity the associated entity
     * @param levelUp the callback for when the entity levels up
     */
    public XPComponent(final Entity entity, final Consumer<Entity> levelUp) {
        this(entity, levelUp, -1);
    }

    /**
     * Create a new XPComponent with an empty level-up callback and add it to the associated entity.
     *
     * <p>Useful for entities that should only give XP and not gain XP themselves, such as monsters.
     *
     * @param entity the associated entity
     * @param lootXP the amount of XP this entity drops if it dies (requires a {@link
     *     HealthComponent})
     */
    public XPComponent(final Entity entity, int lootXP) {
        this(entity, DEFAULT_LEVEL_UP, lootXP);
    }

    /**
     * Create a new XPComponent with an empty level-up callback and add it to the associated entity.
     *
     * <p>The {@link #lootXP} will always be half of {@link #currentXP}, dynamically adjusting as
     * this entity collects more XP.
     *
     * @param entity the associated entity
     */
    public XPComponent(final Entity entity) {
        this(entity, DEFAULT_LEVEL_UP);
    }

    /**
     * Get the current character level of the associated entity.
     *
     * @return the current character level
     */
    public long characterLevel() {
        return characterLevel;
    }

    /**
     * Set the current character level of the associated entity.
     *
     * @param currentLevel the character level to set
     */
    public void characterLevel(long currentLevel) {
        this.characterLevel = currentLevel;
    }

    /**
     * Add XP to the associated entity.
     *
     * <p>Adding negative XP will decrease the current XP. The minimum XP of an entity is 0.
     *
     * <p>This method will only update the XP value and will not check if a new character level is
     * reached.
     *
     * @param xp the amount of XP to add to this entity
     */
    public void addXP(long xp) {
        this.currentXP = Math.max(0, currentXP + xp);
    }

    /**
     * Get the current XP of the associated entity.
     *
     * @return the current XP of the associated entity
     */
    public long currentXP() {
        return currentXP;
    }

    /**
     * Set the current XP of the associated entity.
     *
     * <p>This method will only update the XP value and will not check if a new character level is
     * reached.
     *
     * @param currentXP the value to set the current XP to
     */
    public void currentXP(long currentXP) {
        this.currentXP = currentXP;
    }

    /** Trigger the level-up callback function. */
    public void levelUp() {
        callbackLevelUp.accept(entity);
    }

    /**
     * Get the amount of XP that will be dropped when the associated entity dies.
     *
     * <p>If no value is set, the dropped XP will be set to half of the current XP.
     *
     * <p>XP can only be dropped on death if the associated entity has a {@link HealthComponent}.
     *
     * @return the XP that will be dropped
     */
    public long lootXP() {
        return lootXP == -1 ? currentXP / 2 : lootXP;
    }

    /**
     * Calculate the amount of XP needed to reach the next character level.
     *
     * <p>XP is calculated based on the following formula: {@code FORMULA_SLOPE * (currentLevel +
     * 1)^2 + LEVEL_1_XP}. It's a quadratic function with a slope of {@code FORMULA_SLOPE} and a
     * y-intercept of {@code LEVEL_1_XP}.
     *
     * @return the amount of XP left to reach the next character level
     */
    public long xpToNextCharacterLevel() {
        // character level 0 in Formula is character level 1 in game.
        return Math.round(FORMULA_SLOPE * Math.pow(characterLevel, 2) + NEEDED_XP_FOR_LEVEL_ONE)
                - currentXP;
    }
}
