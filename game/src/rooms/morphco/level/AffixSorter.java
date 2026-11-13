package rooms.morphco.level;

import engine.Entity;
import engine.Game;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.sound.SoundSpec;
import feature.components.UIComponent;
import feature.emote.Emote;
import feature.hud.DialogUtils;
import feature.hud.UIUtils;
import feature.hud.dialogs.ChoiceOption;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogFactory;
import feature.systems.EventScheduler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import rooms.morphco.petrinet.MorphCoMilestone;
import rooms.morphco.puzzles.AffixPuzzle;
import rooms.morphco.puzzles.LeverPull;
import rooms.morphco.puzzles.MorphemeQuestions;
import rooms.morphco.puzzles.Pace;
import rooms.morphco.ui.MorphCoDialogs;
import rooms.morphco.util.MorphCoAchievements;
import rooms.morphco.util.MorphCoText;
import rooms.morphco.util.MorphCoTracking;

/**
 * The affix sorter A-12.
 *
 * <p>Every part of the sorter in the world opens its close-up. The maintenance hatch is locked with
 * a question about free morphemes. Behind it, players cut the jammed words into morphs and sort the
 * affixes. The freed affixes then go into the input shafts for word formation or inflection, and an
 * even pull on the lever restarts the sorter.
 */
final class AffixSorter {

  private static final String MACHINE = "world.a12.machine-title";
  private static final String HATCH = "world.a12.hatch-title";
  private static final String SHELF = "world.a12.shelf-title";
  private static final int LEVER_ACHIEVEMENT_PULLS = 5;

  /** The close-up shows the restart for a moment before it closes. */
  private static final int RESTART_MILLIS = 1500;

  private final MorphCoLevel level;
  private final MorphCoState state;
  private final List<Integer> hamsterJokes = new ArrayList<>();
  private int lastJoke;
  private Entity lock;
  private Entity display;

  AffixSorter(MorphCoLevel level) {
    this.level = level;
    this.state = level.state();
  }

  /** Creates the sorter, its lock and hatch and the inventory of the hall. */
  void spawn() {
    // The whole machine: nothing to highlight in the close-up.
    on("affix-sorter", (self, player) -> closeUp(player, ""));
    // Lever and hatch are part of the sorter's picture; their objects are click areas.
    on("a12-lever", (self, player) -> closeUp(player, MorphCoDialogs.A12_LEVER));
    on("hatch", (self, player) -> closeUp(player, MorphCoDialogs.A12_HATCH));
    MorphCoObjects.on(
        "hatch-lock",
        entity -> {
          lock = entity;
          MorphCoProps.interact(entity, (self, player) -> closeUp(player, MorphCoDialogs.A12_LOCK));
          if (state.done(MorphCoMilestone.HATCH_UNLOCKED)) MorphCoProps.switchOn(entity);
        });
    MorphCoObjects.on(
        "a12-display",
        entity -> {
          display = entity;
          MorphCoProps.interact(
              entity, (self, player) -> closeUp(player, MorphCoDialogs.A12_DISPLAY));
          if (state.done(MorphCoMilestone.A12_REPAIRED)) MorphCoProps.switchOn(entity);
        });
    on(
        "poster_a12",
        (self, player) -> {
          MorphCoProps.picture(
              player, "poster_noeat", "world.a12.poster-title", "world.a12.poster");
          level.seen(MorphCoState.POSTER_PREFIX, "poster_a12", player);
        });
    on(
        "picture",
        (self, player) ->
            MorphCoProps.picture(
                player, "picture_marmot", "world.a12.picture-title", "world.a12.picture"));
    on("shelf-a12", (self, player) -> shelf(player));
  }

  private static void on(String name, BiConsumer<Entity, Entity> onInteract) {
    MorphCoObjects.on(name, entity -> MorphCoProps.interact(entity, onInteract));
  }

  // Savegames from before the lever step are repaired without the flag.
  private boolean filled() {
    return state.has(MorphCoState.SHAFTS_FILLED) || state.done(MorphCoMilestone.A12_REPAIRED);
  }

  private void closeUp(Entity player, String part) {
    if (!state.done(MorphCoMilestone.A12_REPAIRED)) level.observe(MorphCoState.A12_DISPLAY_SEEN);
    if (!state.done(MorphCoMilestone.HATCH_UNLOCKED)) level.observe(MorphCoState.LOCK_SEEN);
    DialogContext context =
        DialogContext.builder()
            .type(MorphCoDialogs.AFFIX_SORTER)
            .put(MorphCoDialogs.A12_PART, part)
            .put(MorphCoDialogs.HATCH_OK, state.done(MorphCoMilestone.HATCH_UNLOCKED))
            .put(MorphCoDialogs.AFFIXES_OK, state.done(MorphCoMilestone.AFFIXES_SORTED))
            .put(MorphCoDialogs.SHAFTS_OK, filled())
            .put(MorphCoDialogs.A12_OK, state.done(MorphCoMilestone.A12_REPAIRED))
            .build();
    UIComponent ui = DialogFactory.show(context, player.id());
    ui.registerCallback(
        MorphCoDialogs.SUBMIT,
        payload -> {
          if (!(payload instanceof DialogResponseMessage.StringValue(String encoded))) return;
          MorphCoDialogs.A12Answer answer = MorphCoDialogs.A12Answer.parse(encoded);
          switch (answer.part()) {
            case MorphCoDialogs.A12_LOCK -> unlock(answer.value(), player, ui);
            case MorphCoDialogs.A12_HATCH -> hatch(player, ui);
            case MorphCoDialogs.A12_LEVER -> lever(answer.value(), player, ui);
            default -> {
              // Nothing else in the close-up answers.
            }
          }
        });
  }

