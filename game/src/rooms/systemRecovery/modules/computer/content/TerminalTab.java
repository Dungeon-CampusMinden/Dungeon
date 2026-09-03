package rooms.systemRecovery.modules.computer.content;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.utils.FontSpec;
import engine.utils.Scene2dElementFactory;
import feature.hud.dialogs.DialogCallbackResolver;
import rooms.systemRecovery.modules.computer.SystemRecoveryComputerCallbacks;
import rooms.systemRecovery.modules.computer.SystemRecoveryComputerTab;
import rooms.systemRecovery.modules.interpreter.TerminalInterpreter;

/** Terminal/editor tab for recovery code input. */
public class TerminalTab extends SystemRecoveryComputerTab {

  public static final String KEY = "terminal";
  private static final int VISIBLE_LINE_COUNT = 14;
  private static String savedCode = "";
  private static String savedFeedback = "";

  private TextArea codeEditor;
  private Label lineNumbers;
  private Label feedbackLabel;
  private int displayedFirstLine = -1;
  private int displayedLineCount = -1;

  /** Creates the terminal tab. */
  public TerminalTab() {
    super(KEY, "Terminal");
    createActors();
  }

  @Override
  protected void createActors() {
    Table layout = new Table(skin);
    layout.top();
    layout.defaults().growX();

    Table editor = new Table(skin);
    editor.setBackground("generic-area");
    editor.pad(12);
    editor.top();

    lineNumbers =
        Scene2dElementFactory.createLabel(
            "",
            FontSpec.of(Scene2dElementFactory.FONT_PATH, 24, new Color(0.55f, 0.6f, 0.64f, 1f)));
    lineNumbers.setAlignment(com.badlogic.gdx.utils.Align.topRight);

    TextField styledField = Scene2dElementFactory.createTextField(savedCode);
    codeEditor = new TextArea(savedCode, new TextField.TextFieldStyle(styledField.getStyle()));
    codeEditor.setPrefRows(VISIBLE_LINE_COUNT);
    codeEditor.setFocusTraversal(false);
    Scene2dElementFactory.addTextFieldChangeListener(
        codeEditor,
        text -> {
          savedCode = text;
          updateLineNumbers();
        });

    editor.add(lineNumbers).width(48).growY().top().right().padRight(14);
    editor.add(codeEditor).grow();
    layout.add(editor).grow().row();

    feedbackLabel = Scene2dElementFactory.createLabel(savedFeedback, 18, Color.WHITE);
    feedbackLabel.setWrap(true);

    Table footer = new Table(skin);
    footer.add(feedbackLabel).growX().left().padRight(20);

    Table buttons = new Table(skin);
    buttons.right();
    TextButton sendButton = Scene2dElementFactory.createButton("Send", "green", 24);
    TextButton deleteButton = Scene2dElementFactory.createButton("Delete", "red-outline", 24);
    sendButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            sendCode();
          }
        });
    deleteButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            clearCodeLines();
          }
        });
    buttons.add(sendButton).width(150).height(52).padRight(12);
    buttons.add(deleteButton).width(150).height(52);
    footer.add(buttons).right();
    layout.add(footer).growX().height(68).padTop(12);

    add(layout).grow();
    updateLineNumbers();
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    updateLineNumbers();
  }

  private void clearCodeLines() {
    codeEditor.setText("");
    savedCode = "";
    showFeedback("");
    updateLineNumbers();
    if (codeEditor.getStage() != null) {
      codeEditor.getStage().setKeyboardFocus(codeEditor);
    }
  }

  private void sendCode() {
    String source = codeText();
    boolean successful = TerminalInterpreter.instance().analyze(source);
    showFeedback(successful ? "Code interpreted." : "Cannot interpret current state.");
    DialogCallbackResolver.createButtonCallback(
            context().dialogId(), SystemRecoveryComputerCallbacks.TERMINAL_SEND)
        .accept(new DialogResponseMessage.StringValue(source));
  }

  private String codeText() {
    return codeEditor.getText();
  }

  private void updateLineNumbers() {
    if (codeEditor == null || lineNumbers == null) {
      return;
    }
    int firstLine = codeEditor.getFirstLineShowing();
    int lineCount = Math.max(VISIBLE_LINE_COUNT, codeEditor.getLinesShowing());
    if (firstLine == displayedFirstLine && lineCount == displayedLineCount) {
      return;
    }

    StringBuilder numbers = new StringBuilder();
    for (int line = 0; line < lineCount; line++) {
      if (line > 0) {
        numbers.append('\n');
      }
      numbers.append(firstLine + line + 1);
    }
    lineNumbers.setText(numbers);
    displayedFirstLine = firstLine;
    displayedLineCount = lineCount;
  }

  private void showFeedback(String message) {
    savedFeedback = message;
    if (feedbackLabel != null) {
      feedbackLabel.setText(message);
    }
  }
}
