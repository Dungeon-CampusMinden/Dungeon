package rooms.lasthour.util.translation;

import com.badlogic.gdx.Input;
import engine.Game;
import engine.language.Language;
import engine.language.Translation;
import feature.input.configuration.KeyboardConfig;
import feature.puzzle.Puzzle;
import feature.utils.Translator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Translator for The Last Hour escape room-specific text keys and template values. */
public class LastHourTranslator extends Translator {

  public final List<String> DecoyVentSerialNumbers = List.of("sv00057---", "sv00031---");
  private final String MertensColor = "#aa00aa";
  public final String VentSerialNumber = "49221";
  private final String cabinetImagePathEN = "images/virus-phrases.png";
  private final String cabinetImagePathDE = "images/virus-phrases-de.png";
  public static Puzzle finalCodePuzzelDE;
  public static Puzzle finalCodePuzzelEN;
  public static Puzzle currentPuzzel;

  /** List of URLs mentioned in the emails, which may or may not be trustworthy. */
  public final List<String> EmailCodeUrls =
      List.of(
          "https://support.secugate.com/sg4/recovery-sequence",
          "http://secure-sg4-reset-now.com/verify",
          "http://quick-unlock-sg4.net/code",
          "https://support.seecugate.com/extract");

  public Translation translation = new Translation("translation");

  /** Creates a translator and registers all The Last Hour translation keys. */
  public LastHourTranslator() {
    addAllKeys();
  }

  /**
   * Replaces known translation keys in the given text with localized content.
   *
   * @param text text that may contain translation keys.
   * @return text with all known keys replaced by their localized values.
   */
  public String translate(String text) {
    Set<String> keysToBeReplaced =
        allKeys.stream().filter(text::contains).collect(Collectors.toSet());
    String translatedText = text;
    for (String s : keysToBeReplaced) {
      switch (s) {
        case TranslationKey.PostIntroDialogText2 ->
            translatedText =
                translatedText.replace(
                    s,
                    translation.text(
                        s,
                        engine.configuration.KeyboardConfig.MOVEMENT_UP.value(),
                        engine.configuration.KeyboardConfig.MOVEMENT_LEFT.value(),
                        engine.configuration.KeyboardConfig.MOVEMENT_DOWN.value(),
                        engine.configuration.KeyboardConfig.MOVEMENT_RIGHT.value(),
                        KeyboardConfig.INTERACT_WORLD.value(),
                        Input.Buttons.LEFT,
                        KeyboardConfig.INVENTORY_OPEN.value(),
                        KeyboardConfig.CLOSE_UI.value(),
                        KeyboardConfig.PAUSE_MENU.value()));
        case TranslationKey.Ringing1 ->
            translatedText = translatedText.replace(s, translation.text(s, MertensColor));
        case TranslationKey.Ringing2 ->
            translatedText = translatedText.replace(s, translation.text(s, MertensColor));
        case TranslationKey.TimerExpiredRecording ->
            translatedText = translatedText.replace(s, translation.text(s, MertensColor));
        case TranslationKey.DecoyVentDialog1 ->
            translatedText =
                translatedText.replace(s, translation.text(s, DecoyVentSerialNumbers.get(0)));
        case TranslationKey.DecoyVentDialog2 ->
            translatedText =
                translatedText.replace(s, translation.text(s, DecoyVentSerialNumbers.get(1)));
        case TranslationKey.VentDialog ->
            translatedText = translatedText.replace(s, translation.text(s, VentSerialNumber));
        case TranslationKey.Email_2_Content ->
            translatedText = translatedText.replace(s, translation.text(s, EmailCodeUrls.get(1)));
        case TranslationKey.Email_4_Content ->
            translatedText = translatedText.replace(s, translation.text(s, EmailCodeUrls.get(0)));
        case TranslationKey.Email_5_Content ->
            translatedText = translatedText.replace(s, translation.text(s, EmailCodeUrls.get(2)));
        case TranslationKey.Email_7_Content ->
            translatedText = translatedText.replace(s, translation.text(s, EmailCodeUrls.get(3)));
        case TranslationKey.cabinetImage ->
            translatedText =
                translatedText.replace(
                    s,
                    Game.localization().currentLanguage().equals(Language.DE)
                        ? cabinetImagePathDE
                        : cabinetImagePathEN);
        default -> translatedText = translatedText.replace(s, translation.text(s));
      }
    }
    return translatedText;
  }

