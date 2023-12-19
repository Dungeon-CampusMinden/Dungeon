package contrib.components;

import core.Component;
import core.utils.components.path.IPath;

/**
 * Stores a String path to a sound file that can be played by the {@link
 * contrib.systems.IdleSoundSystem}.
 *
 * @param soundEffect Path to the sound file to play.
 * @see contrib.systems.IdleSoundSystem
 */
public record IdleSoundComponent(IPath soundEffect) implements Component {}
