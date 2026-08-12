package feature.timer;

import engine.Component;

/**
 * Component that stores a timestamp (UNIX) for a timer.
 *
 * @param timestamp the timestamp (UNIX) for the timer
 * @param duration the duration of the timer in seconds
 */
public record WorldTimerComponent(int timestamp, int duration) implements Component {}
