package modules.computer.content;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import contrib.hud.dialogs.DialogCallbackResolver;
import contrib.hud.elements.RichLabel;
import core.sound.Sounds;
import core.utils.Scene2dElementFactory;
import modules.computer.ComputerFactory;
import modules.computer.ComputerStateComponent;
import util.LastHourSounds;
import util.Lore;

/** Tab content for when the computer is infected with a virus. */
public class VirusTab extends ComputerTab {

  /** Key for identifying the virus tab in the computer dialog. */
  public static String KEY = "virus";

  private static final String STANDARD_TAB_TITLE = "*+*+* VIRUS *+*+*";
  private static final String UNKNOWN_DEVICE_TAB_TITLE = "*+*+* SECURITY *+*+*";

  private static final String STANDARD_TITLE = "[shake strength=0.7 speed=0.8]*+*+* VIRUS *+*+*";
  private static final String UNKNOWN_DEVICE_TITLE =
      "[shake strength=0.7 speed=0.5]Security Violation";
  private static final String UNKNOWN_DEVICE_DESCRIPTION =
      "[tr speed=0.6]An unknown device was plugged into the computer.[n]"
          + "System will be shut down, as per the [color=#ff00ff]security guidelines[/color].";

  private String virusType;

  /**
   * Creates a new VirusTab with the given shared computer state.
   *
   * @param sharedState the shared state component
   */
  public VirusTab(ComputerStateComponent sharedState) {
    super(sharedState, KEY, STANDARD_TAB_TITLE, false);
  }

  protected void createActors() {
    virusType = sharedState().virusType();
    this.clearChildren();

    if (Lore.UnknownDeviceVirusType.equals(virusType)) {
      title(UNKNOWN_DEVICE_TAB_TITLE);
      buildUnknownDeviceLayout();
      return;
    }
    title(STANDARD_TAB_TITLE);

    RichLabel virusLabel = new RichLabel(STANDARD_TITLE, 48, Color.RED);
    virusLabel.setAlignment(Align.center);
    this.add(virusLabel).expandX().center().row();

    RichLabel typeLabel = new RichLabel("Virus Type: " + virusType, 20, Color.RED);
    typeLabel.setAlignment(Align.center);
    this.add(typeLabel).expandX().center().padTop(5).row();

    RichLabel explainLabel =
        new RichLabel(
            "The system has been locked down for protection.[n]"
                + "A reboot with the security pass phrase is required!",
            20,
            Color.RED);
    explainLabel.setAlignment(Align.center);
    this.add(explainLabel).expandX().center().padTop(5).row();

    TextField codeField = Scene2dElementFactory.createTextField("");
    codeField.setMessageText("Security Code");
    codeField.setTextFieldListener(
        (textField, c) -> {
          if (c == '\r' || c == '\n') {
            trySubmitCode(codeField, virusLabel);
          }
        });
    this.add(codeField).width(400).center().padTop(20).row();

    Button submitButton = Scene2dElementFactory.createButton("Submit", "green", 24);
    submitButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            trySubmitCode(codeField, virusLabel);
          }
        });
    this.add(submitButton).width(400).center().padTop(10);

    this.center();
  }

  /**
   * Builds the special "Unknown Device" security violation layout. No code input is shown; the page
   * only informs the player that the system is shutting down.
   */
  private void buildUnknownDeviceLayout() {
    RichLabel header = new RichLabel(UNKNOWN_DEVICE_TITLE, 48, Color.RED);
    header.setAlignment(Align.center);
    this.add(header).expandX().center().row();

    RichLabel typeLabel = new RichLabel("Detected: " + virusType, 20, Color.RED);
    typeLabel.setAlignment(Align.center);
    this.add(typeLabel).expandX().center().padTop(5).row();

    RichLabel explainLabel = new RichLabel(UNKNOWN_DEVICE_DESCRIPTION, 20, Color.RED);
    explainLabel.setAlignment(Align.center);
    this.add(explainLabel).expandX().center().padTop(20).row();

    this.center();
  }

  private void trySubmitCode(TextField codeField, RichLabel virusLabel) {
    String inputCode = codeField.getText().replaceAll("\\s+", "");
    String expected = Lore.VirusTypeToCode.getOrDefault(virusType, "").replaceAll("\\s+", "");
    if (virusType == null || inputCode.equalsIgnoreCase(expected)) {
      virusLabel.setText("[color=#00cc00]Virus Neutralized!");
      VirusTab.this.addAction(
          Actions.sequence(
              Actions.delay(1f),
              Actions.run(
                  () ->
                      DialogCallbackResolver.createButtonCallback(
                              context().dialogId(), ComputerFactory.UPDATE_STATE_KEY)
                          .accept(
                              ComputerStateComponent.getState()
                                  .orElseThrow()
                                  .withInfection(false)))));
      Sounds.play(LastHourSounds.COMPUTER_LOGIN_SUCCESS);
    } else {
      Sounds.play(LastHourSounds.COMPUTER_LOGIN_FAILED);
    }
  }

  @Override
  protected void updateState(ComputerStateComponent newStateComp) {}
}
