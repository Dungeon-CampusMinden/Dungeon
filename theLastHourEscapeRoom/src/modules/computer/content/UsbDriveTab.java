package modules.computer.content;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import core.utils.Cursors;
import core.utils.Scene2dElementFactory;
import modules.computer.ComputerDialog;
import modules.computer.ComputerStateComponent;

/**
 * Tab that represents the contents of the USB stick drive (mounted as F:) once the correct USB
 * stick has been plugged into the PC.
 *
 * <p>Shows a tree view of folders and files. Hidden among the bogus entries are two important
 * files, {@link #HINT_FILE} and {@link #CONTROL_PANEL_KEY_FILE}, which open additional tabs when
 * clicked.
 */
public class UsbDriveTab extends ComputerTab {

  /** Key for identifying the USB drive tab in the computer dialog. */
  public static final String KEY = "usb-f";

  // ----- Constants (text + layout values) -----

  /** Tab title. */
  private static final String TITLE = "F:/";

  /** Header text shown above the file tree. */
  private static final String HEADER_TEXT = "F:/  Removable Drive";

  /** Sub-header text. */
  private static final String SUB_HEADER_TEXT = "USB Storage Device  -  Mounted";

  /** Name of the hint file. */
  public static final String HINT_FILE = "hint.md";

  /** Name of the control panel key file. */
  public static final String CONTROL_PANEL_KEY_FILE = "control-panel.key";

  /** Indentation per tree level, in pixels. */
  private static final float INDENT = 22f;

  /** Vertical padding between tree rows. */
  private static final float ROW_PAD = 2f;

  /** Font size used for tree entries. */
  private static final int ENTRY_FONT_SIZE = 18;

  /** Font size used for the header. */
  private static final int HEADER_FONT_SIZE = 28;

  /** Font size used for the sub-header. */
  private static final int SUB_HEADER_FONT_SIZE = 14;

  /** Folder marker character. */
  private static final String FOLDER_PREFIX = "[+] ";

  /** File marker character. */
  private static final String FILE_PREFIX = " -  ";

  /** Color for folder names. */
  private static final Color FOLDER_COLOR = new Color(0.10f, 0.10f, 0.45f, 1f);

  /** Color for ordinary file names. */
  private static final Color FILE_COLOR = new Color(0.15f, 0.15f, 0.15f, 1f);

  /** Color for hover state on interactive entries. */
  private static final Color HOVER_COLOR = new Color(0.10f, 0.45f, 0.85f, 1f);

  /** Color for header text. */
  private static final Color HEADER_COLOR = new Color(0.10f, 0.10f, 0.45f, 1f);

  /** Color for sub-header text. */
  private static final Color SUB_HEADER_COLOR = Color.GRAY;

  // ----- Fake folder structure -----

  private static final String[] DOCUMENTS = {
    "vacation_2023.txt",
    "shopping_list.txt",
    "old_resume.docx",
    "todo.txt",
    HINT_FILE
  };

  private static final String[] PROJECTS = {
    "ideas.txt",
    "draft_proposal.docx",
    "budget.xlsx",
    "meeting_notes.md"
  };

  private static final String[] BACKUPS = {
    "backup_2024_q1.zip",
    "backup_2024_q2.zip",
    "config.bak"
  };

  private static final String[] SYSTEM = {
    "autorun.inf",
    "drivers.bin",
    "readme.txt",
    CONTROL_PANEL_KEY_FILE
  };

  /**
   * Creates a new UsbDriveTab.
   *
   * @param sharedState the shared computer state component
   */
  public UsbDriveTab(ComputerStateComponent sharedState) {
    super(sharedState, KEY, TITLE, false);
  }

  @Override
  protected void createActors() {
    Table content = new Table();
    content.top().left();
    content.pad(10f);

    Label header =
        Scene2dElementFactory.createLabel(HEADER_TEXT, HEADER_FONT_SIZE, HEADER_COLOR);
    header.setAlignment(Align.left);
    content.add(header).left().padBottom(2f).row();

    Label sub =
        Scene2dElementFactory.createLabel(SUB_HEADER_TEXT, SUB_HEADER_FONT_SIZE, SUB_HEADER_COLOR);
    sub.setAlignment(Align.left);
    content.add(sub).left().padBottom(10f).row();

    content.add(Scene2dElementFactory.createHorizontalDivider()).fillX().height(2f).padBottom(10f).row();

    addFolder(content, "Documents", DOCUMENTS);
    addFolder(content, "Projects", PROJECTS);
    addFolder(content, "Backups", BACKUPS);
    addFolder(content, "System", SYSTEM);

    ScrollPane scroll = Scene2dElementFactory.createScrollPane(content, false, true);
    scroll.setOverscroll(false, false);
    this.add(scroll).grow();
  }

  /** Adds a folder header row and all its children to the given table. */
  private void addFolder(Table table, String name, String[] children) {
    Label folder =
        Scene2dElementFactory.createLabel(FOLDER_PREFIX + name, ENTRY_FONT_SIZE, FOLDER_COLOR);
    folder.setAlignment(Align.left);
    table.add(folder).left().padTop(ROW_PAD).padBottom(ROW_PAD).row();
    for (String child : children) {
      addFile(table, child);
    }
  }

  /** Adds a single file row to the given table. Hooks up click handlers for special files. */
  private void addFile(Table table, String fileName) {
    Label file =
        Scene2dElementFactory.createLabel(FILE_PREFIX + fileName, ENTRY_FONT_SIZE, FILE_COLOR);
    file.setAlignment(Align.left);
    file.setTouchable(Touchable.enabled);
    file.setUserObject(Cursors.INTERACT);
    file.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            onFileClicked(fileName);
          }

          @Override
          public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
            super.enter(event, x, y, pointer, fromActor);
            file.setColor(HOVER_COLOR);
          }

          @Override
          public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
            super.exit(event, x, y, pointer, toActor);
            file.setColor(Color.WHITE);
          }
        });
    table.add(file).left().padLeft(INDENT).padTop(ROW_PAD).padBottom(ROW_PAD).row();
  }

  private void onFileClicked(String fileName) {
    ComputerDialog.getInstance()
        .ifPresent(
            dialog -> {
              if (HINT_FILE.equals(fileName)) {
                openOrFocus(dialog, "file-" + HINT_FILE, () -> new FileTab(sharedState(), HINT_FILE));
              } else if (CONTROL_PANEL_KEY_FILE.equals(fileName)) {
                localState().controlPanelOpen(true);
                openOrFocus(dialog, ControlPanelTab.KEY, () -> new ControlPanelTab(sharedState()));
              }
              // Other entries are bogus and silently do nothing.
            });
  }

  private static void openOrFocus(
      ComputerDialog dialog, String tabKey, java.util.function.Supplier<ComputerTab> factory) {
    if (!dialog.containsTab(tabKey)) {
      dialog.addTab(factory.get());
    }
    dialog.activateTab(tabKey);
  }

  @Override
  protected void updateState(ComputerStateComponent newStateComp) {
    // Static page; nothing to refresh.
  }
}

