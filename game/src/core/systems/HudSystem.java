package core.systems;

import com.badlogic.gdx.scenes.scene2d.Group;

import core.Game;
import core.System;
import core.components.UIComponent;

public class HudSystem extends System {

    public HudSystem() {
        super(UIComponent.class);
    }

    /*
        @Override
        private void addEntity(Entity entity) {
            stage.addActor(entity.getComponent(UIComponent.class).map(UIComponent.class::cast).map(UIComponent::getDialog).get());
        }
    */

    /*
        @Override
        public void removeEntity(Entity entity) {
            entity.getComponent(UIComponent.class).map(UIComponent.class::cast).map(UIComponent::getDialog).get().remove();
        }
    */

    @Override
    public void execute() {
        // temporärer Fix damit die stage aktualisiert wird
        getEntityStream()
                .forEach(
                        x -> {
                            Group d =
                                    x.getComponent(UIComponent.class)
                                            .map(UIComponent.class::cast)
                                            .get()
                                            .getDialog();
                            Game.stage()
                                    .ifPresent(
                                            stage -> {
                                                if (!stage.getActors().contains(d, true)) {
                                                    stage.addActor(d);
                                                }
                                            });
                        }); /*stage.getActors()
                                            .select(
                                                    x ->
                                                            getEntityStream()
                                                                    .anyMatch(
                                                                            y ->
                                                                                    y
                                                                                            .getComponent(UIComponent.class)
                                                                                            .map(UIComponent.class::cast)
                                                                                            .stream()
                                                                                            .noneMatch(
                                                                                                    z -> z.getDialog() == x)))
                                            .forEach(Actor::remove);
                            */
        if (getEntityStream()
                .anyMatch(
                        x ->
                                x.getComponent(UIComponent.class)
                                        .map(UIComponent.class::cast)
                                        .map(y -> y.isVisible() && y.isPauses())
                                        .get())) {
            pauseGame();
        } else {
            unpauseGame();
        }
    }

    private void pauseGame() {
        Game.systems.forEach((t, u) -> u.stop());
    }

    private void unpauseGame() {
        Game.systems.forEach((t, u) -> u.run());
    }

    @Override
    public void stop() {}

    @Override
    public void run() {}
}
