package rooms.morphco.level;

import engine.Entity;
import engine.Game;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.sound.SoundSpec;
import engine.sound.Sounds;
import feature.components.UIComponent;
import feature.emote.Emote;
import feature.hud.DialogUtils;
import feature.hud.UIUtils;
import feature.hud.dialogs.ChoiceOption;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.interaction.cardreader.Pace;
import feature.systems.EventScheduler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import rooms.morphco.items.MorphemeTypesItem;
import rooms.morphco.petrinet.MorphCoMilestone;
import rooms.morphco.puzzles.AffixPuzzle;
import rooms.morphco.puzzles.LeverPull;
import rooms.morphco.puzzles.MorphemeQuestions;
import rooms.morphco.ui.MorphCoDialogs;
import rooms.morphco.util.MorphCoAchievements;
import rooms.morphco.util.MorphCoSounds;
import rooms.morphco.util.MorphCoText;
import rooms.morphco.util.MorphCoTracking;

/**
 * The affix sorter A-12.
 *
 * <p>Every part of the sorter in the world opens its close-up; the monitor on the wall shows the
 * sorter's status, like the screen in F-04. The maintenance hatch is locked with a question about
 * free morphemes. Behind it, players cut the jammed words into morphs and sort the affixes. The
 * freed affixes then go into the input shafts for word formation or inflection. Once the
 * circumfixes from the {@link CircumfixStation} lie in the magazine as well, an even pull on the
 * lever restarts the sorter; until then the lever is stuck.
 */
final class AffixSorter {

  private static final String MACHINE = "world.a12.machine-title";
  private static final String SCREEN = "world.a12.screen-title";
  private static final String HATCH = "world.a12.hatch-title";
  private static final String SHELF = "world.a12.shelf-title";
  private static final int LEVER_ACHIEVEMENT_PULLS = 5;

  /** The close-up shows the restart for a moment before it closes. */
  private static final int RESTART_MILLIS = 1500;

  /** The belts that bring the affixes to A-12 and on to F-04; they run once A-12 does. */
  static final List<String> BELTS = List.of("a12-supply", "a12-conveyor", "f04-feed");

  private final MorphCoLevel level;
  private final MorphCoState state;
  private final List<Integer> hamsterJokes = new ArrayList<>();
  private int lastJoke;
  private Entity sorter;
  private Entity lock;
  private Entity display;

  AffixSorter(MorphCoLevel level) {
    this.level = level;
    this.state = level.state();
  }

