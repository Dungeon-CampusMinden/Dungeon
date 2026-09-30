package rooms.systemRecovery.modules.computer.content;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.network.messages.s2c.DialogFeedbackMessage;
import engine.utils.FontHelper;
import engine.utils.Scene2dElementFactory;
import feature.hud.dialogs.DialogCallbackResolver;
import feature.hud.dialogs.DialogFeedbackFingerprint;
import java.util.Arrays;
import java.util.stream.Collectors;
import rooms.systemRecovery.modules.computer.SystemRecoveryComputerCallbacks;
import rooms.systemRecovery.modules.computer.SystemRecoveryComputerTab;
import rooms.systemRecovery.util.SystemRecoveryText;

/** Input mask for transferring the three results shown by the central system display. */
public final class SystemCoreMetaTab extends SystemRecoveryComputerTab {

  /** Stable tab key used by the computer dialog. */
  public static final String KEY = "system-core-meta";

  private final TextField[] energyFields = new TextField[SystemCoreMetaDraft.ENERGY_SLOT_COUNT];
  private TextField moduleField;
  private TextField batteryField;
  private Label feedback;
  private Table feedbackBar;
  private Table centeredContent;
  private Cell<Table> pageCell;
  private ScrollPane scroll;
  private String lastSubmittedFingerprint;
  private boolean requestPending;
  private TextButton submitButton;
  private TextButton clearButton;
  private Cell<TextButton> submitButtonCell;
  private Cell<TextButton> clearButtonCell;
  private static final Color SUCCESS_COLOR = new Color(0.12f, 0.65f, 0.25f, 1f);
  private static final Color FAILURE_COLOR = new Color(0.85f, 0.12f, 0.12f, 1f);
  private static final float MAX_CONTENT_WIDTH = 1_080f;
  private static final float MIN_CONTENT_WIDTH = 320f;

  /** Creates the final system-state input mask. */
  public SystemCoreMetaTab() {
    super(KEY, SystemRecoveryText.text("computer.meta-tab"));
    createActors();
  }

