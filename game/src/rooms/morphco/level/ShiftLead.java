package rooms.morphco.level;

import engine.Entity;
import feature.emote.Emote;
import feature.hud.dialogs.DialogFactory;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import java.util.Locale;
import rooms.morphco.petrinet.MorphCoMilestone;
import rooms.morphco.util.MorphCoText;
import rooms.morphco.util.MorphCoTracking;

/**
 * The shift lead in front of the control room.
 *
 * <p>Talking to her gives a hint that fits the current progress. Hints are tracked like the hint
 * phone of the other rooms. After a talk her mood shows as an emote above her.
 */
final class ShiftLead {

  private final MorphCoLevel level;
  private final MorphCoState state;
  private Entity npc;

  ShiftLead(MorphCoLevel level) {
    this.level = level;
    this.state = level.state();
  }

  /** Places the shift lead in front of the control room. */
  void spawn() {
    MorphCoObjects.on(
        "shift-lead",
        entity -> {
          npc = entity;
          entity.remove(InteractionComponent.class);
          entity.add(
              new InteractionComponent(
                  new Interaction((self, player) -> talk(player), 1.6f, true)));
        });
  }

  private void talk(Entity player) {
    Step step = hint(state);
    MorphCoTracking.hintUsed(step.puzzle(), step.key(), player);
    DialogFactory.showDialogDialog(MorphCoText.shiftLead(step.key()), this::showMood, player.id());
  }

  /**
   * Shows how the shift lead feels about the progress: in a panic while the alarm is on, smiling
   * while the causes are searched and full of hearts after the restart.
   */
  void showMood() {
    switch (hint(state)) {
      case ALARM -> MorphCoProps.emote(npc, Emote.EXCLAMATIONS);
      case PC -> MorphCoProps.emote(npc, Emote.FACE_HAPPY);
      case DONE -> MorphCoProps.emote(npc, Emote.HEARTS);
      default -> {
        // During the repairs she just waits.
      }
    }
  }

  /**
   * Chooses the hint for the first open step. The hint hotline gives its staged hints for the same
   * step.
   *
   * @param state current room state
   * @return the first open step
   */
  static Step hint(MorphCoState state) {
    if (state.done(MorphCoMilestone.PRODUCTION_RESTARTED)) {
      return Step.DONE;
    }
    if (!state.done(MorphCoMilestone.ALARM_OFF)) {
      return Step.ALARM;
    }
    if (!state.done(MorphCoMilestone.CAUSES_FOUND)) {
      return Step.PC;
    }
    boolean a12 = state.done(MorphCoMilestone.A12_REPAIRED);
    boolean f04 = state.done(MorphCoMilestone.F04_REPAIRED);
    if (a12 && f04) return Step.RESTART;
    boolean a12Started = state.has(MorphCoState.LOCK_SEEN) || state.has(MorphCoState.HATCH_OPENED);
    if (!state.has(MorphCoState.F04_ENTERED) && !a12Started) {
      return Step.REPAIR;
    }
    // A started branch is finished first; the order of the two machines is free.
    if (state.has(MorphCoState.SHAFTS_FILLED) && !a12) {
      return Step.LEVER;
    }
    if (state.done(MorphCoMilestone.AFFIXES_SORTED) && !a12) {
      return Step.SHAFTS;
    }
    if (state.done(MorphCoMilestone.HATCH_UNLOCKED) && !a12) {
      return Step.AFFIXES;
    }
    if (a12Started
        && !state.done(MorphCoMilestone.HATCH_UNLOCKED)
        && !state.done(MorphCoMilestone.WORD_CLASSES_SORTED)) {
      return Step.HATCH;
    }
    if (!state.done(MorphCoMilestone.HANDBOOK_READ)) {
      return Step.HANDBOOK;
    }
    if (!state.done(MorphCoMilestone.WORD_CLASSES_SORTED)) {
      return Step.WORD_CLASSES;
    }
    if (!state.done(MorphCoMilestone.HATCH_UNLOCKED)) {
      return Step.HATCH;
    }
    return Step.FLEXION;
  }

  /**
   * The steps the shift lead gives a hint for, in the order of the solution.
   *
   * <p>The names make up the tracking IDs and the savegame flags of the hints; renaming a step
   * breaks old savegames and the tracking data.
   */
  enum Step {
    /** Switch off the alarm at the console. */
    ALARM(MorphCoTracking.Puzzle.ALARM),
    /** Read the production control system. */
    PC(MorphCoTracking.Puzzle.CAUSES),
    /** Go to the halls; neither machine has been started. */
    REPAIR(MorphCoTracking.Puzzle.CAUSES),
    /** Read the handbook of the inflection machine. */
    HANDBOOK(MorphCoTracking.Puzzle.WORD_CLASSES),
    /** Sort the word classes into the crates of F-04. */
    WORD_CLASSES(MorphCoTracking.Puzzle.WORD_CLASSES),
    /** Unlock the maintenance hatch of A-12. */
    HATCH(MorphCoTracking.Puzzle.HATCH),
    /** Cut and sort the affixes. */
    AFFIXES(MorphCoTracking.Puzzle.AFFIXES),
    /** Fill the input shafts. */
    SHAFTS(MorphCoTracking.Puzzle.SHAFTS),
    /** Restart A-12 with the lever. */
    LEVER(MorphCoTracking.Puzzle.LEVER),
    /** Repair the inflection machine. */
    FLEXION(MorphCoTracking.Puzzle.FLEXION),
    /** Restart the production at the computer. */
    RESTART(MorphCoTracking.Puzzle.RESTART),
    /** The production runs again; nothing is left to do. */
    DONE(MorphCoTracking.Puzzle.RESTART);

    private final MorphCoTracking.Puzzle puzzle;

    Step(MorphCoTracking.Puzzle puzzle) {
      this.puzzle = puzzle;
    }

    /**
     * Returns the key of the shift lead's hint, also its tracking ID.
     *
     * @return story key below {@code morphCo.story}, such as {@code hint-word-classes}
     */
    String key() {
      return "hint-" + name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    /**
     * Returns the puzzle the step belongs to.
     *
     * @return puzzle for the tracking
     */
    MorphCoTracking.Puzzle puzzle() {
      return puzzle;
    }
  }
}