  /** Creates the sorter, its lock and hatch and the inventory of the hall. */
  void spawn() {
    MorphCoObjects.on(
        "affix-sorter",
        entity -> {
          sorter = entity;
          // The whole machine: nothing to highlight in the close-up.
          MorphCoProps.interact(entity, (self, player) -> closeUp(player, ""));
          if (state.done(MorphCoMilestone.A12_REPAIRED)) MorphCoProps.switchOn(entity);
        });
    for (String name : BELTS) {
      MorphCoObjects.on(
          name,
          entity -> {
            if (state.done(MorphCoMilestone.A12_REPAIRED)) MorphCoProps.switchOn(entity);
          });
    }
    // Lever and hatch are part of the sorter's picture; their objects are click areas.
    MorphCoObjects.onInteract(
        "a12-lever", (self, player) -> closeUp(player, MorphCoDialogs.A12_LEVER));
    MorphCoObjects.onInteract("hatch", (self, player) -> closeUp(player, MorphCoDialogs.A12_HATCH));
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
              entity, (self, player) -> MorphCoProps.popup(player, SCREEN, displayText()));
          if (state.done(MorphCoMilestone.A12_REPAIRED)) MorphCoProps.switchOn(entity);
        });
    MorphCoObjects.onInteract(
        "poster_a12",
        (self, player) -> {
          MorphCoProps.picture(player, "poster_noeat", "world.a12.poster");
          level.seen(MorphCoState.POSTER_PREFIX, "poster_a12", player);
        });
    MorphCoObjects.onInteract(
        "picture",
        (self, player) -> MorphCoProps.picture(player, "picture_marmot", "world.a12.picture"));
    MorphCoObjects.onInteract("shelf-a12", (self, player) -> shelf(player));
  }

  private String displayText() {
    if (state.done(MorphCoMilestone.A12_REPAIRED)) return "pc.status-a12-ok";
    if (state.waitsForCircumfixes()) return "pc.status-a12-empty";
    return state.has(MorphCoState.SHAFTS_FILLED) ? "pc.status-a12-restart" : "pc.status-a12-error";
  }

  private boolean filled() {
    return state.has(MorphCoState.SHAFTS_FILLED);
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
            .put(MorphCoDialogs.A12_EMPTY, state.waitsForCircumfixes())
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
            default -> {
              // Nothing else in the close-up answers.
            }
          }
        });
    ui.registerCallback(MorphCoDialogs.LEVER_PULL, leverCallback(true, player, ui));
    ui.registerCallback(MorphCoDialogs.LEVER_INCOMPLETE, leverCallback(false, player, ui));
  }

  private Consumer<DialogResponseMessage.Payload> leverCallback(
      boolean complete, Entity player, UIComponent closeUp) {
    return payload -> {
      if (payload instanceof DialogResponseMessage.LongValue(long millis)) {
        lever(millis, complete, player, closeUp);
      }
    };
  }

  private void unlock(String answer, Entity player, UIComponent closeUp) {
    if (state.done(MorphCoMilestone.HATCH_UNLOCKED)) return;
    boolean correct = MorphemeQuestions.hatchAnswer(answer);
    MorphCoTracking.attempt(
        MorphCoTracking.Puzzle.HATCH, "hatch-lock", "number", answer, correct, player);
    if (!correct) {
      Sounds.playFor(MorphCoSounds.HATCH_LOCKED, player);
      return;
    }
    if (!level.complete(MorphCoMilestone.HATCH_UNLOCKED)) return;
    // Close-ups opened before a step still show the old state; clicking the sorter again opens a
    // new one. The close-up of the player who took the step stays open.
    UIUtils.closeDialogs(
        MorphCoDialogs.AFFIX_SORTER, context -> context != closeUp.dialogContext());
    MorphCoProps.switchOn(lock);
    Game.audio().playGlobal(SoundSpec.builder("retro_event_correct"));
    MorphCoTracking.solved(MorphCoTracking.Puzzle.HATCH);
    MorphCoTracking.started(MorphCoTracking.Puzzle.AFFIXES);
  }

  private void hatch(Entity player, UIComponent closeUp) {
    // The close-up answers a locked hatch and one with filled shafts itself.
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

  private void lever(long millis, boolean complete, Entity player, UIComponent closeUp) {
    if (state.done(MorphCoMilestone.A12_REPAIRED)) return;
    if (state.leverStuck()) {
      if (state.pullLever() == LEVER_ACHIEVEMENT_PULLS) MorphCoAchievements.LEVER.unlock(player);
      MorphCoTracking.interaction("a12-lever", "pull", player);
      return;
    }
    boolean accepted = LeverPull.PACE.rate(millis, complete) == Pace.Result.ACCEPTED;
    // Tracking keeps the duration:complete format of the study data.
    MorphCoTracking.attempt(
        MorphCoTracking.Puzzle.LEVER,
        "a12-lever",
        "pull",
        millis + ":" + complete,
        accepted,
        player);
    if (!accepted) {
      Sounds.playFor(MorphCoSounds.WRONG, player);
      return;
    }
    if (!level.complete(MorphCoMilestone.A12_REPAIRED)) return;
    UIUtils.closeDialogs(
        MorphCoDialogs.AFFIX_SORTER, context -> context != closeUp.dialogContext());
    MorphCoProps.switchOn(sorter);
    MorphCoProps.switchOn(display);
    Game.levelEntities()
        .filter(entity -> BELTS.contains(entity.name()))
        .forEach(MorphCoProps::switchOn);
    Game.audio().playGlobal(SoundSpec.builder("system_recovery_relay_activate"));
    MorphCoTracking.solved(MorphCoTracking.Puzzle.LEVER);
    MorphCoAchievements.MARIE_KONDO.unlock();
    EventScheduler.scheduleAction(
        () -> {
          // The player may have closed it already.
          if (closeUp.dialogContext().findEntity(DialogContextKeys.OWNER_ENTITY).isPresent())
            UIUtils.closeDialog(closeUp, true);
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
            Sounds.playFor(MorphCoSounds.WRONG, player);
            return;
          }
          UIUtils.closeDialogs(MorphCoDialogs.AFFIXES, context -> true);
          if (!level.complete(MorphCoMilestone.AFFIXES_SORTED)) return;
          UIUtils.closeDialogs(MorphCoDialogs.AFFIX_SORTER, context -> true);
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
            Sounds.playFor(MorphCoSounds.WRONG, player);
            return;
          }
          UIUtils.closeDialogs(MorphCoDialogs.SHAFTS, context -> true);
          if (!level.observe(MorphCoState.SHAFTS_FILLED)) return;
          level.save();
          UIUtils.closeDialogs(MorphCoDialogs.AFFIX_SORTER, context -> true);
          Game.audio().playGlobal(SoundSpec.builder("retro_event_correct"));
          MorphCoTracking.solved(MorphCoTracking.Puzzle.SHAFTS);
          if (!state.leverStuck()) MorphCoTracking.started(MorphCoTracking.Puzzle.LEVER);
          MorphCoProps.popupThen(
              player,
              MACHINE,
              state.waitsForCircumfixes()
                  ? "world.a12.shafts-filled-empty"
                  : "world.a12.shafts-filled",
              () -> closeUp(player, MorphCoDialogs.A12_LEVER));
        });
  }

  private void shelf(Entity player) {
    List<ChoiceOption> options =
        List.of(
            book("book-syntagma", "syntagma"),
            book("book-morph2", "morph2"),
            book("book-hamster", "hamster"));
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
    level.seen(MorphCoState.BOOK_PREFIX, "a12-" + book, player);
    Game.audio().playGlobal(SoundSpec.builder("flipping_through_book"));
    if ("morph2".equals(book)) {
      MorphCoBooks.take(player, new MorphemeTypesItem.Part2());
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
