package feature.interaction.sorting;

import static feature.interaction.FeatureDialogStyle.GO;
import static feature.interaction.FeatureDialogStyle.INK;
import static feature.interaction.FeatureDialogStyle.MUTED;
import static feature.interaction.FeatureDialogStyle.PANEL;
import static feature.interaction.FeatureDialogStyle.STOP;
import static feature.interaction.FeatureDialogStyle.tinted;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import engine.Game;
import engine.language.Translation;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.utils.BaseContainerUI;
import engine.utils.Cursors;
import engine.utils.Scene2dElementFactory;
import feature.canvas.CanvasArea;
import feature.canvas.CanvasNode;
import feature.canvas.CanvasOptions;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.HeadlessDialogGroup;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * The engine's dialog of a sorting board: title and help, the terms in rows, the bins below them,
 * and the buttons with the result of the last check.
 *
 * <p>"Check" counts the wrong terms right away and sends the placement to the server, which checks
 * it again, see {@link SortingFactory}. A solved board shows the solution and takes no input.
 */
public final class SortingUI extends Table {

  private static final Translation TRANSLATION = new Translation("dialog.sorting_dialog");
  private static final float MAX_WIDTH = 960;
  // Room for the dialog's padding and a margin to the window edges.
  private static final float MARGIN = 160;
  private static final int MAX_COLUMNS = 4;
  private static final float BIN_HEIGHT = 130;

  private final Sorting sorting;
  private final Label feedback = Scene2dElementFactory.createBoldLabel("", 20, Color.WHITE);
  private final CanvasArea area;
  private final SortBoard board;
  private final List<SortBoard.Slot> slots = new ArrayList<>();
  private final List<SortBoard.Bin> bins = new ArrayList<>();
  private final Table buttons = new Table();
  private float arrangedWidth = -1;

  private SortingUI(DialogContext context) {
    sorting = Sorting.from(context);
    area =
        new CanvasArea(
            "sorting",
            MAX_WIDTH,
            BIN_HEIGHT,
            new CanvasOptions()
                .backgroundColor(PANEL)
                .grid(24, false)
                .selectionEnabled(false)
                .multiSelectEnabled(false)
                .rubberBandEnabled(false)
                .keyboardShortcutsEnabled(false)
                .panButton(-1)
                .zoom(1, 1));
    area.dialogId(context.dialogId());
    board = new SortBoard(area, this::changed);
    for (int term = 0; term < sorting.terms().size(); term++) {
      slots.add(board.item(termId(term), sorting.terms().get(term), 20));
    }
    for (int bin = 0; bin < sorting.bins().size(); bin++) {
      bins.add(board.bin(binId(bin), sorting.bins().get(bin), 0, BIN_HEIGHT));
    }

    setUserObject(Cursors.DEFAULT);
    setBackground(tinted(PANEL));
    pad(28, 40, 28, 40);
    defaults().left();
    String title = sorting.title().isBlank() ? TRANSLATION.text("title") : sorting.title();
    String help = sorting.help().isBlank() ? TRANSLATION.text("help") : sorting.help();
    add(Scene2dElementFactory.createBoldLabel(title, 34, INK)).row();
    Label helpLabel = Scene2dElementFactory.createLabel(help, 20, MUTED);
    helpLabel.setWrap(true);
    add(helpLabel).growX().padTop(12).padBottom(20).row();
    add(area).size(MAX_WIDTH, BIN_HEIGHT).row();

    buttons.defaults().padLeft(10);
    buttons.add(button("reset", "default", board::clear));
    buttons.add(button("check", "green", this::check));
    Table footer = new Table();
    footer.add(feedback).growX().minWidth(0);
    footer
        .add(button("close", "default", () -> area.fireServerEvent(DialogContextKeys.ON_CLOSE)))
        .padLeft(10);
    footer.add(buttons);
    add(footer).growX().padTop(20);
    if (context.find(Sorting.SOLVED_KEY, Boolean.class).orElse(false)) showSolution();
  }

  /**
   * Builds the dialog of a sorting board.
   *
   * @param context the dialog context from {@link SortingFactory}
   * @return the dialog, or a placeholder on a headless server
   */
  public static Group build(DialogContext context) {
    if (Game.isHeadless()) return new HeadlessDialogGroup();
    return new BaseContainerUI(new SortingUI(context));
  }

  @Override
  public void act(float delta) {
    float width = Math.min(MAX_WIDTH, Game.windowWidth() - MARGIN);
    if (width != arrangedWidth) {
      arrangedWidth = width;
      getCell(area).size(width, arrange(width));
      invalidateHierarchy();
    }
    super.act(delta);
  }

  @Override
  public void draw(Batch batch, float alpha) {
    // World coordinates start at the top left corner of the canvas and grow upwards.
    area.pan(0, area.getHeight());
    super.draw(batch, alpha);
  }

  // The terms in rows on top, the bins in a grid below; returns the height both need.
  private float arrange(float width) {
    SortBoard.Flow flow =
        SortBoard.flow(slots.stream().map(SortBoard.Slot::measure).toList(), width);
    for (int index = 0; index < slots.size(); index++) {
      Vector2 offset = flow.offsets().get(index);
      slots.get(index).position(offset.x, -offset.y - SortBoard.CHIP_HEIGHT);
    }
    float top = -flow.height() - 24;
    top -= SortBoard.grid(bins, Math.min(bins.size(), MAX_COLUMNS), 0, top, width);
    board.arrange();
    return -top;
  }

  private void changed() {
    feedback.setText("");
    arrangedWidth = -1;
  }

  private void check() {
    int[] placement = placement();
    int wrong = sorting.wrong(placement);
    area.fireServerEvent(
        DialogContextKeys.ON_CONFIRM, new DialogResponseMessage.IntList(placement));
    if (wrong > 0) {
      feedback(TRANSLATION.text("wrong", wrong), false);
      return;
    }
    // The server closes the dialog in a moment.
    lock();
  }

  private int[] placement() {
    Map<String, String> placed = board.placement();
    List<String> binIds = bins.stream().map(CanvasNode::id).toList();
    // indexOf gives -1, i.e. Sorting.NONE, for a term outside of all bins.
    return IntStream.range(0, sorting.terms().size())
        .map(term -> binIds.indexOf(placed.get(termId(term))))
        .toArray();
  }

  private void showSolution() {
    for (int term = 0; term < sorting.terms().size(); term++) {
      int bin = sorting.targets().get(term);
      if (bin != Sorting.NONE) board.place(termId(term), binId(bin));
    }
    lock();
  }

  private void lock() {
    area.setTouchable(Touchable.disabled);
    buttons.remove();
    feedback(TRANSLATION.text("solved"), true);
  }

  private void feedback(String text, boolean good) {
    feedback.setText(text);
    feedback.setColor(good ? GO : STOP);
  }

  private static TextButton button(String key, String style, Runnable action) {
    TextButton button = Scene2dElementFactory.createButton(TRANSLATION.text(key), style, 20);
    button.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            action.run();
          }
        });
    return button;
  }

  private static String termId(int term) {
    return "term" + term;
  }

  private static String binId(int bin) {
    return "bin" + bin;
  }
}
