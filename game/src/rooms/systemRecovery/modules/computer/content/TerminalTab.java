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
import engine.network.messages.s2c.DialogFeedbackMessage;
import engine.utils.FontHelper;
import engine.utils.FontSpec;
import engine.utils.Scene2dElementFactory;
import feature.hud.dialogs.DialogCallbackResolver;
import feature.hud.dialogs.DialogFeedbackFingerprint;
import rooms.systemRecovery.SystemRecovery;
import rooms.systemRecovery.modules.computer.SystemRecoveryComputerCallbacks;
import rooms.systemRecovery.modules.computer.SystemRecoveryComputerTab;
import rooms.systemRecovery.modules.interpreter.TerminalInterpreter;
import rooms.systemRecovery.util.SystemRecoveryText;
import rooms.systemRecovery.util.interpreter.TerminalInterpreterSetup;

/** Terminal/editor tab for recovery code input. */
public class TerminalTab extends SystemRecoveryComputerTab {

  public static final String KEY = "terminal";
  private static final int VISIBLE_LINE_COUNT = 14;
  private static final Color SUCCESS_COLOR = new Color(0.12f, 0.65f, 0.25f, 1f);
  private static final Color FAILURE_COLOR = new Color(0.85f, 0.12f, 0.12f, 1f);
  private static String savedCode = "";

  private TextArea codeEditor;
  private Label lineNumbers;
  private Label feedbackLabel;
  private Table feedbackBar;
  private String lastSubmittedFingerprint;
  private int displayedFirstLine = -1;
  private int displayedLineCount = -1;

  /** Creates the terminal tab. */
  public TerminalTab() {
    super(KEY, SystemRecoveryText.text("computer.terminal"));
    createActors();
  }

  @Override
  protected void createActors() {
    Table layout = new Table(skin);
    layout.top();
    layout.defaults().growX();

    Table editorPanel = new Table(skin);
    editorPanel.setBackground("generic-area");
    editorPanel.pad(12);
    editorPanel.top();

    lineNumbers = createLabel("", FontSpec.of(Scene2dElementFactory.FONT_PATH, 24, LABEL_COLOR));
    lineNumbers.setAlignment(com.badlogic.gdx.utils.Align.topRight);

    TextField styledField = Scene2dElementFactory.createTextField(savedCode);
    codeEditor = new TextArea(savedCode, new TextField.TextFieldStyle(styledField.getStyle()));
    Label.LabelStyle lineNumberStyle = lineNumbers.getStyle();
    lineNumberStyle.font = codeEditor.getStyle().font;
    lineNumberStyle.fontColor = LABEL_COLOR;
    lineNumbers.setStyle(lineNumberStyle);
    codeEditor.setPrefRows(VISIBLE_LINE_COUNT);
    codeEditor.setFocusTraversal(false);
    Scene2dElementFactory.addTextFieldChangeListener(
        codeEditor,
        text -> {
          savedCode = text;
          lastSubmittedFingerprint = null;
          updateLineNumbers();
        });

    float editorTextTopInset =
        codeEditor.getStyle().background == null
            ? 0f
            : codeEditor.getStyle().background.getTopHeight();
    editorPanel
        .add(lineNumbers)
        .width(64)
        .growY()
        .top()
        .right()
        .padTop(editorTextTopInset)
        .padRight(12);
    editorPanel.add(codeEditor).grow();
    layout.add(editorPanel).grow().row();

    feedbackLabel = createLabel("", 18);
    feedbackLabel.setWrap(true);

    Table footer = new Table(skin);
    Table feedbackPanel = new Table(skin);
    feedbackBar = new Table(skin);
    feedbackBar.setBackground("generic-area-depth");
    feedbackPanel.add(feedbackBar).width(8).growY().padRight(10);
    feedbackPanel.add(feedbackLabel).growX().left();
    footer.add(feedbackPanel).growX().left().padRight(20);

    Table buttons = new Table(skin);
    buttons.right();
    TextButton sendButton = createButton(SystemRecoveryText.text("computer.send"), "green", 24);
    TextButton deleteButton =
        createButton(SystemRecoveryText.text("computer.delete"), "red-outline", 24);
    TextButton nextStepButton =
        createButton(SystemRecoveryText.text("computer.next-step"), "blue-outline", 24);
    TextButton petriNetButton =
        createButton(SystemRecoveryText.text("computer.petri-net"), "blue-outline", 24);
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
    nextStepButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            fillDebugSource();
          }
        });
    petriNetButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            DialogCallbackResolver.createButtonCallback(
                    context().dialogId(), SystemRecoveryComputerCallbacks.DEBUG_PETRI_NET)
                .accept(new DialogResponseMessage.StringValue(""));
          }
        });
    buttons.add(sendButton).width(150).height(52).padRight(12);
    buttons.add(deleteButton).width(150).height(52);
    if (SystemRecovery.debugMode()) {
      buttons.add(nextStepButton).width(180).height(52).padLeft(12);
      buttons.add(petriNetButton).width(180).height(52).padLeft(12);
    }
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
    lastSubmittedFingerprint = null;
    showFeedback("");
    updateLineNumbers();
    if (codeEditor.getStage() != null) {
      codeEditor.getStage().setKeyboardFocus(codeEditor);
    }
  }

  /** Replaces the editor content with valid example code without submitting it. */
  private void fillDebugSource() {
    TerminalInterpreterSetup.debugSourceForState(TerminalInterpreter.instance().currentState())
        .ifPresent(
            source -> {
              codeEditor.setText(source);
              codeEditor.setCursorPosition(0);
              savedCode = source;
              lastSubmittedFingerprint = null;
              showFeedback("");
              updateLineNumbers();
              if (codeEditor.getStage() != null) {
                codeEditor.getStage().setKeyboardFocus(codeEditor);
              }
            });
  }

  private void sendCode() {
    String source = codeText();
    lastSubmittedFingerprint = DialogFeedbackFingerprint.of(source);
    showFeedback(SystemRecoveryText.text("computer.feedback-submitting"), LABEL_COLOR, "generic-area-depth");
    DialogCallbackResolver.createButtonCallback(
            context().dialogId(), SystemRecoveryComputerCallbacks.TERMINAL_SEND)
        .accept(new DialogResponseMessage.StringValue(source));
  }

  /** Applies a server response to the inline terminal status area. */
  public void applyServerFeedback(DialogFeedbackMessage feedback) {
    if (!feedback.sourceFingerprint().isEmpty()
        && !feedback.sourceFingerprint().equals(lastSubmittedFingerprint)) {
      return;
    }
    showFeedback(
        SystemRecoveryText.text(feedback.messageKey()),
        feedback.successful() ? SUCCESS_COLOR : FAILURE_COLOR,
        feedback.successful() ? "green_square_depth_flat" : "red_square_flat");
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
    showFeedback(message, LABEL_COLOR, "generic-area-depth");
  }

  private void showFeedback(String message, Color color, String barBackground) {
    if (feedbackLabel != null) {
      feedbackLabel.getStyle().font =
          FontHelper.getFont(Scene2dElementFactory.FONT_PATH, 18, color, 0, color);
      feedbackLabel.setText(message);
    }
    if (feedbackBar != null) {
      feedbackBar.setBackground(barBackground);
    }
  }
}
