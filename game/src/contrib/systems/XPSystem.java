package contrib.systems;

import contrib.components.XPComponent;

import core.Entity;
import core.System;

public class XPSystem extends System {

    @Override
    protected boolean accept(Entity entity) {
        if (entity.getComponent(XPComponent.class).isPresent()) return true;
        return false;
    }

    @Override
    public void systemUpdate() {
        getEntityStream()
                .forEach(
                        entity -> {
                            XPComponent comp =
                                    (XPComponent) entity.getComponent(XPComponent.class).get();
                            long xpLeft;
                            while ((xpLeft = comp.getXPToNextLevel()) <= 0) {
                                this.performLevelUp(comp, (int) xpLeft);
                            }
                        });
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
        comp.setCurrentLevel(comp.getCurrentLevel() + 1);
        comp.setCurrentXP(xpLeft * -1);
        comp.levelUp(comp.getCurrentLevel());
    }
}
