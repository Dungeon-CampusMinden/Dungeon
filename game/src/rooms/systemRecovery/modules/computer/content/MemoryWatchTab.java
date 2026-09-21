package rooms.systemRecovery.modules.computer.content;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import engine.utils.Scene2dElementFactory;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import rooms.systemRecovery.modules.computer.SystemRecoveryComputerTab;
import rooms.systemRecovery.util.SystemRecoveryMemoryWatch;
import rooms.systemRecovery.util.SystemRecoveryText;

/** Displays the array identifiers and data types found in accepted player code. */
public final class MemoryWatchTab extends SystemRecoveryComputerTab {

  /** Stable key used by the computer tab bar. */
  public static final String KEY = "memory-watch";

  private final Map<String, String> arrayTypes = new LinkedHashMap<>();
  private Table entries;
  private Label status;

  /**
   * Creates a Memory Watch view from the server-provided array entries.
   *
   * @param arrayEntries accepted name/type entries, or an empty array
   */
  public MemoryWatchTab(String[] arrayEntries) {
    super(KEY, SystemRecoveryText.text("computer.memory-watch-tab"));
    if (arrayEntries != null) {
      Arrays.stream(arrayEntries)
          .map(SystemRecoveryMemoryWatch::parseEntry)
          .filter(entry -> !entry.name().isBlank())
          .forEach(this::mergeEntry);
    }
    createActors();
  }

  @Override
  protected void createActors() {
    Table layout = new Table(skin);
    layout.top().left().defaults().growX();

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
    ScrollPane entryScroll =
        Scene2dElementFactory.createScrollPane(entries, false, true);
    entryScroll.setOverscroll(false, false);
    entryScroll.setFadeScrollBars(false);
    layout.add(entryScroll).grow().left().padTop(12).row();
    renderEntries();

    add(layout).grow();
  }

  /**
   * Merges names and types from a server-confirmed terminal source into the visible list.
   *
   * @param source accepted source whose array names and types should be displayed
   */
  public void mergeAcceptedSource(String source) {
    Arrays.stream(SystemRecoveryMemoryWatch.extractArrayEntries(source))
        .map(SystemRecoveryMemoryWatch::parseEntry)
        .filter(entry -> !entry.name().isBlank())
        .forEach(this::mergeEntry);
    renderEntries();
  }

  private void mergeEntry(SystemRecoveryMemoryWatch.ArrayEntry entry) {
    arrayTypes.merge(
        entry.name(),
        entry.type(),
        (knownType, incomingType) -> "?[]".equals(incomingType) ? knownType : incomingType);
  }

  private void renderEntries() {
    if (entries == null) return;
    entries.clearChildren();
    status.setText(
        SystemRecoveryText.text("computer.memory-watch-status", arrayTypes.size()));
    if (arrayTypes.isEmpty()) {
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

    int index = 0;
    for (Map.Entry<String, String> array : arrayTypes.entrySet()) {
      Table row = new Table(skin);
      row.setBackground(index % 2 == 0 ? "generic-area" : "generic-area-depth");
      row
          .add(createLabel(String.format("%02d", index + 1), 18))
          .width(52)
          .left()
          .pad(10, 14, 10, 8);
      row.add(createLabel("[]", 24)).width(54).left().pad(10, 0, 10, 8);
      row
          .add(createLabel(array.getKey(), 20))
          .growX()
          .left()
          .pad(10, 0, 10, 14);
      row
          .add(createLabel(array.getValue(), 18))
          .width(100)
          .right()
          .pad(10, 14, 10, 0);
      entries.add(row).growX().left().padBottom(5).row();
      index++;
    }
  }
}
