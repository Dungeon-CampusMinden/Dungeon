package core.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;

import core.Game;
import core.System;
import core.components.UIComponent;
import core.utils.Constants;

public class HudSystem extends System {

    private Stage stage;

    public HudSystem(SpriteBatch batch) {
        this(
                new Stage(
                        new ScalingViewport(
                                Scaling.stretch, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT),
                        batch));
    }

    public HudSystem(Stage stage) {
        super(UIComponent.class);
        this.stage = stage;
        Gdx.input.setInputProcessor(stage);
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
                            if (!stage.getActors().contains(d, true)) {
                                stage.addActor(d);
                                d.setVisible(false);
                            }
                        });
        stage.getActors()
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

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
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
