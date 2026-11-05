package rooms.morphco.level;

import engine.Entity;
import engine.Game;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.sound.SoundSpec;
import engine.utils.Rectangle;
import feature.components.UIComponent;
import feature.entities.deco.Deco;
import feature.hud.DialogUtils;
import feature.hud.UIUtils;
import feature.hud.dialogs.ChoiceOption;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogFactory;
import java.util.List;
import rooms.morphco.petrinet.MorphCoMilestone;
import rooms.morphco.puzzles.AffixPuzzle;
import rooms.morphco.puzzles.MorphemeQuestions;
import rooms.morphco.ui.MorphCoDialogs;
import rooms.morphco.util.MorphCoAchievements;
import rooms.morphco.util.MorphCoText;
import rooms.morphco.util.MorphCoTracking;

/**
 * The affix sorter A-12.
 *
 * <p>The maintenance hatch is locked with a question about free morphemes. Behind it, players cut
 * the jammed words into morphs and sort the affixes. The freed affixes then go into the input
 * shafts for word formation or inflection, which repairs the sorter.
 */
final class AffixSorter {

  private static final String MACHINE = "world.a12.machine-title";
  private static final String HATCH = "world.a12.hatch-title";
  private static final String LOCK = "world.a12.lock-title";
  private static final String SHELF = "world.a12.shelf-title";
  private static final int LEVER_ACHIEVEMENT_PULLS = 5;

  private final MorphCoLevel level;
  private final MorphCoState state;
  private Entity lock;
  private Entity display;

  AffixSorter(MorphCoLevel level) {
    this.level = level;
    this.state = level.state();
  }

  /** Creates the sorter, its lock and hatch and the inventory of the hall. */
  void spawn() {
    MorphCoProps.texture(
        "affix-sorter",
        level.point("sorter"),
        MorphCoProps.ART + "affix_sorter.png",
        3f,
        new Rectangle(2.8f, 1.2f, 0.1f, 0f),
        (self, player) -> machine(player));
    MorphCoProps.texture(
        "a12-lever",
        level.point("lever"),
        // The lever is part of the sorter's picture; this is its click area.
        MorphCoProps.ART + "hotspot.png",
        0.8f,
        null,
        (self, player) -> lever(player));
    MorphCoProps.texture(
        "hatch",
        level.point("hatch"),
        // The hatch with its lock is drawn on the sorter; this is its click area.
        MorphCoProps.ART + "hotspot.png",
        1f,
        null,
        (self, player) -> hatch(player));
    lock =
        MorphCoProps.switchable(
            "hatch-lock",
            level.point("hatch_lock"),
            "objects/keypad/off.png",
            "objects/keypad/on.png",
            0.45f,
            null,
            (self, player) -> lock(player));
    if (state.done(MorphCoMilestone.HATCH_UNLOCKED)) MorphCoProps.switchOn(lock);
    display =
        MorphCoProps.switchable(
            "a12-display",
            level.point("sorter_display"),
            MorphCoProps.ART + "display_a12_error.png",
            MorphCoProps.ART + "display_a12_ok.png",
            1f,
            null,
            (self, player) -> machine(player));
    if (state.done(MorphCoMilestone.A12_REPAIRED)) MorphCoProps.switchOn(display);

    // Feed belt from the sorter to the sorting compartments.
    for (int index = 0; index < 9; index++) {
      MorphCoProps.texture(
          "a12-conveyor" + index,
          level.point("a12_conveyor").translate(index, 0),
          "objects/tech/transport_conveyor_tile.png",
          1f,
          null,
          null);
    }

    MorphCoProps.texture(
        "poster_a12",
        level.point("poster_a12"),
        MorphCoProps.ART + "poster_noeat.png",
        1.3f,
        null,
        (self, player) -> {
          MorphCoProps.popup(player, "world.a12.poster-title", "world.a12.poster");
          level.seen(MorphCoState.POSTER_PREFIX, "poster_a12", player);
        });
    MorphCoProps.texture(
        "picture",
        level.point("picture"),
        MorphCoProps.ART + "picture_marmot.png",
        1.6f,
        null,
        (self, player) ->
            MorphCoProps.popup(player, "world.a12.picture-title", "world.a12.picture"));
    MorphCoProps.deco("shelf-a12", level.point("shelf_a12"), Deco.BookShelf, (s, p) -> shelf(p));
  }

