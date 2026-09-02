package rooms.lasthour.util;

import engine.language.Language;
import engine.utils.Tuple;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import rooms.lasthour.modules.computer.content.BlogTab;
import rooms.lasthour.modules.computer.content.EmailsTab;
import rooms.lasthour.util.translation.TranslationKey;

/**
 * Helper class to store all the lore related information, such as story texts, character names,
 * emails and blog entries.
 */
public class Lore {

  /** Name of the company. */
  public static final String CompanyName = "Ciphera Labs";

  /** Short name of the company. */
  public static final String CompanyDrawable = "company_logo";

  /** Name of the scientist, the main character of the story. */
  public static final String ScientistName = "Dr. Elias Mertens";

  /** Short name of the scientist. */
  public static final String ScientistNameShort = "Dr. Mertens";

  /** Blog name. */
  public static final String ScientistBlogName = "Elias Blog";

  /** Email of the scientist. */
  public static final String ScientistEmail = "dr.mertens@ciphera-labs.com";

  /** Email and password for the computer login. */
  public static final String LoginEmail = ScientistEmail;

  /** Password for the computer login. */
  public static final String LoginPassword = "a12b34xy";

  /** List of intro texts, each with a corresponding font sizes. */
  public static final List<Tuple<String, Integer>> IntroTexts =
      List.of(
          Tuple.of(TranslationKey.IntroText_1, 32),
          Tuple.of(TranslationKey.IntroText_2, 32),
          Tuple.of(TranslationKey.IntroText_3, 32),
          Tuple.of(TranslationKey.IntroText_4, 32),
          Tuple.of(TranslationKey.IntroText_5, 32),
          Tuple.of(TranslationKey.IntroText_6, 120));

  private static final String MertensColor = "#aa00aa";

  /** List of outro texts, each with a corresponding font sizes. */
  public static final List<Tuple<String, Integer>> OutroTexts =
      List.of(
          Tuple.of(TranslationKey.OutroText_1, 32),
          Tuple.of(TranslationKey.OutroText_2, 32),
          Tuple.of(TranslationKey.OutroText_3, 32),
          Tuple.of(TranslationKey.OutroText_4, 32),
          Tuple.of(TranslationKey.OutroText_5, 32),
          Tuple.of(TranslationKey.OutroText_6, 120));

  /** List of outro texts shown when the timer expires before escape. */
  public static final List<Tuple<String, Integer>> BadOutroTexts =
      List.of(
          Tuple.of(TranslationKey.BadOutroText_1, 32),
          Tuple.of(TranslationKey.BadOutroText_2, 32),
          Tuple.of(TranslationKey.BadOutroText_3, 32),
          Tuple.of(TranslationKey.BadOutroText_4, 32),
          Tuple.of(TranslationKey.BadOutroText_5, 32),
          Tuple.of(TranslationKey.BadOutroText_6, 120));

  /** List of blog entries, each with a title, content and a list of comments. */
  public static final List<BlogTab.BlogEntry> BlogEntries =
      List.of(
          new BlogTab.BlogEntry(
              TranslationKey.BlogEntryTitle_1, TranslationKey.BlogEntry_1, List.of()),
          new BlogTab.BlogEntry(
              TranslationKey.BlogEntryTitle_2,
              TranslationKey.BlogEntry_2,
              List.of(
                  new BlogTab.BlogComment("TechAnalyst", TranslationKey.BlogComment_1, 60),
                  new BlogTab.BlogComment("SecureMind", TranslationKey.BlogComment_2, 120),
                  new BlogTab.BlogComment("CyberLab", TranslationKey.BlogComment_3, 180))),
          new BlogTab.BlogEntry(
              TranslationKey.BlogEntryTitle_3,
              TranslationKey.BlogEntry_3,
              List.of(
                  new BlogTab.BlogComment("BinaryCoffee", TranslationKey.BlogComment_4, 240),
                  new BlogTab.BlogComment("BinaryCoffee", TranslationKey.BlogComment_5, 300),
                  new BlogTab.BlogComment("BinaryCoffee", TranslationKey.BlogComment_6, 360))));

  /** The real access URL. */
  public static final String RealAccessUrl = "https://secugate-support.com/sg4/access";

  /** List of URLs mentioned in the emails, which may or may not be trustworthy. */
  public static final List<String> EmailCodeUrls =
      List.of(
          "https://support.secugate.com/sg4/recovery-sequence",
          "http://secure-sg4-reset-now.com/verify",
          "http://quick-unlock-sg4.net/code",
          "https://support.seecugate.com/extract");

