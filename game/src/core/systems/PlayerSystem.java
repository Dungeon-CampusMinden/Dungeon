package core.systems;

import com.badlogic.gdx.Gdx;

import core.Entity;
import core.System;
import core.components.PlayerComponent;
import core.utils.components.MissingComponentException;

import java.util.Map;

/**
 * Controls the Player.
 *
 * <p>Will work on Entities that implement the {@link PlayerComponent}.
 *
 * <p>This System will check for each registered callback in the {@link PlayerComponent} if the Key
 * is pressed, and if so, will execute the Callback.
 */
public final class PlayerSystem extends System {

    private boolean running = true;

    public PlayerSystem() {
        super(PlayerComponent.class);
    }

    @Override
    public void execute() {
        entityStream().forEach(this::execute);
    }

    private void execute(Entity entity) {
        PlayerComponent pc =
                entity.fetch(PlayerComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                entity, PlayerComponent.class));
        execute(pc.callbacks(), entity, !this.running);
    }

    @Override
    public void stop() {
        this.run = true; // This system can not be stopped.
        this.running = false;
    }

    @Override
    public void run() {
        this.run = true;
        this.running = true;
    }

    /**
     * Execute the callback function registered to a key when it is pressed.
     *
     * <p>The callbacks are executed only if the game is not paused or if the callback is not
     * pauseable.
     *
     * @param entity associated entity of this component.
     * @param paused if the game is paused or not.
     */
    private void execute(
            Map<Integer, PlayerComponent.InputData> callbacks,
            final Entity entity,
            boolean paused) {
        callbacks.forEach(
                (key, value) -> {
                    if (!paused || !value.pauseable()) {
                        execute(entity, key, value);
                    }
                });
    }

    private void execute(Entity entity, int key, final PlayerComponent.InputData data) {
        if ((!data.repeat()
                        && (Gdx.input.isKeyJustPressed(key) || Gdx.input.isButtonJustPressed(key)))
                || (data.repeat()
                        && (Gdx.input.isKeyPressed(key) || Gdx.input.isButtonJustPressed(key)))) {
            data.callback().accept(entity);
        }
    }
}
