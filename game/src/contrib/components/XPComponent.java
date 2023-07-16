package contrib.components;

import core.Component;
import core.Entity;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Allow the associated entity to collect experience points (XP) and level up if enough XP is
 * collected
 *
 * <p>The component stores the current character level, the amount of XP this entity has collected,
 * and the amount of XP this entity will drop if it dies. To drop XP, a {@link HealthComponent} is
 * needed. The collected XP will not be set to zero after a level up was performed.
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
 * <p>The XP needed for the next level is calculated using a formula. You can set your own formula
 * with {@link #levelUPFormula(Function)}. The default formula uses a quadratic function, resulting
 * in each level requiring more XP than the previous one.
 */
public final class XPComponent extends Component {

    private static final double NEEDED_XP_FOR_LEVEL_ONE = 100;
    private static final double FORMULA_SLOPE = 0.5;
    /**
     * XP is calculated based on the following formula: {@code FORMULA_SLOPE * (currentLevel + 1)^2
     * + LEVEL_1_XP}.
     *
     * <p>It's a quadratic function with a slope of {@code FORMULA_SLOPE} and a y-intercept of
     * {@code LEVEL_1_XP}.
     */
    private static final Function<Long, Long> DEFAULT_LEVEL_UP_FORMULA =
        level -> Math.round(FORMULA_SLOPE * Math.pow(level, 2) + NEEDED_XP_FOR_LEVEL_ONE);

    private static final Consumer<Entity> DEFAULT_LEVEL_UP = entity1 -> {};

    private static final Function<XPComponent, Long> DEFAULT_LOOT_XP_FUNCTION =
        xpComponent -> (long)(xpComponent.currentXP() * 0.5f);

    private Function<XPComponent, Long> lootXPFunction;
    private Function<Long, Long> levelUPFormula;
    private Consumer<Entity> callbackLevelUp;
    private long characterLevel;
    private long currentXP;

    /**
     * Create a new XPComponent and add it to the associated entity.
     *
     * <p>Useful for entities that should collect XP to level up, such as the player character.
     *
     * <p>The {@link #lootXP()} will always be half of {@link #currentXP}, dynamically adjusting as
     * this component collects more XP.
     *
     * @param entity the associated entity
     * @param levelUp the callback for when the entity levels up
     */
    public XPComponent(final Entity entity, final Consumer<Entity> levelUp) {
        super(entity);
        callbackLevelUp = levelUp;
        levelUPFormula = DEFAULT_LEVEL_UP_FORMULA;
        lootXPFunction = DEFAULT_LOOT_XP_FUNCTION;
    }

    /**
     * Create a new XPComponent with an empty level-up callback and add it to the associated entity.
     *
     * <p>Useful for entities that should only give XP and not gain XP themselves, such as monsters.
     *
     * @param entity the associated entity
     * @param lootXP the amount of XP this entity drops if it dies (requires a {@link
     *     HealthComponent}). If the value is negativ, XP are taken from the entity that is looting.
     */
    public XPComponent(final Entity entity, long lootXP) {
        super(entity);
        callbackLevelUp = DEFAULT_LEVEL_UP;
        levelUPFormula = DEFAULT_LEVEL_UP_FORMULA;
        this.lootXPFunction = xpComponent -> lootXP;
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

    /**
     * Get the level-up callback function.
     *
     * @return the level-up callback function.
     */
    public Consumer<Entity> levelUp() {
        return callbackLevelUp;
    }

    /**
     * Set the leve-up callback function.
     *
     * @param callback the callback for when the entity levels up.
     */
    public void levelUp(Consumer<Entity> callback) {
        callbackLevelUp = callback;
    }

    /**
     * Get the amount of XP that will be dropped when the associated entity dies.
     *
     * <p>If no value is set, the dropped XP will be the current XP multiplied with the loot-factor.
     *
     * <p>If no factor is set, the dropped XP will be set to half of the current XP.
     *
     * <p>XP can only be dropped on death if the associated entity has a {@link HealthComponent}.
     *
     * <p>If the value is negativ, XP are taken from the entity that is looting.
     *
     * @return the XP that will be dropped
     */
    public long lootXP() {
        return lootXPFunction.apply(this);
    }

    /**
     * Set the function to calculate the amount of XP that will be dropped when the associated
     * entity dies.
     *
     * @param lootXPFunction Function that gets the {@link XPComponent} of the associated entity and
     *     returns the amount of XP to drop as {@link Long}. If the return value is negative, XP is
     *     taken from the entity that is looting.
     */
    public void lootXP(Function<XPComponent, Long> lootXPFunction) {
        this.lootXPFunction = lootXPFunction;
    }

    /**
     * Set the function to calculate the amount of XP that is missing to reach the next character
     * level.
     *
     * @param formula The new formula used to calculate the missing XP. (Function<Long, Long> where
     *     the input is the character level and the output is the XP needed for the next level)
     */
    public void levelUPFormula(Function<Long, Long> formula) {
        this.levelUPFormula = formula;
    }

    /**
     * Calculate the amount of XP needed to reach the next character level.
     *
     * @return the amount of XP left to reach the next character level
     */
    public long xpToNextCharacterLevel() {
        return levelUPFormula.apply(characterLevel) - currentXP;
    }
}
