package ecs.components.xp;

import ecs.components.Component;
import ecs.entities.Entity;

public class XPComponent extends Component {

    private static final long FORMULA_A = 100;
    private static final long FORMULA_B = 10;
    private long currentLevel;
    private long currentXP;
    private long lootXP;
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
     * Get the amount of xp that will be dropped when the entity dies
     *
     * @return xp that will be dropped
     */
    public long getLootXP() {
        return lootXP;
    }

    /**
     * Set the amount of xp that will be dropped when the entity dies
     *
     * @param lootXP xp that will be dropped
     */
    public void setLootXP(long lootXP) {
        this.lootXP = lootXP;
    }

    /**
     * Calculate xp left to next level. XP are calculated based on the following formula:
     * sqrt((currentLevel + 1) * FORMULA_A) * FORMULA_B
     *
     * @return xp left to next level
     */
    public long getXPToNextLevel() {
        return Math.round(Math.sqrt((currentLevel + 1) * FORMULA_A) * FORMULA_B) - currentXP;
    }
}
