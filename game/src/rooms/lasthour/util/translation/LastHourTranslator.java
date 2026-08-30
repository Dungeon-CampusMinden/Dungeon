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
  }
}
