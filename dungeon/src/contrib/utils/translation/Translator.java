package contrib.utils.translation;

import com.badlogic.gdx.Input;
import contrib.configuration.KeyboardConfig;
import core.Game;
import core.language.Language;
import core.language.Translation;

import java.util.*;
import java.util.stream.Collectors;

public class Translator {

  public static Translation translation = new Translation("translation");

  public static String translate(String text) {
    Set<String> keysToBeReplaced = allKeys.stream().filter(text::contains).collect(Collectors.toSet());
    String translatedText = text;
    for (String s : keysToBeReplaced) {
      switch  (s) {
        case TranslationKey.PostIntroDialogText2 -> translatedText = translatedText.replace(s,
          translation.text(s,
            core.configuration.KeyboardConfig.MOVEMENT_UP.value(),
            core.configuration.KeyboardConfig.MOVEMENT_LEFT.value(),
            core.configuration.KeyboardConfig.MOVEMENT_DOWN.value(),
            core.configuration.KeyboardConfig.MOVEMENT_RIGHT.value(),
            KeyboardConfig.INTERACT_WORLD.value(),
            Input.Buttons.LEFT,
            KeyboardConfig.INVENTORY_OPEN.value(),
            KeyboardConfig.CLOSE_UI.value(),
            KeyboardConfig.PAUSE_MENU.value()
          ));
        case TranslationKey.Ringing1 -> translatedText = translatedText.replace(s, translation.text(s, MertensColor));
        case TranslationKey.Ringing2 -> translatedText = translatedText.replace(s, translation.text(s, MertensColor));
        case TranslationKey.TimerExpiredRecording -> translatedText = translatedText.replace(s, translation.text(s, MertensColor));
        case TranslationKey.DecoyVentDialog1 -> translatedText = translatedText.replace(s, DecoyVentSerialNumbers.get(0));
        case TranslationKey.DecoyVentDialog2 -> translatedText = translatedText.replace(s, DecoyVentSerialNumbers.get(1));
        case TranslationKey.VentDialog -> translatedText = translatedText.replace(s, VentSerialNumber);
        default -> translatedText = translatedText.replace(s, translation.text(s));
      }
    }
    return translatedText;
  }

  public static boolean hasKey(String text) {
    return allKeys.stream().anyMatch(text::contains);
  }

  private static final Set<String> allKeys = new HashSet<>();
  public static final List<String> DecoyVentSerialNumbers = List.of("sv00057---", "sv00031---");
  private static final String MertensColor = "#aa00aa";
  public static final String VentSerialNumber = "49221";
  private static final String cabinetImagePath = "images/virus-phrases.png";
  private static final String cabinetImagePathDe = "images/virus-phrases-de.png";
  public static void init() {
    allKeys.add(TranslationKey.DecoyVentDialog1);
    allKeys.add(TranslationKey.DecoyVentDialog2);
    allKeys.add(TranslationKey.IntroText_1);
    allKeys.add(TranslationKey.IntroText_2);
    allKeys.add(TranslationKey.IntroText_3);
    allKeys.add(TranslationKey.IntroText_4);
    allKeys.add(TranslationKey.IntroText_5);
    allKeys.add(TranslationKey.IntroText_6);
    allKeys.add(TranslationKey.PostIntroDialogText1);
    allKeys.add(TranslationKey.PostIntroDialogText2);
    allKeys.add(TranslationKey.TimerExpiredRecording);
    allKeys.add(TranslationKey.OutroText_1);
    allKeys.add(TranslationKey.OutroText_2);
    allKeys.add(TranslationKey.OutroText_3);
    allKeys.add(TranslationKey.OutroText_4);
    allKeys.add(TranslationKey.OutroText_5);
    allKeys.add(TranslationKey.OutroText_6);
    allKeys.add(TranslationKey.BadOutroText_1);
    allKeys.add(TranslationKey.BadOutroText_2);
    allKeys.add(TranslationKey.BadOutroText_3);
    allKeys.add(TranslationKey.BadOutroText_4);
    allKeys.add(TranslationKey.BadOutroText_5);
    allKeys.add(TranslationKey.BadOutroText_6);
    allKeys.add(TranslationKey.HintFilePoem);
    allKeys.add(TranslationKey.Ringing1);
    allKeys.add(TranslationKey.Ringing2);
    allKeys.add(TranslationKey.DeskNothing_1);
    allKeys.add(TranslationKey.DeskNothing_2);
    allKeys.add(TranslationKey.Printer);
    allKeys.add(TranslationKey.LockerFind);
    allKeys.add(TranslationKey.LockerEmpty);
    allKeys.add(TranslationKey.LightSwitch_1);
    allKeys.add(TranslationKey.LightSwitch_2);
    allKeys.add(TranslationKey.PhoneInteraction);
  }
}
