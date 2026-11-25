package rooms.morphco.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import engine.Game;
import feature.canvas.CanvasNode;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.HeadlessDialogGroup;
import feature.interaction.sorting.SortBoard;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import rooms.morphco.puzzles.CircumfixPuzzle;
import rooms.morphco.util.MorphCoText;

/**
 * The circumfix assembly station: a left bowl, a right bowl and a larger bowl in the middle, on a
 * workbench.
 *
 * <p>The tray shows the circumfix parts that the connected players carry. Assembling sends {@code
 * LEFT|RIGHT} to the server, which removes the parts from the inventories. The station stays open:
 * a fitting pair leaves the tray and the circumfix appears in the middle bowl. Task and controls
 * are in the help popup, so the tray and the middle bowl carry no labels and the middle bowl has no
 * name plate.
 */
final class StationUI extends MorphCoConsoleUI {

  private static final String LEFT = "left";
  private static final String RIGHT = "right";
  private static final float SIDE_HEIGHT = MorphCoSortBoard.HEADER + 140;
  private static final float MIDDLE_HEIGHT = MorphCoSortBoard.HEADER + 210;

  private final SortBoard board =
      new MorphCoSortBoard(area(), MorphCoSortBoard.Look.BOWL, this::changed);
  private final PanelNode tray = area().addNode(new PanelNode("tray", PanelNode.Look.BELT, 10));
  private final PanelNode bench = area().addNode(new PanelNode("bench", PanelNode.Look.BENCH, 0));
  private final Map<String, SortBoard.Slot> slots = new LinkedHashMap<>();
  private final SortBoard.Bin left;
  private final SortBoard.Bin right;
  private final MiddleBowl middle = area().addNode(new MiddleBowl());
  private final Set<CircumfixPuzzle.Circumfix> assembled =
      EnumSet.noneOf(CircumfixPuzzle.Circumfix.class);

  private StationUI(DialogContext context) {
    super(
        context,
        MorphCoText.text("ui.station.title"),
        "",
        MorphCoText.text("ui.station.instruction") + "\n\n" + MorphCoText.text("ui.station.help"));
    bench.z(-1);
    String[] fragments =
        context.find(MorphCoDialogs.FRAGMENTS, String[].class).orElse(new String[0]);
    for (int index = 0; index < fragments.length; index++) {
      String id = fragments[index] + "#" + index;
      CircumfixPuzzle.Fragment.parse(fragments[index])
          .ifPresent(fragment -> slots.put(id, board.addItem(id, fragment.label(), 21)));
    }

    left = board.addBin(LEFT, MorphCoText.text("ui.station.left"), 1, SIDE_HEIGHT);
    right = board.addBin(RIGHT, MorphCoText.text("ui.station.right"), 1, SIDE_HEIGHT);
    String[] done = context.find(MorphCoDialogs.ASSEMBLED, String[].class).orElse(new String[0]);
    for (String name : done) assembled.add(CircumfixPuzzle.Circumfix.valueOf(name));
    middle.refresh();

    button("ui.station.assemble", true, this::assemble);
  }

  /**
   * Builds the dialog or a headless placeholder on the server.
   *
   * @param context dialog context
   * @return dialog group
   */
  static Group build(DialogContext context) {
    if (Game.isHeadless()) return new HeadlessDialogGroup();
    return new StationUI(context);
  }

  @Override
  protected float arrange(float width, float height) {
    float top = -MorphCoSortBoard.belt(tray, List.copyOf(slots.values()), 0, width) - 24;
    // The bowls stand side by side on the bench, the one in the middle is larger.
    float side = Math.min(280, width * .24f);
    float large = Math.min(440, width * .36f);
    float gap = (width - 2 * side - large) / 4;
    float benchHeight = MIDDLE_HEIGHT + 40;
    float floor = top - MIDDLE_HEIGHT - 16;
    left.size(side, SIDE_HEIGHT).position(gap, floor);
    middle.size(large, MIDDLE_HEIGHT).position(2 * gap + side, floor);
    right.size(side, SIDE_HEIGHT).position(3 * gap + side + large, floor);
    bench.size(width, benchHeight).position(0, top - benchHeight);
    board.arrange();
    return -top + benchHeight;
  }

  private static Optional<String> inBowl(Map<String, String> placement, String bowl) {
    return placement.entrySet().stream()
        .filter(entry -> entry.getValue().equals(bowl))
        .map(Map.Entry::getKey)
        .findFirst();
  }

  private static CircumfixPuzzle.Fragment fragment(String item) {
    return CircumfixPuzzle.Fragment.valueOf(item.split("#")[0]);
  }

  private void assemble() {
    Map<String, String> placement = board.placement();
    Optional<String> leftItem = inBowl(placement, LEFT);
    Optional<String> rightItem = inBowl(placement, RIGHT);
    if (leftItem.isEmpty() || rightItem.isEmpty()) {
      feedback(MorphCoText.text("world.a12.station-missing"), false);
      return;
    }
    CircumfixPuzzle.Fragment leftPart = fragment(leftItem.get());
    CircumfixPuzzle.Fragment rightPart = fragment(rightItem.get());
    submit(leftPart.name() + "|" + rightPart.name());
    Optional<CircumfixPuzzle.Circumfix> circumfix = CircumfixPuzzle.combine(leftPart, rightPart);
    if (circumfix.isEmpty()) {
      feedback(MorphCoText.text("world.a12.station-wrong"), false);
      return;
    }
    for (String item : List.of(leftItem.get(), rightItem.get())) {
      board.removeItems(item);
      slots.remove(item);
    }
    assembled.add(circumfix.get());
    middle.refresh();
    changed();
    String text =
        MorphCoText.text(
            "world.a12.station-assembled", circumfix.get().label(), circumfix.get().example());
    if (assembled.size() == CircumfixPuzzle.Circumfix.values().length) {
      text += " " + MorphCoText.text("world.a12.station-complete");
    }
    feedback(text, true);
  }

  /** The larger bowl in the middle; the assembled circumfixes lie in its hollow. */
  private final class MiddleBowl extends CanvasNode {
    private final Table pieces = new Table();

    private MiddleBowl() {
      super("middle", 300, MIDDLE_HEIGHT);
      movable(false);
      deletable(false);
      selectable(false);
      setTouchable(Touchable.disabled);
    }

    void refresh() {
      pieces.clearChildren();
      pieces.defaults().padBottom(6);
      if (assembled.isEmpty()) {
        Label empty = MorphCoUI.label(MorphCoText.text("ui.station.empty"), 18, MorphCoUI.TEXT);
        empty.setWrap(false);
        pieces.add(empty).row();
      }
      for (CircumfixPuzzle.Circumfix circumfix : assembled) {
        pieces.add(MorphCoUI.tag(circumfix.label() + " (" + circumfix.example() + ")")).row();
      }
      invalidateLayout();
    }

    @Override
    protected void buildContent() {
      addActor(pieces);
    }

    @Override
    protected void layoutContent() {
      float bowl = height() - MorphCoSortBoard.HEADER;
      Vector2 hollow = MorphCoUI.hollow(0, 0, width(), bowl);
      pieces.pack();
      pieces.setPosition(hollow.x - pieces.getWidth() / 2, hollow.y - pieces.getHeight() / 2 + 3);
    }

    @Override
    protected void drawBackground(Batch batch, float alpha) {
      MorphCoUI.bowl(batch, alpha, x(), y(), width(), height() - MorphCoSortBoard.HEADER, null);
    }
  }
}
