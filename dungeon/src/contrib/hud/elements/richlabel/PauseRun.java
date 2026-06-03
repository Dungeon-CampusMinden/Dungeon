package contrib.hud.elements.richlabel;

/**
 * A control run that pauses the typewriter for the specified duration. Has no effect when
 * typewriter mode is disabled (speed is 0).
 *
 * @param duration pause duration in seconds
 */
public record PauseRun(float duration) implements Run {}
