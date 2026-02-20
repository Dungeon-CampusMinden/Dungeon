package contrib.modules.worldTimer;

import core.Component;

/**
 * Component that stores a timestamp (UNIX) for a timer.
 *
 * @param timestamp the timestamp (UNIX) for the timer
 */
public record WorldTimerComponent(int timestamp, int duration) implements Component {}
