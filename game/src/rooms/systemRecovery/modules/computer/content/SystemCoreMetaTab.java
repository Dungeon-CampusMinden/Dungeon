package rooms.systemRecovery.modules.computer.content;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.utils.Scene2dElementFactory;
import feature.hud.dialogs.DialogCallbackResolver;
import java.util.Arrays;
import java.util.stream.Collectors;
import rooms.systemRecovery.modules.computer.SystemRecoveryComputerCallbacks;
import rooms.systemRecovery.modules.computer.SystemRecoveryComputerTab;
import rooms.systemRecovery.util.SystemRecoveryText;

/** Input mask for transferring the three results shown by the central system display. */
public final class SystemCoreMetaTab extends SystemRecoveryComputerTab {

  /** Stable tab key used by the computer dialog. */
  public static final String KEY = "system-core-meta";
  private static final int ENERGY_SLOT_COUNT = 5;

  private final TextField[] energyFields = new TextField[ENERGY_SLOT_COUNT];
  private TextField moduleField;
  private TextField batteryField;
  private Label feedback;

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
    for (int index = 0; index < ENERGY_SLOT_COUNT; index++) {
      energyFields[index] = createNumberField();
      Table slot = new Table(skin);
      slot.add(
              createLabel(
                  SystemRecoveryText.text("computer.meta-energy-slot", index + 1), 18))
          .left()
          .row();
      slot.add(energyFields[index]).width(100).height(48).padTop(5);
      energyTable.add(slot).left().padRight(14);
    }
    layout.add(energyTable).left().padTop(22).row();

    Table counts = new Table(skin);
    counts.left().top();
    moduleField = createNumberField();
    batteryField = createNumberField();
    addCountField(counts, "computer.meta-modules", moduleField);
    addCountField(counts, "computer.meta-batteries", batteryField);
    layout.add(counts).left().padTop(24).row();

    feedback = createLabel("", 18);
    layout.add(feedback).left().padTop(18).row();

    Table buttons = new Table(skin);
    TextButton submit =
        createButton(SystemRecoveryText.text("computer.meta-submit"), "green", 24);
    submit.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            submitValues();
          }
        });
    TextButton clear = createButton(SystemRecoveryText.text("computer.meta-clear"), "red-outline", 24);
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

  private TextField createNumberField() {
    TextField field = Scene2dElementFactory.createTextField("");
    field.setMaxLength(3);
    return field;
  }

  private void addCountField(Table parent, String labelKey, TextField field) {
    parent
        .add(createLabel(SystemRecoveryText.text(labelKey), 20))
        .left()
        .padRight(12);
    parent.add(field).width(120).height(48).padRight(28);
  }

  private void submitValues() {
    String energy =
        Arrays.stream(energyFields).map(TextField::getText).collect(Collectors.joining(","));
    String payload = energy + "|" + moduleField.getText() + "|" + batteryField.getText();
    DialogCallbackResolver.createButtonCallback(
            context().dialogId(), SystemRecoveryComputerCallbacks.SYSTEM_CORE_META_SUBMIT)
        .accept(new DialogResponseMessage.StringValue(payload));
    feedback.setText(SystemRecoveryText.text("computer.meta-submitting"));
  }

  private void clearValues() {
    Arrays.stream(energyFields).forEach(field -> field.setText(""));
    moduleField.setText("");
    batteryField.setText("");
    feedback.setText("");
  }
}
