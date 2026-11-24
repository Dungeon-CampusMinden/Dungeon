package rooms.morphco.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import engine.Game;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.utils.Cursors;
import feature.canvas.CanvasGraphics;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.HeadlessDialogGroup;
import feature.interaction.sorting.SortBoard;
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
 * <p>Every word lies on a cutting bench. Clicking between two letters cuts it there. The parts
 * become chips that can be sorted into the prefix, suffix and circumfix bins; a word without any
 * affix goes as a whole into the bin "Kein Affix". Another cut or a joined cut takes only the
 * changed parts out of the bins, so the rest of a word can be cut further. Closing the hatch leaves
 * changed cuts and bins with the server, which hands them back on the next opening. A hatch closed
 * unchanged sends nothing, so it does not undo what another player left there in the meantime.
 */
final class AffixUI extends MorphCoConsoleUI {

  private static final float SPACE = 16;

  private final MorphCoSortBoard board =
      new MorphCoSortBoard(area(), MorphCoSortBoard.Look.COMPARTMENT, this::changed);
  private final Map<String, TreeSet<Integer>> cuts = new HashMap<>();
  private final Map<String, PanelNode> benches = new LinkedHashMap<>();
  private final Map<String, Table> letterRows = new HashMap<>();
  private final Map<String, Map<String, SortBoard.Slot>> parts = new HashMap<>();
  private final Map<String, Label> marks = new HashMap<>();
  // The cuts and bins the hatch opened with.
  private final String opened;
  private final List<SortBoard.Bin> bins = new ArrayList<>();

  private AffixUI(DialogContext context) {
    super(
        context,
        MorphCoText.text("ui.affixes.title"),
        "",
        MorphCoText.text("ui.affixes.instruction") + "\n\n" + MorphCoText.text("ui.affixes.help"));
    Map<String, AffixPuzzle.WordAnswer> draft =
        AffixPuzzle.decodeWords(context.find(MorphCoDialogs.AFFIX_DRAFT, String.class).orElse(""));
    for (AffixPuzzle.Word word : AffixPuzzle.words()) {
      TreeSet<Integer> wordCuts = new TreeSet<>();
      AffixPuzzle.WordAnswer earlier = draft.get(word.id());
      if (earlier != null) wordCuts.addAll(earlier.cuts());
      cuts.put(word.id(), wordCuts);
      PanelNode bench =
          area().addNode(new PanelNode("bench-" + word.id(), PanelNode.Look.PLATE, 6));
      Table letters = new Table();
      Label mark = MorphCoUI.bold("", 16, Color.WHITE);
      mark.setAlignment(Align.center);
      Table display = new Table();
      display.setBackground(MorphCoUI.screen());
      display.add(mark).grow().pad(4, 6, 4, 6);
      bench.content().left();
      bench.content().add(letters).left().padLeft(6);
      bench.content().add().growX();
      bench.content().add(display).width(108).height(34).padLeft(SPACE);
      benches.put(word.id(), bench);
      letterRows.put(word.id(), letters);
      marks.put(word.id(), mark);
      rebuildWord(word);
    }
    for (AffixPuzzle.Bin bin : AffixPuzzle.Bin.values()) {
      bins.add(board.addBin(bin.name(), MorphCoText.text("ui.affixes." + bin.name()), 0, 130));
    }
    restoreBins(draft);
    opened = AffixPuzzle.encodeWords(answers());
    // Escape and the pad's back button close the hatch the same way as its close button.
    UIUtils.onCloseRequest(this, this::requestClose);
    button("ui.reset", false, this::reset);
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
    return new AffixUI(context);
  }

  @Override
  protected float arrange(float width, float height) {
    // The bins stand next to the words, so no part has to be dragged out of view.
    float binsWidth = Math.clamp(width * .36f, 340, 480);
    float wordsWidth = width - binsWidth - 2 * SPACE;
    float benchWidth = 0;
    for (PanelNode bench : benches.values()) {
      benchWidth = Math.max(benchWidth, bench.content().getPrefWidth() + 12);
    }
    float partsLeft = benchWidth + SPACE;
    float top = 0;
    for (Map.Entry<String, PanelNode> entry : benches.entrySet()) {
      PanelNode bench = entry.getValue();
      List<SortBoard.Slot> slots = List.copyOf(parts.get(entry.getKey()).values());
      float benchHeight = bench.fit(benchWidth);
      SortBoard.Flow flow =
          SortBoard.flow(
              slots.stream().map(SortBoard.Slot::measure).toList(), wordsWidth - partsLeft);
      bench.position(0, top - benchHeight);
      float slotTop = top - (benchHeight - SortBoard.CHIP_HEIGHT) / 2;
      for (int index = 0; index < slots.size(); index++) {
        Vector2 offset = flow.offsets().get(index);
        slots.get(index).position(partsLeft + offset.x, slotTop - offset.y - SortBoard.CHIP_HEIGHT);
      }
      top -= Math.max(benchHeight, flow.height()) + 8;
    }
    float binsHeight = SortBoard.grid(bins, 2, wordsWidth + 2 * SPACE, 0, binsWidth);
    board.arrange();
    return Math.max(-top, binsHeight);
  }