  /** List of emails, each with a sender, sender email, subject, content and list of attachments. */
  public static final List<EmailsTab.Email> EmailList =
      List.of(
          new EmailsTab.Email(
              "Prince Adewale Foundation",
              "royaloffice.transferdesk@poqwmavnakld.gl",
              TranslationKey.Email_1_Subject,
              TranslationKey.Email_1_Content,
              List.of(TranslationKey.EmailAttachment_1)),
          new EmailsTab.Email(
              "SecuGate Support",
              "support@secugate-reset247.com",
              TranslationKey.Email_2_Subject,
              TranslationKey.Email_2_Content,
              List.of()),
          new EmailsTab.Email(
              "Music Downloader",
              "noreply@illegal-music-downloader.com",
              TranslationKey.Email_3_Subject,
              TranslationKey.Email_3_Content,
              List.of(TranslationKey.EmailAttachment_2)),
          new EmailsTab.Email(
              "Andreas Keller",
              "andreas.keller@secugate.com",
              TranslationKey.Email_4_Subject,
              TranslationKey.Email_4_Content,
              List.of()),
          new EmailsTab.Email(
              "Marc",
              "marc.unlockhelp@gmail.com",
              TranslationKey.Email_5_Subject,
              TranslationKey.Email_5_Content,
              List.of()),
          new EmailsTab.Email(
              "Telekom",
              "contact@local-connections-now.net",
              TranslationKey.Email_6_Subject,
              TranslationKey.Email_6_Content,
              List.of()),
          new EmailsTab.Email(
              "SG4 Recovery System",
              "system@secugate.com",
              TranslationKey.Email_7_Subject,
              TranslationKey.Email_7_Content,
              List.of()),
          new EmailsTab.Email(
              "Global Parcel Logistics",
              "tracking@parcel-hold-center.info",
              TranslationKey.Email_8_Subject,
              TranslationKey.Email_8_Content,
              List.of(TranslationKey.EmailAttachment_3)),
          new EmailsTab.Email(
              "CryptoGrowth Alerts",
              "alerts@cryptogrowth-daily.biz",
              TranslationKey.Email_9_Subject,
              TranslationKey.Email_9_Content,
              List.of()));

  /** List of attachment file names that lead to viruses. */
  public static final List<String> VirusAttachmentNames =
      List.of(TranslationKey.EmailAttachment_2, TranslationKey.EmailAttachment_3);

  /** List of URLs that lead to viruses. */
  public static final List<String> VirusWebsites;

  static {
    List<String> sites = new java.util.ArrayList<>();
    // Direct virus links from emails
    sites.add("https://illegal-music-downloader.com/download/12345");
    sites.add("https://adq.mmcaok.com/pl10fonmxdm1asmokxx0");
    sites.add("https://local-connections-now.net/start");
    sites.add("https://royal-transferdesk.org/secure");
    sites.add("https://cryptogrowth-daily.biz/start?si=1ujg0h1ju8mnc980mumsdnuz0");
    // Phishing code URLs (all EmailCodeUrls except the real one at index 0)
    sites.addAll(EmailCodeUrls.subList(1, EmailCodeUrls.size()));
    VirusWebsites = Collections.unmodifiableList(sites);
  }

  /** List of ASCII codes used for the security code pages in the browser recovery portal. */
  public static final List<String> AsciiCodes = List.of("6548", "1765", "3912", "8256");

  /**
   * Mapping from security code page index (1-based, excluding the real page at index 0) to the
   * virus type that is triggered when the user downloads the file from a fake page.
   */
  public static final List<String> CodePageIndexToVirusType =
      List.of("Trojan", "Ransomware", "Adware");

  /** Mapping from virus type name to the code required to neutralize it in the virus tab. */
  public static final Map<String, Map<Language, String>> VirusTypeToCode =
      Map.of(
          "Trojan",
              Map.of(
                  Language.EN,
                  "verify before trust",
                  Language.DE,
                  "ERST ÜBERPRÜFEN, DANN VERTRAUEN"),
          "Ransomware",
              Map.of(Language.EN, "backup your data", Language.DE, "SICHERN SIE IHRE DATEN"),
          "Adware", Map.of(Language.EN, "read before click", Language.DE, "VOR DEM KLICKEN LESEN"));

  /**
   * Special virus type triggered exclusively by plugging a wrong USB stick into the PC. This type
   * is never produced by emails or browser sites and cannot be neutralized via the standard virus
   * tab pass phrase flow. Instead the system shuts itself down after a short delay.
   */
  public static final String UnknownDeviceVirusType = "Unknown Device";

  /** Password required to unlock door 2 in the control panel. */
  public static final String ControlPanelDoor2Password = "214795541";

  /** File name of the access code document downloaded from the recovery portal. */
  public static final String AccessCodeDownloadFileName = "unlock_code.pdf";

  /**
   * The door code encoded in Morse, to be decoded by the player using the 2nd decryption manual.
   */
  public static final String DoorCodeMorse = "...-- --... ..... ---..";

  /** The actual numeric door code as a list of individual digits. */
  public static final List<Integer> DoorCode = List.of(3, 7, 5, 8);

  public static final String VentSerialNumber = "49221";
}
