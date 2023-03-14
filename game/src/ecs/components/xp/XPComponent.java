package ecs.components.xp;

import ecs.components.Component;
import ecs.entities.Entity;

public class XPComponent extends Component {

    private static final double LEVEL_1_XP = 100;
    private static final double FORMULA_SLOPE = 0.5;
    private long currentLevel;
    private long currentXP;
    private long lootXP = -1;
    private ILevelUp callbackLevelUp;

    /**
     * Create a new XP-Component and add it to the associated entity
     *
     * @param entity associated entity
     */
    public XPComponent(Entity entity) {
        super(entity);
    }

    /**
     * Create a new XP-Component and add it to the associated entity
     *
     * @param entity associated entity
     * @param levelUp callback for when the entity levels up
     */
    public XPComponent(Entity entity, ILevelUp levelUp, int lootXP) {
        super(entity);
        this.callbackLevelUp = levelUp;
        this.lootXP = lootXP;
    }

    public XPComponent(Entity entity, ILevelUp levelUp) {
        super(entity);
        this.callbackLevelUp = levelUp;
    }

    /**
     * Get the current level of the entity
     *
     * @return current level
     */
    public long getCurrentLevel() {
        return currentLevel;
    }

    /**
     * Set the current level of the entity
     *
     * @param currentLevel current level
     */
    public void setCurrentLevel(long currentLevel) {
        this.currentLevel = currentLevel;
    }

    /**
     * Add xp to the entity. Adding negative xp will decrease the current xp. The minimum xp of an
     * entity is 0.
     *
     * @param xp xp to add
     */
    public void addXP(long xp) {
        this.currentXP = Math.max(0, currentXP + xp);
    }

    /**
     * Get the current xp of the entity
     *
     * @return current xp
     */
    public long getCurrentXP() {
        return currentXP;
    }

    /**
     * Set the current xp of the entity
     *
     * @param currentXP current xp
     */
    public void setCurrentXP(long currentXP) {
        this.currentXP = currentXP;
    }

    /**
     * Trigger the level up callback
     *
     * @param level new level
     */
    public void levelUp(long level) {
        if (this.callbackLevelUp != null) this.callbackLevelUp.onLevelUp(level);
    }

    /**
     * Get the amount of xp that will be dropped when the entity dies. If no value is set, the xp
     * will be set to half of the current xp
     *
     * @return xp that will be dropped
     */
    public long getLootXP() {
        return lootXP == -1 ? currentXP / 2 : lootXP;
    }

    /**
     * Set the amount of xp that will be dropped when the entity dies. Set to -1 to use the default.
     *
     * @param lootXP xp that will be dropped
     */
    public void setLootXP(long lootXP) {
        this.lootXP = lootXP;
    }

    /**
     * Calculate xp left to next level. XP are calculated based on the following formula: {@code
     * FORMULA_SLOPE * (currentLevel + 1)^2 + LEVEL_1_XP} It's a quadratic function with a slope of
     * {@code FORMULAR_SLOPE} and a y-intercept of {@code LEVEL_1_XP}
     *
     * @return xp left to next level
     */
    public long getXPToNextLevel() {
        // level 0 in Formula is level 1 in game.
        return Math.round(FORMULA_SLOPE * Math.pow(currentLevel, 2) + LEVEL_1_XP) - currentXP;
    }
}
