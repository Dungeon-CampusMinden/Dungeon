package rooms.systemRecovery.modules.computer.content;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
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

  private final TextField[] energyFields =
      new TextField[SystemCoreMetaDraft.ENERGY_SLOT_COUNT];
  private TextField moduleField;
  private TextField batteryField;
  private Label feedback;
  private Table feedbackBar;
  private String lastSubmittedFingerprint;
  private static final Color SUCCESS_COLOR = new Color(0.12f, 0.65f, 0.25f, 1f);
  private static final Color FAILURE_COLOR = new Color(0.85f, 0.12f, 0.12f, 1f);

  /** Creates the final system-state input mask. */
  public SystemCoreMetaTab() {
    super(KEY, SystemRecoveryText.text("computer.meta-tab"));
    createActors();
  }

  @Override
  protected void createActors() {
    Table layout = new Table(skin);
    layout.top().defaults().growX();
    layout.add(createLabel(SystemRecoveryText.text("computer.meta-heading"), 24)).left().row();
    layout
        .add(createLabel(SystemRecoveryText.text("computer.meta-instruction"), 20))
        .left()
        .padTop(12)
        .row();

    Table energyTable = new Table(skin);
    energyTable.top().left();
    for (int index = 0; index < SystemCoreMetaDraft.ENERGY_SLOT_COUNT; index++) {
      int slotIndex = index;
      energyFields[index] =
          createNumberField(
              SystemCoreMetaDraft.energyValue(index),
              value -> SystemCoreMetaDraft.energyValue(slotIndex, value));
      Table slot = new Table(skin);
      slot.add(createLabel(SystemRecoveryText.text("computer.meta-energy-slot", index + 1), 18))
          .left()
          .row();
      slot.add(energyFields[index]).width(100).height(48).padTop(5);
      energyTable.add(slot).left().padRight(14);
    }
    layout.add(energyTable).left().padTop(22).row();

    Table counts = new Table(skin);
    counts.left().top();
    moduleField =
        createNumberField(
            SystemCoreMetaDraft.moduleCount(), SystemCoreMetaDraft::moduleCount);
    batteryField =
        createNumberField(
            SystemCoreMetaDraft.scannedModuleCount(),
            SystemCoreMetaDraft::scannedModuleCount);
    addCountField(counts, "computer.meta-modules", moduleField);
    addCountField(counts, "computer.meta-batteries", batteryField);
    layout.add(counts).left().padTop(24).row();

    Table feedbackPanel = new Table(skin);
    feedbackBar = new Table(skin);
    feedbackBar.setBackground("generic-area-depth");
    feedbackPanel.add(feedbackBar).width(8).growY().padRight(10);
    feedback = createLabel("", 18);
    feedback.setWrap(true);
    feedbackPanel.add(feedback).growX().left();
    layout.add(feedbackPanel).growX().left().padTop(18).row();

    Table buttons = new Table(skin);
    TextButton submit = createButton(SystemRecoveryText.text("computer.meta-submit"), "green", 24);
    submit.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            submitValues();
          }
        });
    TextButton clear =
        createButton(SystemRecoveryText.text("computer.meta-clear"), "red-outline", 24);
    clear.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            clearValues();
          }
        });
    buttons.add(submit).width(290).height(52).padRight(12);
    buttons.add(clear).width(180).height(52);
    layout.add(buttons).right().padTop(18);
    add(layout).grow();
  }

  private TextField createNumberField(
      String initialValue, java.util.function.Consumer<String> draftWriter) {
    TextField field = Scene2dElementFactory.createTextField(initialValue);
    field.setMaxLength(3);
    Scene2dElementFactory.addTextFieldChangeListener(
        field,
        value -> {
          draftWriter.accept(value);
          lastSubmittedFingerprint = null;
        });
    return field;
  }

  private void addCountField(Table parent, String labelKey, TextField field) {
    parent.add(createLabel(SystemRecoveryText.text(labelKey), 20)).left().padRight(12);
    parent.add(field).width(120).height(48).padRight(28);
  }

  private void submitValues() {
    String energy =
        Arrays.stream(energyFields).map(TextField::getText).collect(Collectors.joining(","));
    String payload = energy + "|" + moduleField.getText() + "|" + batteryField.getText();
    lastSubmittedFingerprint = DialogFeedbackFingerprint.of(payload);
    applyLocalFeedback(
        SystemRecoveryText.text("computer.meta-submitting"), LABEL_COLOR, "generic-area-depth");
    DialogCallbackResolver.createButtonCallback(
            context().dialogId(), SystemRecoveryComputerCallbacks.SYSTEM_CORE_META_SUBMIT)
        .accept(new DialogResponseMessage.StringValue(payload));
  }

  private void clearValues() {
    SystemCoreMetaDraft.clear();
    Arrays.stream(energyFields).forEach(field -> field.setText(""));
    moduleField.setText("");
    batteryField.setText("");
    lastSubmittedFingerprint = null;
    applyLocalFeedback("", LABEL_COLOR, "generic-area-depth");
  }

  /**
   * Applies server-authoritative feedback to the currently used final input mask.
   *
   * @param serverFeedback authoritative validation result
   */
  public void applyServerFeedback(DialogFeedbackMessage serverFeedback) {
    if (!serverFeedback.sourceFingerprint().isEmpty()
        && !serverFeedback.sourceFingerprint().equals(lastSubmittedFingerprint)) {
      return;
    }
    applyLocalFeedback(
        SystemRecoveryText.text(serverFeedback.messageKey()),
        serverFeedback.successful() ? SUCCESS_COLOR : FAILURE_COLOR,
        serverFeedback.successful() ? "green_square_depth_flat" : "red_square_flat");
  }

  private void applyLocalFeedback(String text, Color color, String barBackground) {
    feedback.getStyle().font =
        FontHelper.getFont(Scene2dElementFactory.FONT_PATH, 18, color, 0, color);
    feedback.setText(text);
    feedbackBar.setBackground(barBackground);
  }
}