  private void machine(Entity player) {
    if (state.done(MorphCoMilestone.A12_REPAIRED)) {
      MorphCoProps.popup(player, MACHINE, "world.a12.machine-repaired");
      return;
    }
    level.observe(MorphCoState.A12_DISPLAY_SEEN);
    MorphCoProps.popup(
        player,
        MACHINE,
        state.has(MorphCoState.HATCH_OPENED) ? "world.a12.machine-sorting" : "world.a12.machine");
  }

  private void lever(Entity player) {
    if (state.done(MorphCoMilestone.A12_REPAIRED)) {
      MorphCoProps.popup(player, "world.a12.lever-title", "world.a12.lever-repaired");
      return;
    }
    if (state.pullLever() == LEVER_ACHIEVEMENT_PULLS) MorphCoAchievements.LEVER.unlock(player);
    MorphCoTracking.interaction("a12-lever", "pull", player);
    MorphCoProps.popup(player, "world.a12.lever-title", "world.a12.lever");
  }

  private void lock(Entity player) {
    if (state.done(MorphCoMilestone.HATCH_UNLOCKED)) {
      MorphCoProps.popup(player, LOCK, "world.a12.lock-open");
      return;
    }
    level.observe(MorphCoState.LOCK_SEEN);
    DialogFactory.showInputDialog(
        MorphCoText.key("world.a12.lock-question"),
        MorphCoText.key(LOCK),
        "",
        MorphCoText.key("pc.security-placeholder"),
        MorphCoText.key("pc.security-submit"),
        MorphCoText.key("ui.cancel"),
        payload -> {
          if (payload instanceof DialogResponseMessage.StringValue(String answer)) {
            unlock(answer, player);
          }
        },
        () -> {},
        player.id());
  }

  private void unlock(String answer, Entity player) {
    boolean correct = MorphemeQuestions.hatchAnswer(answer);
    MorphCoTracking.attempt(
        MorphCoTracking.Puzzle.HATCH, "hatch-lock", "number", answer, correct, player);
    if (!correct) {
      Game.audio().playGlobal(SoundSpec.builder("kenney_ui_error_002").targets(player.id()));
      boolean almost = "9".equals(answer == null ? "" : answer.trim());
      MorphCoProps.popup(player, LOCK, almost ? "world.a12.lock-almost" : "world.a12.lock-wrong");
      return;
    }
    if (!level.complete(MorphCoMilestone.HATCH_UNLOCKED)) return;
    MorphCoProps.switchOn(lock);
    Game.audio().playGlobal(SoundSpec.builder("retro_event_correct"));
    MorphCoTracking.solved(MorphCoTracking.Puzzle.HATCH);
    MorphCoTracking.started(MorphCoTracking.Puzzle.AFFIXES);
    MorphCoProps.popup(player, LOCK, "world.a12.lock-correct");
  }

  private void hatch(Entity player) {
    if (!state.done(MorphCoMilestone.HATCH_UNLOCKED)) {
      MorphCoProps.popup(player, HATCH, "world.a12.hatch-locked");
      return;
    }
    if (state.done(MorphCoMilestone.A12_REPAIRED)) {
      MorphCoProps.popup(player, HATCH, "world.a12.hatch-done");
      return;
    }
    if (state.done(MorphCoMilestone.AFFIXES_SORTED)) {
      shafts(player);
      return;
    }
    if (level.observe(MorphCoState.HATCH_OPENED)) {
      MorphCoProps.popupThen(player, HATCH, "world.a12.hatch-open", () -> affixes(player));
    } else {
      affixes(player);
    }
  }

