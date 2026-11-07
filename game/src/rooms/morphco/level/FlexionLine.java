package rooms.morphco.level;

import engine.Entity;
import engine.Game;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.sound.SoundSpec;
import engine.utils.Rectangle;
import feature.components.UIComponent;
import feature.entities.deco.Deco;
import feature.hud.UIUtils;
import feature.hud.dialogs.ChoiceOption;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import rooms.morphco.petrinet.MorphCoMilestone;
import rooms.morphco.puzzles.WordClassPuzzle;
import rooms.morphco.ui.MorphCoDialogs;
import rooms.morphco.util.MorphCoAchievements;
import rooms.morphco.util.MorphCoText;
import rooms.morphco.util.MorphCoTracking;

/**
 * The inflection line F-04.
 *
 * <p>The handbook on the shelf explains which word classes can be inflected. With it, players sort
 * the words of two sentences into the crates next to the machine. The machine itself works again
 * once the crates are right and A-12 delivers the correct affixes.
 */
final class FlexionLine {

  /** Faulty word forms on the conveyor belt, in the order of the script. */
  static final List<String> FAULTY_FORMS =
      List.of("du schreiben", "Gemäuse", "undes", "zerkleinst");

  private static final String MACHINE = "world.f04.machine-title";
  private static final String SHELF = "world.f04.shelf-title";

  private final MorphCoLevel level;
  private final MorphCoState state;
  private Entity screen;

  FlexionLine(MorphCoLevel level) {
    this.level = level;
    this.state = level.state();
  }

  /** Creates the machine, the crates, the conveyor belt and the inventory of the hall. */
  void spawn() {
    MorphCoProps.texture(
        "flexion-machine",
        level.point("flexion_machine"),
        MorphCoProps.ART + "flexion_machine.png",
        3f,
        new Rectangle(2.6f, 1.4f, 0.2f, 0f),
        (self, player) -> machine(player));
    screen =
        MorphCoProps.switchable(
            "f04-screen",
            level.point("f04_screen"),
            MorphCoProps.ART + "display_f04_error.png",
            MorphCoProps.ART + "display_f04_ok.png",
            1f,
            null,
            (self, player) ->
                MorphCoProps.popup(
                    player,
                    "world.f04.screen-title",
                    state.done(MorphCoMilestone.F04_REPAIRED)
                        ? "world.f04.screen-fixed"
                        : "world.f04.screen"));
    if (state.done(MorphCoMilestone.F04_REPAIRED)) MorphCoProps.switchOn(screen);

    // One crate per inflectable word class, labelled like the crates in the sorting dialog.
    WordClassPuzzle.Crate[] classes = WordClassPuzzle.Crate.values();
    for (int index = 0; index < classes.length; index++) {
      MorphCoProps.texture(
          "crate" + index,
          level.point("crate" + index),
          MorphCoProps.ART + "crate_" + classes[index].name().toLowerCase(Locale.ROOT) + ".png",
          1.375f,
          new Rectangle(1.4f, 0.7f, 0.05f, 0f),
          (self, player) -> crates(player));
    }

    for (int index = 0; index < 13; index++) {
      MorphCoProps.texture(
          "conveyor" + index,
          level.point("conveyor").translate(index, 0),
          "objects/tech/transport_conveyor_tile.png",
          1f,
          null,
          (self, player) ->
              MorphCoProps.popup(player, "world.f04.conveyor-title", "world.f04.conveyor"));
    }
    for (int index = 0; index < FAULTY_FORMS.size(); index++) {
      String form = FAULTY_FORMS.get(index);
      MorphCoProps.texture(
          "conveyor-word" + index,
          level.point("conveyor_word" + index),
          "items/rpg/item_paper.png",
          0.7f,
          null,
          (self, player) -> conveyorWord(player, form));
    }

    MorphCoProps.deco(
        "shelf-f04-a", level.point("shelf_f04_a"), Deco.BookShelf, (s, p) -> shelf(p));
    MorphCoProps.deco(
        "shelf-f04-b", level.point("shelf_f04_b"), Deco.BookshelfLarge, (s, p) -> shelf(p));
    MorphCoProps.deco(
        "trash-f04",
        level.point("trash_f04"),
        Deco.TrashCanRed,
        (self, player) -> {
          level.observe(MorphCoState.COMPLAINT_SEEN);
          MorphCoProps.popup(player, "world.f04.trash-title", "world.f04.trash");
        });
    poster("poster_f04_1", "poster_form.png", "world.f04.poster1");
    poster("poster_f04_2", "poster_warning.png", "world.f04.poster2");
    MorphCoProps.texture(
        "plant-f04",
        level.point("plant_f04"),
        "office/plant.png",
        1.2f,
        new Rectangle(0.9f, 0.5f, 0.15f, 0f),
        (self, player) -> {
          MorphCoProps.popup(player, "world.f04.plant-title", "world.f04.plant");
          level.seen(MorphCoState.PLANT_PREFIX, "f04", player);
        });
    MorphCoProps.texture(
        "f04-door-sign",
        level.point("f04_door_sign"),
        MorphCoProps.ART + "sign_f04.png",
        0.94f,
        null,
        (self, player) -> MorphCoProps.popup(player, "world.titles.f04", "world.f04.sign"));
  }

