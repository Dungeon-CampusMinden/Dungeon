package core.systems;

import com.badlogic.gdx.scenes.scene2d.Group;

import core.Game;
import core.System;
import core.components.UIComponent;

public final class HudSystem extends System {

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
        // Temp fix until either addEntity is available or removeEntity
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
                        });

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
        Game.systems.values().forEach(System::stop);
    }

    private void unpauseGame() {
        Game.systems.values().forEach(System::run);
    }

    /** HudSystem can´t be paused */
    @Override
    public void stop() {}

    /** HudSystem is always running no need to log it */
    @Override
    public void run() {}
}
