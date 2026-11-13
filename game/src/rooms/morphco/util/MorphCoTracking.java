package rooms.morphco.util;

import engine.Entity;
import engine.tracking.Tracking;
import java.util.Locale;

/** Server-side tracking hooks for the puzzles of the Morph & Co. room. */
public final class MorphCoTracking {

  /** Puzzles reported to the tracking backend. */
  public enum Puzzle {
    /** Switch off the alarm at the console. */
    ALARM,
    /** Read the production control system. */
    CAUSES,
    /** Sort inflectable words into the crates of F-04. */
    WORD_CLASSES,
    /** Unlock the maintenance hatch of A-12. */
    HATCH,
    /** Cut words and sort their affixes. */
    AFFIXES,
    /** Push the affixes into the word-formation and inflection shafts. */
    SHAFTS,
    /** Restart A-12 with an even pull on the lever. */
    LEVER,
    /** Repair the inflection machine. */
    FLEXION,
    /** Assemble the broken circumfixes (optional). */
    CIRCUMFIX,
    /** Answer the security question and restart the production. */
    RESTART;

    /**
     * Returns the stable tracking identifier.
     *
     * @return puzzle ID such as {@code word-classes}
     */
    public String id() {
      return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }
  }

  private MorphCoTracking() {}

  /**
   * Records that a puzzle became accessible.
   *
   * @param puzzle room puzzle
   */
  public static void started(Puzzle puzzle) {
    Tracking.puzzleStarted(puzzle.id());
  }

  /**
   * Records a puzzle solution once per session.
   *
   * @param puzzle room puzzle
   */
  public static void solved(Puzzle puzzle) {
    Tracking.puzzleSolved(puzzle.id());
  }

  /**
   * Records one server-checked answer.
   *
   * @param puzzle room puzzle
   * @param objectId answered object
   * @param answerKind shape of the raw answer
   * @param rawAnswer submitted answer
   * @param correct whether the server accepted it
   * @param player acting player
   */
  public static void attempt(
      Puzzle puzzle,
      String objectId,
      String answerKind,
      String rawAnswer,
      boolean correct,
      Entity player) {
    Tracking.participantForEntity(player.id())
        .ifPresent(
            participant ->
                Tracking.attempt(
                    puzzle.id(), objectId, answerKind, rawAnswer, correct, participant));
  }

  /**
   * Records an interaction with a world object.
   *
   * @param objectId inspected object
   * @param action performed action
   * @param player acting player
   */
  public static void interaction(String objectId, String action, Entity player) {
    Tracking.participantForEntity(player.id())
        .ifPresent(participant -> Tracking.interaction(objectId, action, participant));
  }

  /**
   * Records a hint given by the shift lead.
   *
   * @param puzzle puzzle the hint is about
   * @param hintId stable hint identifier
   * @param player player who asked
   */
  public static void hintUsed(Puzzle puzzle, String hintId, Entity player) {
    Tracking.participantForEntity(player.id())
        .ifPresent(participant -> Tracking.hintUsed(puzzle.id(), hintId, participant));
  }
}
