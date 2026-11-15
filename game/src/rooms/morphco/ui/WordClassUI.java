package rooms.morphco.ui;

import com.badlogic.gdx.scenes.scene2d.Group;
import engine.Game;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.HeadlessDialogGroup;
import feature.interaction.sorting.SortBoard;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import rooms.morphco.puzzles.WordClassPuzzle;
import rooms.morphco.util.MorphCoText;

/**
 * The crates next to the inflection machine F-04.
 *
 * <p>The words of two sentences arrive on conveyor belts. Players drag them into the crates of the
 * inflectable word classes; words without a fitting crate stay on the belt. The client gives
 * immediate feedback; the server checks every submitted placement again and closes the dialog when
 * it is correct.
 */
final class WordClassUI extends MorphCoConsoleUI {

  private final SortBoard board = new MorphCoSortBoard(area(), this::changed);
  private final List<PanelNode> belts = new ArrayList<>();
  private final List<List<SortBoard.Slot>> sentences = new ArrayList<>();
  private final List<SortBoard.Bin> crates = new ArrayList<>();

  private WordClassUI(DialogContext context) {
    super(
        context,
        MorphCoText.text("ui.word-classes.title"),
        MorphCoText.text("ui.word-classes.instruction"),
        MorphCoText.text("ui.word-classes.help"));
    for (int sentence = 0; sentence < WordClassPuzzle.sentenceCount(); sentence++) {
      PanelNode belt = area().addNode(new PanelNode("belt" + sentence, PanelNode.Look.BELT, 10));
      belt.content()
          .add(MorphCoUI.tag(MorphCoText.text("ui.word-classes.table", sentence + 1)))
          .padTop(4);
      belts.add(belt);
      int index = sentence;
      sentences.add(
          WordClassPuzzle.words().stream()
              .filter(word -> word.sentence() == index)
              .map(word -> board.item(word.id(), word.text(), 21))
              .toList());
    }
    for (WordClassPuzzle.Crate crate : WordClassPuzzle.Crate.values()) {
      crates.add(
          board.bin(crate.name(), MorphCoText.text("ui.word-classes." + crate.name()), 0, 150));
    }
    button("ui.reset", false, board::clear);
    button("ui.check", true, this::check);
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

  @Override
  protected float arrange(float width, float height) {
    float top = 0;
    for (int sentence = 0; sentence < belts.size(); sentence++) {
      top -= MorphCoSortBoard.belt(belts.get(sentence), sentences.get(sentence), top, width) + 14;
    }
    top -= 10;
    top -= SortBoard.grid(crates, width >= 900 ? crates.size() : 3, 0, top, width);
    board.arrange();
    return -top;
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
