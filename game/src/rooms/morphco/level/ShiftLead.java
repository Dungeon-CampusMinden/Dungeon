package rooms.morphco.level;

import engine.Entity;
import feature.emote.Emote;
import feature.hud.dialogs.DialogFactory;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
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
    Hint hint = hint();
    MorphCoTracking.hintUsed(hint.puzzle(), hint.key(), player);
    DialogFactory.showDialogDialog(MorphCoText.shiftLead(hint.key()), this::showMood, player.id());
  }

  /**
   * Shows how the shift lead feels about the progress: in a panic while the alarm is on, relieved
   * once it is off and happy after the restart.
   */
  void showMood() {
    switch (hint().key()) {
      case "hint-alarm" -> MorphCoProps.emote(npc, Emote.EXCLAMATIONS);
      case "hint-pc" -> MorphCoProps.emote(npc, Emote.FACE_HAPPY);
      case "hint-done" -> MorphCoProps.emote(npc, Emote.HEARTS);
      default -> {
        // During the repairs she just waits.
      }
    }
  }

  /**
   * Chooses the hint for the first open step.
   *
   * @return hint key and puzzle
   */
  Hint hint() {
    if (state.done(MorphCoMilestone.PRODUCTION_RESTARTED)) {
      return new Hint("hint-done", MorphCoTracking.Puzzle.RESTART);
    }
    if (!state.done(MorphCoMilestone.ALARM_OFF)) {
      return new Hint("hint-alarm", MorphCoTracking.Puzzle.ALARM);
    }
    if (!state.done(MorphCoMilestone.CAUSES_FOUND)) {
      return new Hint("hint-pc", MorphCoTracking.Puzzle.CAUSES);
    }
    boolean a12 = state.done(MorphCoMilestone.A12_REPAIRED);
    boolean f04 = state.done(MorphCoMilestone.F04_REPAIRED);
    if (a12 && f04) return new Hint("hint-restart", MorphCoTracking.Puzzle.RESTART);
    boolean a12Started = state.has(MorphCoState.LOCK_SEEN) || state.has(MorphCoState.HATCH_OPENED);
    if (!state.has(MorphCoState.F04_ENTERED) && !a12Started) {
      return new Hint("hint-repair", MorphCoTracking.Puzzle.CAUSES);
    }
    // A started branch is finished first; the order of the two machines is free.
    if (state.done(MorphCoMilestone.AFFIXES_SORTED) && !a12) {
      return new Hint("hint-shafts", MorphCoTracking.Puzzle.SHAFTS);
    }
    if (state.done(MorphCoMilestone.HATCH_UNLOCKED) && !a12) {
      return new Hint("hint-affixes", MorphCoTracking.Puzzle.AFFIXES);
    }
    if (a12Started
        && !state.done(MorphCoMilestone.HATCH_UNLOCKED)
        && !state.done(MorphCoMilestone.WORD_CLASSES_SORTED)) {
      return new Hint("hint-hatch", MorphCoTracking.Puzzle.HATCH);
    }
    if (!state.done(MorphCoMilestone.HANDBOOK_READ)) {
      return new Hint("hint-handbook", MorphCoTracking.Puzzle.WORD_CLASSES);
    }
    if (!state.done(MorphCoMilestone.WORD_CLASSES_SORTED)) {
      return new Hint("hint-word-classes", MorphCoTracking.Puzzle.WORD_CLASSES);
    }
    if (!state.done(MorphCoMilestone.HATCH_UNLOCKED)) {
      return new Hint("hint-hatch", MorphCoTracking.Puzzle.HATCH);
    }
    return new Hint("hint-flexion", MorphCoTracking.Puzzle.FLEXION);
  }

  /**
   * A hint of the shift lead.
   *
   * @param key story key below {@code morphCo.story}
   * @param puzzle puzzle the hint belongs to
   */
  record Hint(String key, MorphCoTracking.Puzzle puzzle) {}
}
