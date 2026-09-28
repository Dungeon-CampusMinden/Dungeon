package rooms.systemRecovery.modules.computer.content;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import rooms.systemRecovery.SystemRecovery;
import rooms.systemRecovery.modules.computer.SystemRecoveryComputerCallbacks;
import rooms.systemRecovery.modules.computer.SystemRecoveryComputerTab;
import rooms.systemRecovery.modules.interpreter.TerminalInterpreter;
import rooms.systemRecovery.util.SystemRecoveryText;
import rooms.systemRecovery.util.interpreter.TerminalInterpreterSetup;

/** Terminal/editor tab for recovery code input and accepted-source history. */
public class TerminalTab extends SystemRecoveryComputerTab {

  public static final String KEY = "terminal";
  private static final int VISIBLE_LINE_COUNT = 14;
  private static final Color SUCCESS_COLOR = new Color(0.12f, 0.65f, 0.25f, 1f);
  private static final Color FAILURE_COLOR = new Color(0.85f, 0.12f, 0.12f, 1f);
  private static String savedCode = "";

  private final List<String> acceptedSources = new ArrayList<>();
  private TextArea codeEditor;
  private Label lineNumbers;
  private Label feedbackLabel;
  private Table feedbackBar;
  private Table inputView;
  private Table historyView;
  private Table historyEntries;
  private ScrollPane historyScroll;
  private String lastSubmittedFingerprint;
  private int displayedFirstLine = -1;
  private int displayedLineCount = -1;

  /** Creates the terminal tab without prior accepted code. */
  public TerminalTab() {
    this(new String[0]);
  }

  /**
   * Creates the terminal tab with the server-confirmed code history for this run.
   *
   * @param previousSources previously accepted terminal sources
   */
  public TerminalTab(String[] previousSources) {
    super(KEY, SystemRecoveryText.text("computer.terminal"));
    if (previousSources != null) {
      for (String source : previousSources) addHistoryEntry(source);
    }
    createActors();
  }

  @Override
  protected void createActors() {
    Table layout = new Table(skin);
    layout.top();
    layout.defaults().growX();

    inputView = createInputView();
    historyView = createHistoryView();
    Table split = new Table(skin);
    split.add(inputView).grow().uniformX().padRight(8);
    split.add(historyView).grow().uniformX();
    layout.add(split).grow();

    add(layout).grow();
    updateLineNumbers();
    renderHistory();
  }