  private void poster(String point, String art, String textKey) {
    MorphCoProps.texture(
        point,
        level.point(point),
        MorphCoProps.ART + art,
        1.3f,
        null,
        (self, player) -> {
          MorphCoProps.popup(player, "world.f04.poster-title", textKey);
          level.seen(MorphCoState.POSTER_PREFIX, point, player);
        });
  }

  private void conveyorWord(Entity player, String form) {
    level.observe(MorphCoState.CONVEYOR_SEEN);
    MorphCoProps.popup(
        player,
        "world.f04.conveyor-title",
        state.done(MorphCoMilestone.F04_REPAIRED)
            ? "world.f04.conveyor-word-fixed"
            : "world.f04.conveyor-word",
        form);
  }

  private void shelf(Entity player) {
    List<ChoiceOption> options = new ArrayList<>();
    options.add(book("book-green", "green"));
    options.add(book("book-zero", "zero"));
    options.add(book("book-handbook", "handbook"));
    options.add(book("book-evidentia", "evidentia"));
    options.add(book("book-morph1", "morph1"));
    options.add(ChoiceOption.of(MorphCoText.key("world.f04.close-shelf"), "close"));
    DialogFactory.showMultipleChoiceDialog(
        MorphCoText.key("world.f04.shelf"),
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
    return ChoiceOption.of(MorphCoText.key("world.f04." + key), value);
  }

  private void read(String book, Entity player) {
    if ("close".equals(book)) return;
    level.seen(MorphCoState.BOOK_PREFIX, "f04-" + book, player);
    Game.audio().playGlobal(SoundSpec.builder("flipping_through_book"));
    switch (book) {
      case "handbook" -> {
        MorphCoDocuments.show(player, MorphCoDialogs.HANDBOOK);
        if (level.complete(MorphCoMilestone.HANDBOOK_READ)) {
          MorphCoTracking.interaction("handbook", "read", player);
        }
      }
      case "morph1" -> {
        MorphCoDocuments.show(player, MorphCoDialogs.MORPH_1);
        level.observe(MorphCoState.MORPH1_READ);
      }
      default -> MorphCoProps.popup(player, SHELF, "world.f04.book-" + book + "-text");
    }
  }

  private void crates(Entity player) {
    if (state.done(MorphCoMilestone.WORD_CLASSES_SORTED)) {
      MorphCoProps.popup(player, "world.f04.crates-title", "world.f04.crates-done");
      return;
    }
    if (!state.done(MorphCoMilestone.HANDBOOK_READ)) {
      MorphCoProps.popup(player, "world.f04.crates-title", "world.f04.crates-locked");
      return;
    }
    UIComponent ui =
        DialogFactory.show(
            DialogContext.builder().type(MorphCoDialogs.WORD_CLASSES).build(), player.id());
    ui.registerCallback(
        MorphCoDialogs.SUBMIT,
        payload -> {
          if (!(payload instanceof DialogResponseMessage.StringValue(String answer))) return;
          WordClassPuzzle.Result result = WordClassPuzzle.check(WordClassPuzzle.decode(answer));
          MorphCoTracking.attempt(
              MorphCoTracking.Puzzle.WORD_CLASSES,
              "crates",
              "assignment",
              answer,
              result.solved(),
              player);
          if (!result.solved()) {
            Game.audio().playGlobal(SoundSpec.builder("kenney_ui_error_001").targets(player.id()));
            return;
          }
          UIUtils.closeDialog(ui, true);
          if (!level.complete(MorphCoMilestone.WORD_CLASSES_SORTED)) return;
          Game.audio().playGlobal(SoundSpec.builder("retro_event_correct"));
          MorphCoTracking.solved(MorphCoTracking.Puzzle.WORD_CLASSES);
          MorphCoAchievements.WORD_CLASSES.unlock();
          MorphCoProps.popup(
              player,
              "world.f04.crates-title",
              state.done(MorphCoMilestone.A12_REPAIRED)
                  ? "world.f04.crates-solved-a12"
                  : "world.f04.crates-solved");
        });
  }

  private void machine(Entity player) {
    String text;
    if (state.done(MorphCoMilestone.F04_REPAIRED)) {
      text = "world.f04.machine-done";
    } else if (!state.done(MorphCoMilestone.HANDBOOK_READ)) {
      text = "world.f04.machine-locked";
    } else {
      boolean crates = state.done(MorphCoMilestone.WORD_CLASSES_SORTED);
      boolean affixes = state.done(MorphCoMilestone.A12_REPAIRED);
      if (crates && affixes) {
        repair(player);
        return;
      }
      level.observe(MorphCoState.MACHINE_INSPECTED);
      MorphCoTracking.started(MorphCoTracking.Puzzle.FLEXION);
      if (!crates && !affixes) text = "world.f04.machine-inspect";
      else text = crates ? "world.f04.machine-need-a12" : "world.f04.machine-need-crates";
    }
    MorphCoProps.popup(player, MACHINE, text);
  }

  private void repair(Entity player) {
    if (!level.complete(MorphCoMilestone.F04_REPAIRED)) return;
    MorphCoProps.switchOn(screen);
    Game.audio().playGlobal(SoundSpec.builder("system_recovery_conveyor_start"));
    MorphCoTracking.solved(MorphCoTracking.Puzzle.FLEXION);
    MorphCoAchievements.FLEXION.unlock();
    MorphCoProps.popup(player, MACHINE, "world.f04.machine-repaired");
  }
}
