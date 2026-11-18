package rooms.morphco.level;

import engine.Entity;
import feature.hud.dialogs.DialogFactory;
import java.util.Locale;
import rooms.morphco.util.MorphCoText;
import rooms.morphco.util.MorphCoTracking;

/**
 * The maintenance hotline behind the phone in the control room.
 *
 * <p>It helps with the step the shift lead would talk about, in three stages: first the direction,
 * then a concrete tip and, after a warning, the solution. Declining the warning costs nothing; once
 * the solution has been given, the hotline repeats it. The stages count for the whole room: they
 * are flags of the room state, so the shared quest log lists them and the savegame keeps them. A
 * savegame without such flags starts at the first stage.
 */
final class HintHotline {

  private static final String TITLE = "hotline.title";

  private final MorphCoLevel level;
  private final MorphCoState state;

  HintHotline(MorphCoLevel level) {
    this.level = level;
    this.state = level.state();
  }

  /**
   * Answers a call with the next stage of the current step.
   *
   * @param player calling player
   */
  void call(Entity player) {
    Offer offer = offer(state);
    // After the restart there is nothing left to stage.
    if (offer.step() == ShiftLead.Step.DONE) {
      MorphCoProps.popup(player, TITLE, "story.hint-done");
      return;
    }
    if (offer.stage() != Stage.SOLUTION || state.has(offer.flag())) {
      give(offer, player);
      return;
    }
    DialogFactory.showYesNoDialog(
        MorphCoText.key("hotline.warning"),
        MorphCoText.key(TITLE),
        () -> {
          // Another player may have solved the step during the warning.
          if (offer(state).equals(offer)) give(offer, player);
          else call(player);
        },
        () -> {},
        player.id());
  }

  private void give(Offer offer, Entity player) {
    // Another player may have heard this stage a moment earlier; it counts once.
    if (level.observe(offer.flag())) {
      MorphCoTracking.hintUsed(offer.step().puzzle(), offer.id(), player);
      level.save();
    }
    MorphCoProps.popup(player, TITLE, offer.text());
  }

  /**
   * Chooses what the hotline says next: the first stage of the current step that the room has not
   * heard yet, or, once all stages have been heard, the solution again.
   *
   * @param state current room state
   * @return step and stage
   */
  static Offer offer(MorphCoState state) {
    ShiftLead.Step step = ShiftLead.hint(state);
    for (Stage stage : Stage.values()) {
      Offer offer = new Offer(step, stage);
      if (!state.has(offer.flag())) return offer;
    }
    return new Offer(step, Stage.SOLUTION);
  }

  /** Stages of every step, from the direction to the solution. */
  enum Stage {
    /** Where to look. */
    DIRECTION,
    /** A concrete tip. */
    TIP,
    /** The solution; the hotline warns before it. */
    SOLUTION;

    /**
     * Returns the stable identifier of the stage.
     *
     * @return stage key such as {@code tip}
     */
    String key() {
      return name().toLowerCase(Locale.ROOT);
    }
  }

  /**
   * A stage of a step that the hotline offers.
   *
   * @param step step as the shift lead chooses it
   * @param stage stage of the step
   */
  record Offer(ShiftLead.Step step, Stage stage) {

    /**
     * Returns the stable identifier of the stage, used for the tracking and the flag.
     *
     * @return step and stage such as {@code hint-alarm:tip}
     */
    String id() {
      return step.key() + ":" + stage.key();
    }

    /**
     * Returns the flag that records this stage as heard.
     *
     * @return flag of the room state
     */
    String flag() {
      return MorphCoState.HINT_PREFIX + id();
    }

    /**
     * Returns the text of this stage.
     *
     * @return text key below {@code morphCo}
     */
    String text() {
      return "hotline." + step.key() + "." + stage.key();
    }
  }
}
