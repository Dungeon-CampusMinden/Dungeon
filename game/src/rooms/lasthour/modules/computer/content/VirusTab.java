package rooms.lasthour.modules.computer.content;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import engine.language.Localization;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.sound.Sounds;
import engine.utils.Scene2dElementFactory;
import feature.hud.dialogs.DialogCallbackResolver;
import feature.hud.elements.RichLabel;
import rooms.lasthour.modules.computer.ComputerCallbacks;
import rooms.lasthour.modules.computer.ComputerStateComponent;
import rooms.lasthour.util.LastHourSounds;
import rooms.lasthour.util.Lore;
import rooms.lasthour.util.translation.TranslationKey;

/** Tab content for when the computer is infected with a virus. */
public class VirusTab extends ComputerTab {

  /** Key for identifying the virus tab in the computer dialog. */
  public static String KEY = "virus";

  private static final String STANDARD_TAB_TITLE = "*+*+* VIRUS *+*+*";

  private static final String STANDARD_TITLE = "[shake strength=0.7 speed=0.8]*+*+* VIRUS *+*+*";

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
      title(
          Localization.getInstance().getCurrentTranslator().translate(TranslationKey.VirusText01));
      buildUnknownDeviceLayout();
      return;
    }
    title(STANDARD_TAB_TITLE);

    RichLabel virusLabel = new RichLabel(STANDARD_TITLE, 48, Color.RED);
    virusLabel.setAlignment(Align.center);
    this.add(virusLabel).expandX().center().row();

    RichLabel typeLabel =
        new RichLabel(
            Localization.getInstance().getCurrentTranslator().translate(TranslationKey.VirusText04)
                + ": "
                + virusType,
            20,
            Color.RED);
    typeLabel.setAlignment(Align.center);
    this.add(typeLabel).expandX().center().padTop(5).row();

    RichLabel explainLabel =
        new RichLabel(
            Localization.getInstance().getCurrentTranslator().translate(TranslationKey.VirusText05),
            20,
            Color.RED);
    explainLabel.setAlignment(Align.center);
    this.add(explainLabel).expandX().center().padTop(5).row();

    TextField codeField = Scene2dElementFactory.createTextField("");
    codeField.setMessageText(
        Localization.getInstance().getCurrentTranslator().translate(TranslationKey.VirusText06));
    codeField.setTextFieldListener(
        (textField, c) -> {
          if (c == '\r' || c == '\n') {
            trySubmitCode(codeField, virusLabel);
          }
        });
    this.add(codeField).width(400).center().padTop(20).row();

    Button submitButton =
        Scene2dElementFactory.createButton(
            Localization.getInstance().getCurrentTranslator().translate(TranslationKey.VirusText07),
            "green",
            24);
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
    RichLabel header =
        new RichLabel(
            Localization.getInstance().getCurrentTranslator().translate(TranslationKey.VirusText02),
            48,
            Color.RED);
    header.setAlignment(Align.center);
    this.add(header).expandX().center().row();

    RichLabel typeLabel =
        new RichLabel(
            Localization.getInstance().getCurrentTranslator().translate(TranslationKey.VirusText08)
                + ": "
                + virusType,
            20,
            Color.RED);
    typeLabel.setAlignment(Align.center);
    this.add(typeLabel).expandX().center().padTop(5).row();

    RichLabel explainLabel =
        new RichLabel(
            Localization.getInstance().getCurrentTranslator().translate(TranslationKey.VirusText03),
            20,
            Color.RED);
    explainLabel.setAlignment(Align.center);
    this.add(explainLabel).expandX().center().padTop(20).row();

    this.center();
  }

  private void trySubmitCode(TextField codeField, RichLabel virusLabel) {
    String rawCode = codeField.getText();
    DialogCallbackResolver.createButtonCallback(
            context().dialogId(), ComputerCallbacks.VIRUS_CODE_ATTEMPT_KEY)
        .accept(new DialogResponseMessage.StringValue(rawCode));
    String inputCode = rawCode.replaceAll("\\s+", "");

    String expected = Lore.VirusTypeToCode.getOrDefault(virusType, null);
    String expectedString =
        Localization.getInstance()
            .getCurrentTranslator()
            .translate(expected)
            .replaceAll("\\s+", "");
    if (virusType == null || inputCode.equalsIgnoreCase(expectedString)) {
      virusLabel.setText(
          Localization.getInstance().getCurrentTranslator().translate(TranslationKey.VirusText09));
      Sounds.play(LastHourSounds.COMPUTER_LOGIN_SUCCESS);
    } else {
      Sounds.play(LastHourSounds.COMPUTER_LOGIN_FAILED);
    }
  }

  @Override
  protected void updateState(ComputerStateComponent newStateComp) {}
}
