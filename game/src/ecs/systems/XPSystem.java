package ecs.systems;

import ecs.components.xp.XPComponent;
import mydungeon.ECS;

public class XPSystem extends ECS_System {

    @Override
    public void update() {
        ECS.entities.stream()
                .flatMap(e -> e.getComponent(XPComponent.class).stream())
                .forEach(
                        component -> {
                            XPComponent comp = (XPComponent) component;
                            long xpLeft;
                            while ((xpLeft = comp.getXPToNextLevel()) <= 0) {
                                comp.setCurrentLevel(comp.getCurrentLevel() + 1);
                                comp.setCurrentXP(xpLeft * -1);
                                comp.levelUp(comp.getCurrentLevel());
                            }
                        });
    }
}