  @Override
  protected void createActors() {
    Table page = new Table(skin);
    page.top().left().defaults().growX();

    Table heading = new Table(skin);
    heading.setBackground("blue_square_flat");
    Label headingLabel = createLabel(SystemRecoveryText.text("computer.meta-heading"), 26);
    headingLabel.setWrap(true);
    heading.add(headingLabel).growX().left().pad(12, 18, 12, 18);
    page.add(heading).growX().row();

    Label instruction = createLabel(SystemRecoveryText.text("computer.meta-instruction"), 18);
    instruction.setWrap(true);
    Table instructionPanel = new Table(skin);
    instructionPanel.setBackground("generic-area-depth");
    instructionPanel.add(instruction).growX().left().pad(12, 16, 12, 16);
    page.add(instructionPanel).growX().padTop(12).row();

    Table energyTable = new Table(skin);
    energyTable.top().left().defaults().growX();
    for (int index = 0; index < SystemCoreMetaDraft.ENERGY_SLOT_COUNT; index++) {
      int slotIndex = index;
      energyFields[index] =
          createNumberField(
              SystemCoreMetaDraft.energyValue(index),
              value -> SystemCoreMetaDraft.energyValue(slotIndex, value));
      Table slot = new Table(skin);
      slot.add(createLabel(SystemRecoveryText.text("computer.meta-energy-slot", index + 1), 18))
          .left()
          .padBottom(4)
          .row();
      slot.add(energyFields[index]).growX().height(44);
      energyTable.add(slot).growX().padRight(12).padBottom(10);
      if (index % 2 == 1) energyTable.row();
    }
    Table energyPanel =
        createSection(SystemRecoveryText.text("computer.meta-energy-heading"), energyTable);
    page.add(energyPanel).growX().left().padTop(16).row();

    Table counts = new Table(skin);
    counts.left().top().defaults().growX();
    moduleField =
        createNumberField(SystemCoreMetaDraft.moduleCount(), SystemCoreMetaDraft::moduleCount);
    batteryField =
        createNumberField(
            SystemCoreMetaDraft.scannedModuleCount(), SystemCoreMetaDraft::scannedModuleCount);
    addCountField(counts, "computer.meta-modules", moduleField);
    addCountField(counts, "computer.meta-batteries", batteryField);
    Table countPanel =
        createSection(SystemRecoveryText.text("computer.meta-count-heading"), counts);
    page.add(countPanel).growX().left().padTop(8).row();

    Table feedbackPanel = new Table(skin);
    feedbackBar = new Table(skin);
    feedbackBar.setBackground("blue_square_depth_flat");
    feedbackPanel.setBackground("generic-area-depth");
    feedbackPanel.add(feedbackBar).width(7).height(28).padRight(12);
    feedback = createLabel(SystemRecoveryText.text("computer.meta-ready"), 17);
    feedback.setWrap(true);
    feedbackPanel.add(feedback).growX().left().padRight(12);
    page.add(feedbackPanel).growX().height(50).left().padTop(14).row();

    Table buttons = new Table(skin);
    submitButton = createButton(SystemRecoveryText.text("computer.meta-submit"), "green", 20);
    submitButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            submitValues();
          }
        });
    clearButton = createButton(SystemRecoveryText.text("computer.meta-clear"), "red-outline", 20);
    clearButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            clearValues();
          }
        });
    buttons.left();
    submitButtonCell = buttons.add(submitButton).height(50).padRight(12);
    clearButtonCell = buttons.add(clearButton).height(50);
    page.add(buttons).left().padTop(14).row();

    centeredContent = new Table(skin);
    centeredContent.top().center();
    pageCell = centeredContent.add(page).top().center().padTop(12);
    scroll = Scene2dElementFactory.createScrollPane(centeredContent, false, true);
    scroll.setOverscroll(false, false);
    add(scroll).grow();
    updatePageWidth();
  }

  private Table createSection(String title, Table body) {
    Table section = new Table(skin);
    section.setBackground("generic-area");
    section.top().left().defaults().growX();
    section.add(createLabel(title, 18)).left().pad(12, 14, 8, 14).row();
    section.add(body).growX().left().pad(0, 14, 4, 4);
    return section;
  }

  @Override
  protected void sizeChanged() {
    super.sizeChanged();
    updatePageWidth();
  }

  private void updatePageWidth() {
    if (pageCell == null) return;
    float width = contentWidth(getWidth());
    pageCell.width(width);
    float buttonWidth = Math.min(230f, (width - 12f) / 2f);
    submitButtonCell.width(buttonWidth);
    clearButtonCell.width(buttonWidth);
    centeredContent.invalidateHierarchy();
    if (scroll != null) scroll.invalidateHierarchy();
  }

  static float contentWidth(float viewportWidth) {
    return Math.min(MAX_CONTENT_WIDTH, Math.max(MIN_CONTENT_WIDTH, viewportWidth - 96f));
  }

  private TextField createNumberField(
      String initialValue, java.util.function.Consumer<String> draftWriter) {
    TextField field = Scene2dElementFactory.createTextField(initialValue);
    field.setMaxLength(3);
    Scene2dElementFactory.addTextFieldChangeListener(
        field,
        value -> {
          draftWriter.accept(value);
          if (!requestPending) lastSubmittedFingerprint = null;
        });
    return field;
  }

  private void addCountField(Table parent, String labelKey, TextField field) {
    Table group = new Table(skin);
    group.top().left();
    Label label = createLabel(SystemRecoveryText.text(labelKey), 20);
    label.setWrap(true);
    group.add(label).growX().left().padBottom(4).row();
    group.add(field).growX().height(44);
    parent.add(group).growX().padRight(12);
  }

  private void submitValues() {
    if (requestPending) return;
    String energy =
        Arrays.stream(energyFields).map(TextField::getText).collect(Collectors.joining(","));
    String payload = energy + "|" + moduleField.getText() + "|" + batteryField.getText();
    lastSubmittedFingerprint = DialogFeedbackFingerprint.of(payload);
    requestPending = true;
    setInputsDisabled(true);
    applyLocalFeedback(
        SystemRecoveryText.text("computer.meta-submitting"), LABEL_COLOR, "generic-area-depth");
    DialogCallbackResolver.createButtonCallback(
            context().dialogId(), SystemRecoveryComputerCallbacks.SYSTEM_CORE_META_SUBMIT)
        .accept(new DialogResponseMessage.StringValue(payload));
  }

  private void clearValues() {
    if (requestPending) return;
    SystemCoreMetaDraft.clear();
    Arrays.stream(energyFields).forEach(field -> field.setText(""));
    moduleField.setText("");
    batteryField.setText("");
    lastSubmittedFingerprint = null;
    applyLocalFeedback(
        SystemRecoveryText.text("computer.meta-ready"), LABEL_COLOR, "blue_square_depth_flat");
  }

  /**
   * Applies server-authoritative feedback to the currently used final input mask.
   *
   * @param serverFeedback authoritative validation result
   */
  public void applyServerFeedback(DialogFeedbackMessage serverFeedback) {
    if (!requestPending
        || lastSubmittedFingerprint == null
        || !lastSubmittedFingerprint.equals(serverFeedback.sourceFingerprint())) return;
    requestPending = false;
    setInputsDisabled(false);
    applyLocalFeedback(
        SystemRecoveryText.text(serverFeedback.messageKey()),
        serverFeedback.successful() ? SUCCESS_COLOR : FAILURE_COLOR,
        serverFeedback.successful() ? "green_square_depth_flat" : "red_square_flat");
  }

  private void setInputsDisabled(boolean disabled) {
    Arrays.stream(energyFields).forEach(field -> field.setDisabled(disabled));
    if (moduleField != null) moduleField.setDisabled(disabled);
    if (batteryField != null) batteryField.setDisabled(disabled);
    if (submitButton != null) submitButton.setDisabled(disabled);
    if (clearButton != null) clearButton.setDisabled(disabled);
  }

  private void applyLocalFeedback(String text, Color color, String barBackground) {
    feedback.getStyle().font =
        FontHelper.getFont(Scene2dElementFactory.FONT_PATH, 18, color, 0, color);
    feedback.setText(text);
    feedbackBar.setBackground(barBackground);
  }
}
