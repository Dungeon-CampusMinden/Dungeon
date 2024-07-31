package level.utils;

/** ITickable interface for objects that need to perform actions on each tick. */
public interface ITickable {

  /**
   * Method to be executed on each tick.
   *
   * @param isFirstTick A boolean indicating whether this is the first tick.
   */
  void onTick(boolean isFirstTick);
}
