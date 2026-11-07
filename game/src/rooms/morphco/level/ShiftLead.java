package rooms.morphco.level;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.utils.Vector2;
import engine.utils.components.draw.DepthLayer;
import engine.utils.components.path.SimpleIPath;
import feature.components.CollideComponent;
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
 * phone of the other rooms.
 */
final class ShiftLead {

  private final MorphCoLevel level;
  private final MorphCoState state;

  ShiftLead(MorphCoLevel level) {
    this.level = level;
    this.state = level.state();
  }

  /** Places the shift lead in front of the control room. */
  void spawn() {
    // The lab-coat sprite only has undirected idle and run animations.
    Entity npc = new Entity("shift-lead");
    npc.add(new PositionComponent(level.point("shift_lead")));
    DrawComponent draw = new DrawComponent(new SimpleIPath("character/monster/doc"));
    draw.depth(DepthLayer.Player.depth());
    npc.add(draw);
    CollideComponent collider = new CollideComponent(Vector2.of(0.1f, 0f), Vector2.of(0.8f, 0.6f));
    collider.isSolid(true);
    npc.add(collider);
    npc.add(new InteractionComponent(new Interaction((self, player) -> talk(player), 1.6f, true)));
    Game.add(npc);
  }

  private void talk(Entity player) {
    Hint hint = hint();
    MorphCoTracking.hintUsed(hint.puzzle(), hint.key(), player);
    DialogFactory.showDialogDialog(MorphCoText.shiftLead(hint.key()), () -> {}, player.id());
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
