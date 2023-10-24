package contrib.components;

import core.Component;

/**
 * Stores a String path to a sound file that can be played by the {@link
 * contrib.systems.IdleSoundSystem}.
 *
 * @param soundEffect Path to the sound file to play.
 * @see contrib.systems.IdleSoundSystem
 */
public record IdleSoundComponent(String soundEffect) implements Component {}
