package rooms.systemRecovery.modules.computer.content;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import engine.utils.Scene2dElementFactory;
import rooms.systemRecovery.modules.computer.SystemRecoveryComputerTab;
import rooms.systemRecovery.util.SystemRecoveryMemoryWatch;
import rooms.systemRecovery.util.SystemRecoveryText;

/** Displays the arrays and values derived from server-accepted player code. */
public final class MemoryWatchTab extends SystemRecoveryComputerTab {

  /** Stable key used by the computer tab bar. */
  public static final String KEY = "memory-watch";

  private final SystemRecoveryMemoryWatch memoryWatch = new SystemRecoveryMemoryWatch();
  private Table entries;
  private Label status;

  /**
   * Creates a Memory Watch view from the server-provided array snapshot.
   *
   * @param arrayEntries accepted name/type/value entries, or an empty array
   */
  public MemoryWatchTab(String[] arrayEntries) {
    super(KEY, SystemRecoveryText.text("computer.memory-watch-tab"));
    memoryWatch.restoreEntries(arrayEntries);
    createActors();
  }

  @Override
  protected void createActors() {
    Table layout = new Table(skin);
    layout.top().left().defaults().growX();
    layout.pad(0, 18, 14, 18);

    Table header = new Table(skin);
    header.setBackground("blue_square_flat");
    header
        .add(createLabel(SystemRecoveryText.text("computer.memory-watch-heading"), 24))
        .left()
        .pad(12, 16, 12, 16);
    layout.add(header).growX().height(58).left().row();

    Table statusPanel = new Table(skin);
    statusPanel.setBackground("generic-area-depth");
    status = createLabel("", 18);
    statusPanel.add(status).left().pad(10, 14, 10, 14);
    layout.add(statusPanel).growX().left().padTop(16).row();

    entries = new Table(skin);
    entries.top().left().defaults().growX().fillX();
    ScrollPane entryScroll = Scene2dElementFactory.createScrollPane(entries, false, true);
    entryScroll.setOverscroll(false, false);
    entryScroll.setFadeScrollBars(false);
    layout.add(entryScroll).grow().left().padTop(12).row();
    renderEntries();

    add(layout).grow();
  }

  /**
   * Applies a server-confirmed terminal source to the displayed memory snapshot.
   *
   * @param source accepted source whose array names and types should be displayed
   */
  public void mergeAcceptedSource(String source) {
    memoryWatch.recordAcceptedSource(source);
    renderEntries();
  }

  private void renderEntries() {
    if (entries == null) return;
    entries.clearChildren();
    String[] arrays = memoryWatch.arrayEntries();
    status.setText(SystemRecoveryText.text("computer.memory-watch-status", arrays.length));
    if (arrays.length == 0) {
      Table emptyState = new Table(skin);
      emptyState.setBackground("generic-area");
      emptyState
          .add(createLabel(SystemRecoveryText.text("computer.memory-watch-empty"), 20))
          .growX()
          .left()
          .pad(20);
      entries.add(emptyState).growX().left().padTop(4).row();
      return;
    }

    for (String encoded : arrays) {
      SystemRecoveryMemoryWatch.ArrayEntry array = SystemRecoveryMemoryWatch.parseEntry(encoded);
      Table row = new Table(skin);
      row.setBackground("generic-area");
      row.defaults().left();
      row.add(createLabel(array.name(), 22)).growX().left().pad(10, 14, 4, 14);
      row.add(createLabel(array.type(), 18)).right().pad(10, 14, 4, 0).row();
      Label contents = createLabel(array.contents(), 20);
      contents.setWrap(true);
      row.add(contents).growX().left().colspan(2).pad(2, 14, 12, 14);
      entries.add(row).growX().left().padBottom(9).row();
    }
  }
}
