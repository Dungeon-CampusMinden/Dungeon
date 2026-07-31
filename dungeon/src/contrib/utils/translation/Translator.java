package contrib.utils.translation;

import core.Game;
import core.language.Language;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Translator {

  public record TranslatedText(String en, String de) {}

  public static String translate(String text, Language language) {
    Set<String> keysToBeReplaced = allKeys.stream().filter(text::contains).collect(Collectors.toSet());
    String translatedText = text;
    if (language == Language.DE) {
      for (String s : keysToBeReplaced) {
        translatedText = translatedText.replace(s, allTexts.get(s).de);
      }
    } else {
      for (String s : keysToBeReplaced) {
        translatedText = translatedText.replace(s, allTexts.get(s).en);
      }
    }
    return translatedText;
  }

  public static boolean hasKey(String text) {
    return allKeys.stream().anyMatch(text::contains);
  }

  private static final Map<String, TranslatedText> allTexts = new HashMap<>();
  private static final Set<String> allKeys = new HashSet<>();

  public static void init() {
    add(TranslationKey.DecoyVentDialogKey,
      "Just an ordinary air conditioner.[n][n]You see a text engraved on the steel rim of the gutter,"
        + " but most of it has been scratched off and is no longer readable:[n][n][n][font=fonts/Doto_Rounded-ExtraBold][align=center][color=#777777]Smart Vents Inc. - SV.IO.5[n]Product Serial: {serial}[n]",
      "Einfach nur eine ganz normale Klimaanlage.[n][n]Man sieht eine Inschrift, die in den Stahlrand des Gitters eingraviert ist,"
        + "aber das meiste davon wurde weggekratzt und ist nicht mehr lesbar:[n][n][n][font=fonts/Doto_Rounded-ExtraBold][align=center][color=#777777]Smart Vents Inc. - SV.IO.5[n]Product Serial: {serial}[n]");

    add(TranslationKey.R2DeskNoteText,
      "[tr speed=0]A note from a colleague:[n][n]"
        + "[tr speed=2.4]Hey, hope you're doing alright! Things have been pretty hectic"
        + " around here lately, so I figured I'd leave you a quick note"
        + " instead of trying to catch you between meetings.[n][n]"
        + "[pause=0.3]Oh, and about that USB stick of yours I borrowed,"
        + " here's the quick rundown:[n][n]"
        + "[tr speed=1.0]- [color=#444477]B[/color]rought it back and left it with the control"
        + " panel key.[n]"
        + "- [color=#444477]L[/color]ightning quick, by the way - best stick I've used.[n]"
        + "- [color=#444477]U[/color]seful little thing, really saved me this week.[n]"
        + "- [color=#444477]E[/color]xpect I'll ask to borrow it again sometime soon![n][n]"
        + "[pause=0.3][tr speed=2.0]Anyway, take care and don't stay too late again. See you"
        + " tomorrow!"
      ,"[tr speed=0]Eine Nachricht von einem Kollegen:[n][n]"
        + "[tr speed=2.4]Hey, ich hoffe, es geht dir gut! In letzter Zeit war es hier ziemlich hektisch,"
        + " deshalb dachte ich mir, ich hinterlasse dir kurz eine Nachricht"
        + " anstatt zu versuchen, dich zwischen zwei Besprechungen zu erwischen.[n][n]"
        + "[pause=0.3]Ach ja, und was deinen USB-Stick angeht, den ich mir ausgeliehen habe,"
        + " hier ist eine kurze Zusammenfassung:[n][n]"
        + "[tr speed=1.0]- [color=#444477]H[/color]abe ihn zurückgebracht und bei der Schalttafel abgelegt.[n]"
        + "- [color=#444477]Ü[/color]brigens blitzschnell – der beste Stick, den ich je benutzt habe.[n]"
        + "- [color=#444477]E[/color]in nützliches kleines Ding, hat mir diese Woche wirklich geholfen.[n]"
        + "- [color=#444477]I[/color]ch werde wohl bald wieder fragen, ob ich ihn mir ausleihen kann![n][n]"
        + "[pause=0.3][tr speed=2.0]Wie auch immer, pass auf dich auf und bleib nicht wieder zu lange weg. Bis"
        + " morgen!");

    add(TranslationKey.IntroText_1, """
       Dr. Elias Mertens, lead security researcher at Ciphera Labs, had been working on a highly
              confidential project over the past several weeks.

              His research was considered a technological breakthrough in digital security.""",
      """
         Dr. Elias Mertens, leitender Sicherheitsforscher bei Ciphera Labs, hatte in den vergangenen Wochen an einem streng
                vertraulichen Projekt gearbeitet.

                Seine Forschungsarbeit galt als technologischer Durchbruch im Bereich der digitalen Sicherheit.
        """);
    add(TranslationKey.IntroText_2,
      "Shortly before his disappearance, he reported unusual access attempts to his systems and suspicious contact requests from unknown sources online.",
      "Kurz vor seinem Verschwinden meldete er ungewöhnliche Zugriffsversuche auf seine Systeme und verdächtige Kontaktanfragen von unbekannten Quellen im Internet.");
    add(TranslationKey.IntroText_3,
      "72 hours ago, all communication with him suddenly stopped.",
      "Vor 72 Stunden brach der Kontakt zu ihm plötzlich ab.");
    add(TranslationKey.IntroText_4,
      "His office was found ransacked...\n\nHis project may have attracted more attention than he realized.",
      "Sein Büro wurde durchwühlt aufgefunden...\n\nSein Projekt hat möglicherweise mehr Aufmerksamkeit auf sich gezogen, als ihm bewusst war.");
    add(TranslationKey.IntroText_5,
      "Your intrusion to his office triggered an alarm and locked the door behind you.\n\nA timer on the wall shows: You have 60 minutes until something happens...",
      "Dein Eindringen in sein Büro hat einen Alarm ausgelöst und die Tür hinter dir verriegelt.\n\nEine Uhr an der Wand zeigt an: Du hast 60 Minuten Zeit, bis etwas passiert...");
    add(TranslationKey.IntroText_6,
      "The Last Hour",
      "The Last Hour");
  }

  public static void add(String key, String en, String de) {
    allKeys.add(key);
    allTexts.put(key, new TranslatedText(en, de));
  }

}