  private Table createInputView() {
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

    feedbackLabel = createLabel("", 16);
    feedbackLabel.setWrap(true);

    Table feedbackPanel = new Table(skin);
    feedbackBar = new Table(skin);
    feedbackBar.setBackground("generic-area-depth");
    feedbackPanel.add(feedbackBar).width(8).height(28).padRight(8);
    feedbackPanel.add(feedbackLabel).growX().left();
    layout.add(feedbackPanel).growX().height(34).padTop(8).row();

    Table buttons = new Table(skin);
    buttons.right();
    TextButton sendButton = createButton(SystemRecoveryText.text("computer.send"), "green", 20);
    TextButton deleteButton =
        createButton(SystemRecoveryText.text("computer.delete"), "red-outline", 20);
    TextButton nextStepButton =
        createButton(SystemRecoveryText.text("computer.next-step"), "blue-outline", 18);
    TextButton petriNetButton =
        createButton(SystemRecoveryText.text("computer.petri-net"), "blue-outline", 18);
    TextButton getUsbButton =
        createButton(SystemRecoveryText.text("computer.get-usb"), "blue-outline", 18);
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
    getUsbButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            DialogCallbackResolver.createButtonCallback(
                    context().dialogId(), SystemRecoveryComputerCallbacks.DEBUG_GIVE_USB)
                .accept(new DialogResponseMessage.StringValue(""));
          }
        });
    buttons.add(sendButton).width(145).height(46).padRight(8);
    buttons.add(deleteButton).width(145).height(46);
    layout.add(buttons).growX().right().padTop(8).row();
    if (SystemRecovery.debugMode()) {
      Table debugButtons = new Table(skin);
      debugButtons.right();
      debugButtons.add(nextStepButton).width(150).height(42).padRight(8);
      debugButtons.add(petriNetButton).width(145).height(42).padRight(8);
      debugButtons.add(getUsbButton).width(125).height(42);
      layout.add(debugButtons).growX().right().padTop(6);
    }
    return layout;
  }

  private Table createHistoryView() {
    Table layout = new Table(skin);
    layout.top().left().defaults().growX();
    layout.setBackground("generic-area-depth");
    layout.pad(12);
    layout
        .add(createLabel(SystemRecoveryText.text("computer.terminal-history-heading"), 20))
        .left()
        .padBottom(10)
        .row();

    historyEntries = new Table(skin);
    historyEntries.top().left().defaults().growX().fillX();
    historyScroll = Scene2dElementFactory.createScrollPane(historyEntries, false, true);
    historyScroll.setOverscroll(false, false);
    historyScroll.setFadeScrollBars(false);
    layout.add(historyScroll).grow().left();
    return layout;
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
    showFeedback(
        SystemRecoveryText.text("computer.feedback-submitting"), LABEL_COLOR, "generic-area-depth");
    DialogCallbackResolver.createButtonCallback(
            context().dialogId(), SystemRecoveryComputerCallbacks.TERMINAL_SEND)
        .accept(new DialogResponseMessage.StringValue(source));
  }

  /**
   * Applies a server response to the inline terminal status area.
   *
   * @param feedback authoritative terminal result
   */
  public void applyServerFeedback(DialogFeedbackMessage feedback) {
    if (!feedback.sourceFingerprint().isEmpty()
        && !feedback.sourceFingerprint().equals(lastSubmittedFingerprint)) {
      return;
    }
    if (feedback.successful()) {
      addHistoryEntry(codeText());
      codeEditor.setText("");
      savedCode = "";
      lastSubmittedFingerprint = null;
      updateLineNumbers();
    }
    showFeedback(
        SystemRecoveryText.text(feedback.messageKey()),
        feedback.successful() ? SUCCESS_COLOR : FAILURE_COLOR,
        feedback.successful() ? "green_square_depth_flat" : "red_square_flat");
  }

  /**
   * Returns the source currently shown in the editor when it matches a server response.
   *
   * @param feedback server response to correlate
   * @return the submitted source, or empty for a stale response
   */
  public Optional<String> sourceForFeedback(DialogFeedbackMessage feedback) {
    if (feedback == null || !feedback.successful()) return Optional.empty();
    if (!feedback.sourceFingerprint().isEmpty()
        && !feedback.sourceFingerprint().equals(lastSubmittedFingerprint)) {
      return Optional.empty();
    }
    return Optional.of(codeText());
  }

  private void addHistoryEntry(String source) {
    if (source == null || source.isBlank()) return;
    if (!acceptedSources.isEmpty() && acceptedSources.get(acceptedSources.size() - 1).equals(source)) {
      return;
    }
    acceptedSources.add(source);
    renderHistory();
  }

  private void renderHistory() {
    if (historyEntries == null) return;
    historyEntries.clearChildren();
    if (acceptedSources.isEmpty()) {
      Table emptyState = new Table(skin);
      emptyState.setBackground("generic-area");
      emptyState
          .add(createLabel(SystemRecoveryText.text("computer.terminal-history-empty"), 20))
          .growX()
          .left()
          .pad(18);
      historyEntries.add(emptyState).growX().left().row();
      return;
    }

    for (int index = 0; index < acceptedSources.size(); index++) {
      Table entry = new Table(skin);
      entry.setBackground(index % 2 == 0 ? "generic-area" : "generic-area-depth");
      entry.top().left().defaults().growX();
      entry
          .add(createLabel(SystemRecoveryText.text("computer.terminal-history-entry", index + 1), 18))
          .left()
          .pad(10, 14, 4, 14)
          .row();
      Label source =
          createLabel(
              acceptedSources.get(index),
              FontSpec.of(Scene2dElementFactory.FONT_PATH, 18, LABEL_COLOR));
      source.setWrap(true);
      entry.add(source).growX().left().pad(4, 14, 12, 14);
      historyEntries.add(entry).growX().left().padBottom(8).row();
    }
    historyScroll.layout();
    historyScroll.setScrollY(historyScroll.getMaxY());
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
