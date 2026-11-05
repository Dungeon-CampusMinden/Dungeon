package rooms.morphco.ui;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import engine.Game;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.HeadlessDialogGroup;
import java.util.LinkedHashMap;
import java.util.Map;
import rooms.morphco.puzzles.WordClassPuzzle;
import rooms.morphco.util.MorphCoText;

/**
 * The crates next to the inflection machine F-04.
 *
 * <p>Players drag the words of two sentences into the crates of the inflectable word classes. Words
 * without a fitting crate stay in their sentence. The client gives immediate feedback; the server
 * checks every submitted placement again and closes the dialog when it is correct.
 */
final class WordClassUI extends MorphCoPanel {

  private final SortBoard board;

  private WordClassUI(DialogContext context) {
    super(
        context,
        MorphCoText.text("ui.word-classes.title"),
        MorphCoText.text("ui.word-classes.instruction"),
        MorphCoText.text("ui.word-classes.help"));
    board = new SortBoard(skin, this::clearFeedback);
    WordClassPuzzle.words().forEach(word -> board.item(word.id(), word.text()));

    Table sentences = new Table();
    sentences.defaults().left().padBottom(8);
    for (int sentence = 0; sentence < WordClassPuzzle.sentenceCount(); sentence++) {
      HorizontalGroup row = new HorizontalGroup();
      row.space(8);
      row.wrap(true);
      row.wrapSpace(6);
      int index = sentence;
      WordClassPuzzle.words().stream()
          .filter(word -> word.sentence() == index)
          .forEach(word -> row.addActor(board.homeChip(word.id())));
      sentences
          .add(text(MorphCoText.text("ui.word-classes.table", sentence + 1), 18))
          .width(90)
          .top();
      sentences.add(row).width(contentWidth() - 110f).row();
    }
    body.add(sentences).growX().padBottom(12).row();

    Table crates = new Table();
    crates.defaults().pad(4).grow().uniformX().width(contentWidth() / 5f - 10f);
    for (WordClassPuzzle.Crate crate : WordClassPuzzle.Crate.values()) {
      crates.add(
          board.bin(crate.name(), MorphCoText.text("ui.word-classes." + crate.name()), 0, 110));
    }
    body.add(crates).growX();
    board.refresh();

    button("ui.reset", "blue-outline", board::clear);
    button("ui.check", "green", this::check);
  }

  /**
   * Builds the dialog or a headless placeholder on the server.
   *
   * @param context dialog context
   * @return dialog group
   */
  static Group build(DialogContext context) {
    if (Game.isHeadless()) return new HeadlessDialogGroup();
    return new WordClassUI(context);
  }

  private void check() {
    Map<String, WordClassPuzzle.Crate> placement = new LinkedHashMap<>();
    board
        .placement()
        .forEach(
            (word, crate) ->
                WordClassPuzzle.Crate.parse(crate)
                    .ifPresent(parsed -> placement.put(word, parsed)));
    WordClassPuzzle.Result result = WordClassPuzzle.check(placement);
    submit(WordClassPuzzle.encode(placement));
    if (result.solved()) {
      feedback(MorphCoText.text("world.f04.crates-solved"), true);
    } else if (result.misplaced() > 0 && result.missing() > 0) {
      feedback(
          MorphCoText.text("ui.word-classes.feedback", result.misplaced(), result.missing()),
          false);
    } else if (result.misplaced() > 0) {
      feedback(MorphCoText.text("ui.word-classes.feedback-misplaced", result.misplaced()), false);
    } else {
      feedback(MorphCoText.text("ui.word-classes.feedback-missing", result.missing()), false);
    }
  }
}
