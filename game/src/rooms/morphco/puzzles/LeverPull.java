package rooms.morphco.puzzles;

import feature.interaction.cardreader.Pace;

/**
 * The lever of the affix sorter A-12: once the input shafts are filled, an even pull from the top
 * to the bottom restarts the sorter.
 *
 * <p>The server repairs A-12 only for {@link Pace.Result#ACCEPTED}; letting go before the lever
 * reached the bottom counts as {@link Pace.Result#INCOMPLETE}.
 */
public final class LeverPull {

  /** Between 0.6 and 2 s to the bottom; a quick flick of the mouse takes about 0.3 s. */
  public static final Pace PACE = new Pace(600, 2000);

  private LeverPull() {}
}