  // Close-ups opened before a step still show the old state; clicking the sorter again opens a new
  // one.
  private static void closeCloseUps() {
    closeUps().toList().forEach(ui -> UIUtils.closeDialog(ui, true));
  }

  // As above, but the close-up of the player who took the step stays open.
  private static void closeOtherCloseUps(UIComponent own) {
    closeUps().filter(ui -> ui != own).toList().forEach(ui -> UIUtils.closeDialog(ui, true));
  }

  private static Stream<UIComponent> closeUps() {
    return Game.levelEntities()
        .flatMap(entity -> entity.fetch(UIComponent.class).stream())
        .filter(ui -> ui.dialogContext().dialogType() == MorphCoDialogs.AFFIX_SORTER);
  }

  private void unlock(String answer, Entity player, UIComponent closeUp) {
    if (state.done(MorphCoMilestone.HATCH_UNLOCKED)) return;
    boolean correct = MorphemeQuestions.hatchAnswer(answer);
    MorphCoTracking.attempt(
        MorphCoTracking.Puzzle.HATCH, "hatch-lock", "number", answer, correct, player);
    if (!correct) {
      Game.audio().playGlobal(SoundSpec.builder("kenney_ui_error_002").targets(player.id()));
      return;
    }
    if (!level.complete(MorphCoMilestone.HATCH_UNLOCKED)) return;
    closeOtherCloseUps(closeUp);
    MorphCoProps.switchOn(lock);
    Game.audio().playGlobal(SoundSpec.builder("retro_event_correct"));
    MorphCoTracking.solved(MorphCoTracking.Puzzle.HATCH);
    MorphCoTracking.started(MorphCoTracking.Puzzle.AFFIXES);
  }

  private void hatch(Entity player, UIComponent closeUp) {
    // The close-up answers a locked hatch and a sorted one itself.
    if (!state.done(MorphCoMilestone.HATCH_UNLOCKED) || filled()) return;
    UIUtils.closeDialog(closeUp, true);
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

  private void lever(String pull, Entity player, UIComponent closeUp) {
    if (state.done(MorphCoMilestone.A12_REPAIRED)) return;
    if (!state.has(MorphCoState.SHAFTS_FILLED)) {
      if (state.pullLever() == LEVER_ACHIEVEMENT_PULLS) MorphCoAchievements.LEVER.unlock(player);
      MorphCoTracking.interaction("a12-lever", "pull", player);
      return;
    }
    boolean accepted = LeverPull.PACE.rate(pull) == Pace.Result.ACCEPTED;
    MorphCoTracking.attempt(
        MorphCoTracking.Puzzle.LEVER, "a12-lever", "pull", pull, accepted, player);
    if (!accepted) {
      Game.audio().playGlobal(SoundSpec.builder("kenney_ui_error_001").targets(player.id()));
      return;
    }
    if (!level.complete(MorphCoMilestone.A12_REPAIRED)) return;
    closeOtherCloseUps(closeUp);
    MorphCoProps.switchOn(display);
    Game.audio().playGlobal(SoundSpec.builder("system_recovery_relay_activate"));
    MorphCoTracking.solved(MorphCoTracking.Puzzle.LEVER);
    MorphCoAchievements.MARIE_KONDO.unlock();
    EventScheduler.scheduleAction(
        () -> {
          // The player may have closed it already.
          if (closeUps().anyMatch(ui -> ui == closeUp)) UIUtils.closeDialog(closeUp, true);
          MorphCoProps.popupReaction(player, Emote.STARS, MACHINE, "world.a12.shafts-solved");
        },
        RESTART_MILLIS);
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
          closeCloseUps();
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
          if (!level.observe(MorphCoState.SHAFTS_FILLED)) return;
          level.save();
          closeCloseUps();
          Game.audio().playGlobal(SoundSpec.builder("retro_event_correct"));
          MorphCoTracking.solved(MorphCoTracking.Puzzle.SHAFTS);
          MorphCoTracking.started(MorphCoTracking.Puzzle.LEVER);
          MorphCoProps.popupThen(
              player,
              MACHINE,
              "world.a12.shafts-filled",
              () -> closeUp(player, MorphCoDialogs.A12_LEVER));
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
    } else if ("hamster".equals(book)) {
      DialogFactory.show(
          DialogContext.builder()
              .type(MorphCoDialogs.HAMSTER_BOOK)
              .put(MorphCoDialogs.JOKE, nextJoke())
              .build(),
          player.id());
    } else {
      DialogUtils.showTextPopup(
          MorphCoText.key("world.a12.book-" + book + "-text"), MorphCoText.key(SHELF), player.id());
    }
  }

  // Every opening shows another joke; once all have been shown, they are shuffled again, and the
  // last one shown does not come first.
  private int nextJoke() {
    if (hamsterJokes.isEmpty()) {
      for (int joke = 1; joke <= MorphCoDialogs.HAMSTER_JOKES; joke++) hamsterJokes.add(joke);
      Collections.shuffle(hamsterJokes);
      if (hamsterJokes.getLast() == lastJoke)
        Collections.swap(hamsterJokes, 0, hamsterJokes.size() - 1);
    }
    lastJoke = hamsterJokes.removeLast();
    return lastJoke;
  }
}
