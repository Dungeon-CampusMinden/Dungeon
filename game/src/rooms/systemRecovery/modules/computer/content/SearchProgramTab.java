package rooms.systemRecovery.modules.computer.content;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.network.messages.s2c.DialogFeedbackMessage;
import engine.utils.Scene2dElementFactory;
import feature.hud.dialogs.DialogCallbackResolver;
import java.util.List;
import rooms.systemRecovery.SystemRecovery;
import rooms.systemRecovery.modules.computer.SystemRecoveryComputerCallbacks;
import rooms.systemRecovery.modules.computer.SystemRecoveryComputerTab;
import rooms.systemRecovery.modules.computer.UsbProgramDraft;
import rooms.systemRecovery.util.SystemRecoveryText;

/** Fill-in editor shown while an empty locator chip is inserted into the computer. */
public final class SearchProgramTab extends SystemRecoveryComputerTab {

  /** Stable tab key used by server feedback routing. */
  public static final String KEY = "search-program";

  private final TextField innerBound;
  private ProgramWriteStatus writeStatus;

  /**
   * Creates the search-program editor with values restored from the inserted chip.
   *
   * @param savedDraft encoded field values restored from the chip
   */
  public SearchProgramTab(String savedDraft) {
    super(KEY, SystemRecoveryText.text("computer.search-tab"));
    List<String> fields = UsbProgramDraft.decode(savedDraft, 3);
    innerBound = createCodeField(fields.get(0));
    createActors();
  }

  @Override
  protected void createActors() {
    Table layout = new Table(skin);
    layout.top().defaults().growX();
    layout.add(createLabel(SystemRecoveryText.text("computer.search-heading"), 24)).left().row();

    Table code = new Table(skin);
    code.setBackground("generic-area");
    code.top().left().pad(12);
    addFixedLine(code, "for (int row = 0; row < map.length; row++) {");
    addFillLine(code, "    for (int column = 0; column < ", innerBound, "; column++) {");
    addFixedLine(code, "        if (map[row][column] == 1) {");
    addFixedLine(code, "            roboter.collect();");
    addFixedLine(code, "        }");
    addFixedLine(code, "    }");
    addFixedLine(code, "}");
    layout.add(code).growX().padTop(12).row();

    writeStatus = new ProgramWriteStatus();
    layout.add(writeStatus).growX().left().padTop(10).row();

    TextButton upload = createButton(SystemRecoveryText.text("computer.save-search"), "green", 24);
    upload.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            sendUpload(source());
          }
        });
    TextButton eject = createButton(SystemRecoveryText.text("computer.eject-stick"), "blue-outline", 24);
    eject.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            String draft = draft();
            writeStatus.begin(draft, "computer.eject-in-progress", false);
            DialogCallbackResolver.createButtonCallback(
                    context().dialogId(), SystemRecoveryComputerCallbacks.SEARCH_PROGRAM_EJECT)
                .accept(new DialogResponseMessage.StringValue(draft));
          }
        });

    Table actions = new Table(skin);
    actions.right();
    if (SystemRecovery.debugMode()) {
      TextButton solve = createButton(SystemRecoveryText.text("computer.solve"), "blue-outline", 24);
      solve.addListener(
          new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
              innerBound.setText("map[row].length");
              sendUpload(source());
            }
          });
      actions.add(solve).width(180).height(52).padRight(8);
    }
    actions.add(eject).width(165).height(52).padRight(8);
    actions.add(upload).width(240).height(52);
    layout.add(actions).right().padTop(12);
    add(layout).grow();
  }

  private TextField createCodeField(String value) {
    TextField styleSource = Scene2dElementFactory.createTextField("");
    TextField field = new TextField(value, new TextField.TextFieldStyle(styleSource.getStyle()));
    field.setMaxLength(256);
    return field;
  }

  private static void addFixedLine(Table code, String line) {
    code.add(lineLabel(line)).left().height(32).row();
  }

  private void addFillLine(Table code, String before, TextField field, String after) {
    Table line = new Table(skin);
    line.add(lineLabel(before)).left();
    line.add(field).width(340).height(34).left();
    line.add(lineLabel(after)).left();
    code.add(line).left().height(34).row();
  }

  private static com.badlogic.gdx.scenes.scene2d.ui.Label lineLabel(String text) {
    return Scene2dElementFactory.createLabel(text, 20, LABEL_COLOR);
  }

  private String source() {
    return buildSource(innerBound.getText());
  }

  static String buildSource(String innerBound) {
    return "for (int row = 0; row < map.length; row++) {\n"
        + "    for (int column = 0; column < "
        + innerBound
        + "; column++) {\n"
        + "        if (map[row][column] == 1) {\n"
        + "            roboter.collect();\n"
        + "        }\n"
        + "    }\n"
        + "}";
  }

  private String draft() {
    return UsbProgramDraft.encode(
        List.of(innerBound.getText(), "map[row][column] == 1", "roboter.collect()"));
  }

  private void sendUpload(String source) {
    writeStatus.begin(source);
    DialogCallbackResolver.createButtonCallback(
            context().dialogId(), SystemRecoveryComputerCallbacks.SEARCH_PROGRAM_SAVE)
        .accept(new DialogResponseMessage.StringValue(source));
  }

  /**
   * Applies the authoritative write result without opening a modal popup.
   *
   * @param serverFeedback authoritative write or eject result
   * @param onSuccess action to run after a successful result
   */
  public void applyServerFeedback(DialogFeedbackMessage serverFeedback, Runnable onSuccess) {
    writeStatus.apply(serverFeedback, onSuccess);
  }
}
