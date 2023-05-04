package ecs.systems;

import ecs.components.xp.XPComponent;
import starter.Game;

public class XPSystem extends ECS_System {

    @Override
    public void update() {
        Game.getEntities().stream()
                .flatMap(e -> e.getComponent(XPComponent.class).stream())
                .forEach(
                        component -> {
                            XPComponent comp = (XPComponent) component;
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
