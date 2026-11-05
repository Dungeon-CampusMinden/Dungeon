package rooms.morphco.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import engine.Game;
import engine.utils.Cursors;
import engine.utils.Scene2dElementFactory;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.HeadlessDialogGroup;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import rooms.morphco.puzzles.AffixPuzzle;
import rooms.morphco.util.MorphCoText;

/**
 * The jammed affix sorter A-12, seen through its maintenance hatch.
 *
 * <p>Clicking between two letters cuts a word. The parts become chips that can be sorted into the
 * prefix, suffix and circumfix bins; a word without any affix goes as a whole into the bin "Kein
 * Affix". Changing the cuts of a word takes its parts out of the bins again.
 */
final class AffixUI extends MorphCoPanel {

  private static final Color GAP_OPEN = new Color(0.4f, 0.42f, 0.5f, 1f);
  private static final Color GAP_CUT = new Color(0.75f, 0.1f, 0.1f, 1f);

  private final SortBoard board;
  private final Map<String, TreeSet<Integer>> cuts = new HashMap<>();
  private final Map<String, HorizontalGroup> partRows = new HashMap<>();
  private final Map<String, Table> letterRows = new HashMap<>();
  private final Map<String, Label> marks = new HashMap<>();

  private AffixUI(DialogContext context) {
    super(
        context,
        MorphCoText.text("ui.affixes.title"),
        MorphCoText.text("ui.affixes.instruction"),
        MorphCoText.text("ui.affixes.help"));
    board = new SortBoard(skin, this::clearFeedback);

    Table words = new Table();
    words.defaults().left().padBottom(5);
    float wordsWidth = contentWidth() * 0.6f;
    for (AffixPuzzle.Word word : AffixPuzzle.words()) {
      cuts.put(word.id(), new TreeSet<>());
      Table letters = new Table();
      letterRows.put(word.id(), letters);
      HorizontalGroup parts = new HorizontalGroup();
      parts.space(6);
      partRows.put(word.id(), parts);
      Label mark = Scene2dElementFactory.createLabel("", 18, INK);
      marks.put(word.id(), mark);
      words.add(letters).width(320).left();
      words.add(parts).width(wordsWidth - 400f).left();
      words.add(mark).width(80).right().row();
      rebuildWord(word);
    }
    // The bins stand next to the words, so no part has to be dragged out of view.
    float binsWidth = contentWidth() - wordsWidth - 20f;
    Table bins = new Table();
    bins.defaults().pad(4).grow().uniformX().width(binsWidth / 2f - 8f);
    for (AffixPuzzle.Bin bin : AffixPuzzle.Bin.values()) {
      bins.add(board.bin(bin.name(), MorphCoText.text("ui.affixes." + bin.name()), 0, 120));
      if (bin.ordinal() % 2 == 1) bins.row();
    }
    body.add(words).width(wordsWidth).top().left();
    body.add(bins).width(binsWidth).top().padLeft(20);
    board.refresh();

    button("ui.reset", "blue-outline", this::reset);
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
    return new AffixUI(context);
  }

  private void rebuildWord(AffixPuzzle.Word word) {
    Table letters = letterRows.get(word.id());
    letters.clearChildren();
    String text = word.text();
    TreeSet<Integer> wordCuts = cuts.get(word.id());
    for (int index = 0; index < text.length(); index++) {
      if (index > 0) letters.add(gap(word, index, wordCuts.contains(index))).height(30);
      Label letter = Scene2dElementFactory.createLabel(String.valueOf(text.charAt(index)), 22, INK);
      letters.add(letter);
    }

    Label mark = marks.get(word.id());
    if (mark != null) mark.setText("");
    board.removeItems(word.id() + "#");
    HorizontalGroup parts = partRows.get(word.id());
    parts.clearChildren();
    List<String> pieces = AffixPuzzle.split(text, new ArrayList<>(wordCuts));
    for (int index = 0; index < pieces.size(); index++) {
      String id = word.id() + "#" + index;
      board.item(id, pieces.get(index));
      parts.addActor(board.homeChip(id, 18));
    }
  }

  private Table gap(AffixPuzzle.Word word, int position, boolean cut) {
    Table gap = new Table();
    gap.setTouchable(Touchable.enabled);
    gap.setUserObject(Cursors.INTERACT);
    gap.add(Scene2dElementFactory.createLabel(cut ? "|" : "·", 26, cut ? GAP_CUT : GAP_OPEN))
        .width(14);
    gap.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            TreeSet<Integer> wordCuts = cuts.get(word.id());
            if (!wordCuts.remove(position)) wordCuts.add(position);
            rebuildWord(word);
            board.refresh();
            clearFeedback();
          }
        });
    return gap;
  }

  private void reset() {
    cuts.values().forEach(TreeSet::clear);
    AffixPuzzle.words().forEach(this::rebuildWord);
    board.clear();
  }

  private Map<String, AffixPuzzle.WordAnswer> answers() {
    Map<String, String> placement = board.placement();
    Map<String, AffixPuzzle.WordAnswer> answers = new LinkedHashMap<>();
    for (AffixPuzzle.Word word : AffixPuzzle.words()) {
      List<Integer> wordCuts = new ArrayList<>(cuts.get(word.id()));
      int parts = AffixPuzzle.split(word.text(), wordCuts).size();
      List<AffixPuzzle.Bin> bins = new ArrayList<>();
      for (int index = 0; index < parts; index++) {
        String bin = placement.get(word.id() + "#" + index);
        bins.add(bin == null ? null : AffixPuzzle.Bin.valueOf(bin));
      }
      answers.put(word.id(), new AffixPuzzle.WordAnswer(wordCuts, bins));
    }
    return answers;
  }

  private void check() {
    Map<String, AffixPuzzle.WordAnswer> answers = answers();
    AffixPuzzle.Result result = AffixPuzzle.check(answers);
    submit(AffixPuzzle.encodeWords(answers));
    for (AffixPuzzle.Word word : AffixPuzzle.words()) {
      boolean correct = AffixPuzzle.correct(word, answers.get(word.id()));
      Label mark = marks.get(word.id());
      mark.setText(MorphCoText.text(correct ? "ui.affixes.mark-ok" : "ui.affixes.mark-wrong"));
      mark.setColor(correct ? GOOD : BAD);
    }
    if (result.solved()) {
      feedback(MorphCoText.text("world.a12.affixes-solved"), true);
    } else {
      feedback(
          MorphCoText.text("ui.affixes.feedback", result.correctWords(), result.totalWords()),
          false);
    }
  }
}