  private void affixes(Entity player) {
    UIComponent ui =
        DialogFactory.show(
            DialogContext.builder().type(MorphCoDialogs.AFFIXES).build(), player.id());
    ui.registerCallback(
        MorphCoDialogs.SUBMIT,
        payload -> {
          if (!(payload instanceof DialogResponseMessage.StringValue(String answer))) return;
          AffixPuzzle.Result result = AffixPuzzle.check(AffixPuzzle.decodeWords(answer));
          int attempt = state.affixAttempt();
          MorphCoTracking.attempt(
              MorphCoTracking.Puzzle.AFFIXES,
              "affix-sorter",
              "segmentation",
              answer,
              result.solved(),
              player);
          if (!result.solved()) {
            Game.audio().playGlobal(SoundSpec.builder("kenney_ui_error_001").targets(player.id()));
            return;
          }
          UIUtils.closeDialog(ui, true);
          if (!level.complete(MorphCoMilestone.AFFIXES_SORTED)) return;
          Game.audio().playGlobal(SoundSpec.builder("retro_event_correct"));
          MorphCoTracking.solved(MorphCoTracking.Puzzle.AFFIXES);
          MorphCoTracking.started(MorphCoTracking.Puzzle.SHAFTS);
          if (attempt == 1) MorphCoAchievements.FIRST_TRY.unlock();
          MorphCoProps.popupThen(player, HATCH, "world.a12.affixes-solved", () -> shafts(player));
        });
  }

  private void shafts(Entity player) {
    UIComponent ui =
        DialogFactory.show(
            DialogContext.builder().type(MorphCoDialogs.SHAFTS).build(), player.id());
    ui.registerCallback(
        MorphCoDialogs.SUBMIT,
        payload -> {
          if (!(payload instanceof DialogResponseMessage.StringValue(String answer))) return;
          boolean correct = AffixPuzzle.checkShafts(AffixPuzzle.decodeShafts(answer));
          MorphCoTracking.attempt(
              MorphCoTracking.Puzzle.SHAFTS, "input-shafts", "assignment", answer, correct, player);
          if (!correct) {
            Game.audio().playGlobal(SoundSpec.builder("kenney_ui_error_001").targets(player.id()));
            return;
          }
          UIUtils.closeDialog(ui, true);
          if (!level.complete(MorphCoMilestone.A12_REPAIRED)) return;
          MorphCoProps.switchOn(display);
          Game.audio().playGlobal(SoundSpec.builder("system_recovery_relay_activate"));
          MorphCoTracking.solved(MorphCoTracking.Puzzle.SHAFTS);
          MorphCoAchievements.MARIE_KONDO.unlock();
          MorphCoProps.popup(player, MACHINE, "world.a12.shafts-solved");
        });
  }

  private void shelf(Entity player) {
    List<ChoiceOption> options =
        List.of(
            book("book-syntagma", "syntagma"),
            book("book-morph2", "morph2"),
            book("book-hamster", "hamster"),
            ChoiceOption.of(MorphCoText.key("world.a12.close-shelf"), "close"));
    DialogFactory.showMultipleChoiceDialog(
        MorphCoText.key("world.a12.shelf"),
        MorphCoText.key(SHELF),
        options,
        false,
        true,
        payload -> {
          if (payload instanceof DialogResponseMessage.StringValue(String book)) read(book, player);
        },
        () -> {},
        player.id());
  }

  private static ChoiceOption book(String key, String value) {
    return ChoiceOption.of(MorphCoText.key("world.a12." + key), value);
  }

  private void read(String book, Entity player) {
    if ("close".equals(book)) return;
    level.seen(MorphCoState.BOOK_PREFIX, "a12-" + book, player);
    Game.audio().playGlobal(SoundSpec.builder("flipping_through_book"));
    if ("morph2".equals(book)) {
      MorphCoDocuments.show(player, MorphCoDialogs.MORPH_2);
      level.observe(MorphCoState.MORPH2_READ);
    } else {
      DialogUtils.showTextPopup(
          MorphCoText.key("world.a12.book-" + book + "-text"), MorphCoText.key(SHELF), player.id());
    }
  }
}
