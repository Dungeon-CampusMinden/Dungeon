package contrib.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

import contrib.components.IdleSoundComponent;

import core.System;
import core.utils.components.MissingComponentException;

import java.util.Random;

/**
 * Works on Entities that contain the {@link IdleSoundComponent} and plays the stored sound effect
 * randomly.
 *
 * <p>Use this if you want to add some white noise monster sounds to your game.
 *
 * <p>Note: The chance that the sound is played is very low, so it shouldn't be too much noise.
 */
public final class IdleSoundSystem extends System {

    private final float CHANCE_TO_PLAY_SOUND = 0.001f;
    private final Random RANDOM = new Random();

    /** Create a new {@link IdleSoundSystem} */
    public IdleSoundSystem() {
        super(IdleSoundComponent.class);
    }

    @Override
    public void execute() {
        entityStream()
                .forEach(
                        e ->
                                playSound(
                                        e.fetch(IdleSoundComponent.class)
                                                .orElseThrow(
                                                        () ->
                                                                MissingComponentException.build(
                                                                        e,
                                                                        IdleSoundComponent
                                                                                .class))));
    }

    private void playSound(IdleSoundComponent component) {
        if (RANDOM.nextFloat(0f, 1f) < CHANCE_TO_PLAY_SOUND) {
            Music soundEffect = Gdx.audio.newMusic(Gdx.files.internal(component.soundEffect()));
            soundEffect.setLooping(false);
            soundEffect.play();
            soundEffect.setVolume(.35f);
        }
    }
}