  private void rebuildWord(AffixPuzzle.Word word) {
    Table letters = letterRows.get(word.id());
    letters.clearChildren();
    String text = word.text();
    TreeSet<Integer> wordCuts = cuts.get(word.id());
    for (int index = 0; index < text.length(); index++) {
      if (index > 0) letters.add(new Gap(word, index, wordCuts.contains(index))).size(14, 34);
      Label letter = MorphCoUI.bold(String.valueOf(text.charAt(index)), 24, MorphCoUI.TEXT);
      letter.setWrap(false);
      letters.add(letter);
    }

    marks.get(word.id()).setText("");
    // Only the part a cut splits or joins is replaced; the others stay in their bins. All names
    // have the same length, so removeItems() with a whole ID takes just that part.
    Map<String, SortBoard.Slot> old = parts.getOrDefault(word.id(), Map.of());
    Map<String, SortBoard.Slot> slots = new LinkedHashMap<>();
    AffixPuzzle.parts(text, new ArrayList<>(wordCuts))
        .forEach((name, piece) -> {
          String id = word.id() + "#" + name;
          SortBoard.Slot slot = old.get(id);
          slots.put(id, slot != null ? slot : board.addItem(id, piece, 19));
        });
    old.keySet().stream().filter(id -> !slots.containsKey(id)).forEach(board::removeItems);
    parts.put(word.id(), slots);
    changed();
  }

  private void restoreBins(Map<String, AffixPuzzle.WordAnswer> draft) {
    draft.forEach((wordId, answer) -> {
      List<String> ids = List.copyOf(parts.getOrDefault(wordId, Map.of()).keySet());
      for (int index = 0; index < Math.min(ids.size(), answer.bins().size()); index++) {
        AffixPuzzle.Bin bin = answer.bins().get(index);
        if (bin != null) board.restore(ids.get(index), bin.name());
      }
    });
  }

  /** Leaves changed cuts and bins with the server, so the next opening starts from them. */
  @Override
  public void requestClose() {
    String draft = AffixPuzzle.encodeWords(answers());
    if (!draft.equals(opened))
      area().fireServerEvent(MorphCoDialogs.DRAFT, new DialogResponseMessage.StringValue(draft));
    super.requestClose();
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
      List<AffixPuzzle.Bin> wordBins = new ArrayList<>();
      for (String id : parts.get(word.id()).keySet()) {
        String bin = placement.get(id);
        wordBins.add(bin == null ? null : AffixPuzzle.Bin.valueOf(bin));
      }
      answers.put(word.id(), new AffixPuzzle.WordAnswer(wordCuts, wordBins));
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
      mark.setColor(correct ? MorphCoUI.GO : MorphCoUI.STOP);
    }
    if (result.solved()) {
      feedback(MorphCoText.text("world.a12.affixes-solved"), true);
    } else {
      feedback(
          MorphCoText.text("ui.affixes.feedback", result.correctWords(), result.totalWords()),
          false);
    }
  }

  /** The space between two letters: a dotted line while open, a red blade once cut. */
  private final class Gap extends Actor {
    private final boolean cut;
    private final ClickListener click;

    private Gap(AffixPuzzle.Word word, int position, boolean cut) {
      this.cut = cut;
      setTouchable(Touchable.enabled);
      setUserObject(Cursors.INTERACT);
      click =
          new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
              TreeSet<Integer> wordCuts = cuts.get(word.id());
              if (!wordCuts.remove(position)) wordCuts.add(position);
              rebuildWord(word);
            }
          };
      addListener(click);
    }

    @Override
    public void draw(Batch batch, float alpha) {
      Color color = click.isOver() ? MorphCoUI.SIGNAL : cut ? MorphCoUI.STOP : MorphCoUI.MUTED;
      float center = getX() + getWidth() / 2;
      if (cut) {
        CanvasGraphics.fill(batch, color, alpha, center - 2, getY(), 4, getHeight());
        return;
      }
      for (float y = getY() + 5; y < getY() + getHeight() - 5; y += 7)
        CanvasGraphics.fill(batch, color, alpha, center - 1, y, 2, 3);
    }
  }
}
