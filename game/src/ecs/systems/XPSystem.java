package ecs.systems;

import ecs.components.HealthComponent;
import ecs.components.xp.XPComponent;
import starter.Game;

public class XPSystem extends ECS_System {

    @Override
    public void update() {
        Game.getEntities().stream()
                // Considers only Entities that have a HealthComponent
                .filter(e -> e.getComponent(HealthComponent.class).isPresent())
                // convert to HealthComponent
                .flatMap(e -> e.getComponent(HealthComponent.class).map(HealthComponent.class::cast).stream())
                // Considers only health components that have zero or less health points
                .filter(hc -> hc.getCurrentHealthpoints() <= 0)
                // If the last damage cause is not null
                .forEach(hc -> hc.getLastDamageCause()
                        // If it has an XPComponent
                        .ifPresent(ce -> ce.getComponent(XPComponent.class)
                                // Add XP to the component
                                .ifPresent(xpc -> ((XPComponent) xpc)
                                        // If the original entity had an XPComponent
                                        .addXP(hc.getEntity().getComponent(XPComponent.class).isPresent()
                                                // Add loot XP
                                                ? hc.getEntity().getComponent(XPComponent.class)
                                                        .map(XPComponent.class::cast).get().getLootXP()
                                                // Add 10 XP
                                                : 10))));

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
     * Perform a level up by increasing the current level and resetting the current
     * XP. If the current XP is greater than the needed amount for the level up the
     * remaining xp are added to the current XP.
     *
     * @param comp   XPComponent of entity
     * @param xpLeft XP left to level up (can be negative if greater the needed
     *               amount)
     */
    private void performLevelUp(XPComponent comp, int xpLeft) {
        comp.setCurrentLevel(comp.getCurrentLevel() + 1);
        comp.setCurrentXP(xpLeft * -1);
        comp.levelUp(comp.getCurrentLevel());
    }
}