  /** Registers all translation keys used by The Last Hour. */
  public static void addAllKeys() {
    registerKey(TranslationKey.DecoyVentDialog1);
    registerKey(TranslationKey.DecoyVentDialog2);
    registerKey(TranslationKey.VentDialog);
    registerKey(TranslationKey.R2DeskNoteText);
    registerKey(TranslationKey.IntroText_1);
    registerKey(TranslationKey.IntroText_2);
    registerKey(TranslationKey.IntroText_3);
    registerKey(TranslationKey.IntroText_4);
    registerKey(TranslationKey.IntroText_5);
    registerKey(TranslationKey.IntroText_6);
    registerKey(TranslationKey.PostIntroDialogText1);
    registerKey(TranslationKey.PostIntroDialogText2);
    registerKey(TranslationKey.TimerExpiredRecording);
    registerKey(TranslationKey.OutroText_1);
    registerKey(TranslationKey.OutroText_2);
    registerKey(TranslationKey.OutroText_3);
    registerKey(TranslationKey.OutroText_4);
    registerKey(TranslationKey.OutroText_5);
    registerKey(TranslationKey.OutroText_6);
    registerKey(TranslationKey.BadOutroText_1);
    registerKey(TranslationKey.BadOutroText_2);
    registerKey(TranslationKey.BadOutroText_3);
    registerKey(TranslationKey.BadOutroText_4);
    registerKey(TranslationKey.BadOutroText_5);
    registerKey(TranslationKey.BadOutroText_6);
    registerKey(TranslationKey.HintFilePoem);
    registerKey(TranslationKey.Ringing1);
    registerKey(TranslationKey.Ringing2);
    registerKey(TranslationKey.DeskNothing_1);
    registerKey(TranslationKey.DeskNothing_2);
    registerKey(TranslationKey.Printer);
    registerKey(TranslationKey.LockerFind);
    registerKey(TranslationKey.LockerEmpty);
    registerKey(TranslationKey.LightSwitch_1);
    registerKey(TranslationKey.LightSwitch_2);
    registerKey(TranslationKey.PhoneInteraction);
    registerKey(TranslationKey.BlogEntryTitle_1);
    registerKey(TranslationKey.BlogEntry_1);
    registerKey(TranslationKey.BlogEntryTitle_2);
    registerKey(TranslationKey.BlogEntry_2);
    registerKey(TranslationKey.BlogEntryTitle_3);
    registerKey(TranslationKey.BlogEntry_3);
    registerKey(TranslationKey.BlogComment_1);
    registerKey(TranslationKey.BlogComment_2);
    registerKey(TranslationKey.BlogComment_3);
    registerKey(TranslationKey.BlogComment_4);
    registerKey(TranslationKey.BlogComment_5);
    registerKey(TranslationKey.BlogComment_6);
    registerKey(TranslationKey.Email_1_Subject);
    registerKey(TranslationKey.Email_1_Content);
    registerKey(TranslationKey.Email_2_Subject);
    registerKey(TranslationKey.Email_2_Content);
    registerKey(TranslationKey.Email_3_Subject);
    registerKey(TranslationKey.Email_3_Content);
    registerKey(TranslationKey.Email_4_Subject);
    registerKey(TranslationKey.Email_4_Content);
    registerKey(TranslationKey.Email_5_Subject);
    registerKey(TranslationKey.Email_5_Content);
    registerKey(TranslationKey.Email_6_Subject);
    registerKey(TranslationKey.Email_6_Content);
    registerKey(TranslationKey.Email_7_Subject);
    registerKey(TranslationKey.Email_7_Content);
    registerKey(TranslationKey.Email_8_Subject);
    registerKey(TranslationKey.Email_8_Content);
    registerKey(TranslationKey.Email_9_Subject);
    registerKey(TranslationKey.Email_9_Content);
    registerKey(TranslationKey.cabinetImage);
    registerKey(TranslationKey.LoginScreenText);
    registerKey(TranslationKey.LoginScreenWrongFeedback);
    registerKey(TranslationKey.LoginScreenCorrectFeedback);
    registerKey(TranslationKey.AboutHeaderText);
    registerKey(TranslationKey.AboutInfoText);
    registerKey(TranslationKey.AboutQnAText);
    registerKey(TranslationKey.AboutFooterText);
    registerKey(TranslationKey.EmailSelectText);
    registerKey(TranslationKey.EmailFromText);
    registerKey(TranslationKey.EmailSubjectText);
    registerKey(TranslationKey.EmailAttachmentsText);
    registerKey(TranslationKey.EmailLINK_SOMEText);
    registerKey(TranslationKey.EmailLINK_NONEText);
    registerKey(TranslationKey.EmailAttachment_1);
    registerKey(TranslationKey.EmailAttachment_2);
    registerKey(TranslationKey.EmailAttachment_3);
    registerKey(TranslationKey.BrowserPlaceholder);
    registerKey(TranslationKey.BrowserHistory);
    registerKey(TranslationKey.BrowserGo);
    registerKey(TranslationKey.BrowserPageNotFound);
    registerKey(TranslationKey.BrowserEnterWebsite);
    registerKey(TranslationKey.BrowserSecurityCodePage01);
    registerKey(TranslationKey.BrowserSecurityCodePage02);
    registerKey(TranslationKey.BrowserSecurityCodePage03);
    registerKey(TranslationKey.BrowserSecurityCodePage04);
    registerKey(TranslationKey.BrowserSecurityCodePage05);
    registerKey(TranslationKey.BrowserSecurityCodePage06);
    registerKey(TranslationKey.BrowserSecurityCodePage07);
    registerKey(TranslationKey.BrowserSecurityCodePage08);
    registerKey(TranslationKey.BrowserSecurityCodePage09);
    registerKey(TranslationKey.BrowserSecurityCodePage10);
    registerKey(TranslationKey.BrowserSecurityCodePage11);
    registerKey(TranslationKey.BrowserSecurityCodePage12);
    registerKey(TranslationKey.BrowserSecurityCodePage13);
    registerKey(TranslationKey.BrowserSecurityCodePage14);
    registerKey(TranslationKey.BrowserSecurityCodePage15);
    registerKey(TranslationKey.BrowserSecurityCodePage16);
    registerKey(TranslationKey.BrowserSecurityCodePage17);
    registerKey(TranslationKey.BrowserSecurityCodePage18);
    registerKey(TranslationKey.BrowserSecurityCodePage19);
    registerKey(TranslationKey.BrowserSecurityCodePage20);
    registerKey(TranslationKey.VirusText01);
    registerKey(TranslationKey.VirusText02);
    registerKey(TranslationKey.VirusText03);
    registerKey(TranslationKey.VirusText04);
    registerKey(TranslationKey.VirusText05);
    registerKey(TranslationKey.VirusText06);
    registerKey(TranslationKey.VirusText07);
    registerKey(TranslationKey.VirusText08);
    registerKey(TranslationKey.VirusText09);
    registerKey(TranslationKey.BlogText1);
    registerKey(TranslationKey.BlogText2);
    registerKey(TranslationKey.BlogText3);
    registerKey(TranslationKey.UnlockCodePage1);
    registerKey(TranslationKey.UnlockCodePage2);
    registerKey(TranslationKey.UnlockCodePage3);
    registerKey(TranslationKey.UnlockCodePage4);
    registerKey(TranslationKey.UnlockCodePage5);
    registerKey(TranslationKey.UnlockCodePage6);
    registerKey(TranslationKey.UnlockCodePage7);
    registerKey(TranslationKey.UnlockCodePage8);

    registerKey(TranslationKey.ControlPanel01);
    registerKey(TranslationKey.ControlPanel02);
    registerKey(TranslationKey.ControlPanel03);
    registerKey(TranslationKey.ControlPanel04);
    registerKey(TranslationKey.ControlPanel05);
    registerKey(TranslationKey.ControlPanel06);
    registerKey(TranslationKey.ControlPanel07);
    registerKey(TranslationKey.ControlPanel08);
    registerKey(TranslationKey.ControlPanel09);
    registerKey(TranslationKey.ControlPanel10);
    registerKey(TranslationKey.ControlPanel11);
    registerKey(TranslationKey.ControlPanel12);
    registerKey(TranslationKey.ControlPanel13);
    registerKey(TranslationKey.ControlPanel14);
    registerKey(TranslationKey.ControlPanel15);
    registerKey(TranslationKey.ControlPanel16);
    registerKey(TranslationKey.ControlPanel17);
    registerKey(TranslationKey.ControlPanel18);
    registerKey(TranslationKey.ControlPanel19);
    registerKey(TranslationKey.ControlPanel20);
    registerKey(TranslationKey.ControlPanel21);
    registerKey(TranslationKey.ControlPanel22);
    registerKey(TranslationKey.ControlPanel23);
    registerKey(TranslationKey.ControlPanel24);

    registerKey(TranslationKey.ComputerOfflineText);
    registerKey(TranslationKey.ComputerUSBStickText);
    registerKey(TranslationKey.ComputerAccessText);
  }
}
