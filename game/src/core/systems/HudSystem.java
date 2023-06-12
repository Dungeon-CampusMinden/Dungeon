package core.systems;

import com.badlogic.gdx.scenes.scene2d.Group;

import core.Game;
import core.System;
import core.components.UIComponent;

public final class HudSystem extends System {

    public HudSystem() {
        super(UIComponent.class);
    }

    @Override
    public void execute() {
        // Temp fix until either addEntity is available or removeEntity until dungeon Issue #727
        // implemented
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
                                        .map(y -> y.isVisible() && y.isPausesGame())
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
