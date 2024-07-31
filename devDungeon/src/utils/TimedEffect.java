package utils;

/**
 * TimedEffect is a functional interface that defines a single method, applyEffect, which applies an
 * effect.
 *
 * <p>TimedEffect is used in the {@link systems.EventScheduler} to define the effect that should be
 * applied after a certain amount of time.
 *
 * @see systems.EventScheduler EventScheduler
 */
@FunctionalInterface
public interface TimedEffect {
  /**
   * Callback method that should get called when the effect should be applied by the {@link
   * systems.EventScheduler}.
   */
  void applyEffect();
}
