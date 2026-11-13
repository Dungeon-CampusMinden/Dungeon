package rooms.morphco.puzzles;

/**
 * The card reader at the entrance: the security card has to be swiped through the slot at the right
 * pace.
 *
 * <p>The server opens the door only for {@link Pace.Result#ACCEPTED}; letting go before the card
 * reached the end of the slot counts as {@link Pace.Result#INCOMPLETE}.
 */
public final class CardSwipe {

  /** Between 0.3 and 1.2 s. Among Us asks for about 0.5 s; the reader is more forgiving. */
  public static final Pace PACE = new Pace(300, 1200);

  private CardSwipe() {}
}
