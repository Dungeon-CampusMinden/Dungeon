package contrib.systems;

import contrib.components.XPComponent;

import core.Entity;
import core.System;
import core.utils.components.MissingComponentException;

public final class XPSystem extends System {

    public XPSystem() {
        super(XPComponent.class);
    }

    @Override
    public void execute() {
        entityStream().forEach(this::checkForLevelUP);
    }

    private void checkForLevelUP(Entity entity) {
        XPComponent comp =
                entity.fetch(XPComponent.class)
                        .orElseThrow(() -> MissingComponentException.build(entity, XPSystem.class));
        long xpLeft;
        while ((xpLeft = comp.xpToNextLevel()) <= 0) {
            this.performLevelUp(comp, (int) xpLeft);
        }
    }

    /**
     * Perform a level up by increasing the current level and resetting the current XP. If the
     * current XP is greater than the needed amount for the level up the remaining xp are added to
     * the current XP.
     *
     * @param comp XPComponent of entity
     * @param xpLeft XP left to level up (can be negative if greater the needed amount)
     */
    private void performLevelUp(XPComponent comp, int xpLeft) {
        comp.currentLevel(comp.currentLevel() + 1);
        comp.currentXP(xpLeft * -1);
        comp.levelUp(comp.currentLevel());
    }
}
