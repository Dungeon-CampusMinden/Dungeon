package util;

import com.badlogic.gdx.Input;
import contrib.configuration.KeyboardConfig;
import contrib.utils.translation.TranslationKey;
import core.utils.Tuple;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import modules.computer.content.BlogTab;
import modules.computer.content.EmailsTab;

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
          Tuple.of(
            TranslationKey.IntroText_1,
              32),
          Tuple.of(
            TranslationKey.IntroText_2,32),
          Tuple.of(TranslationKey.IntroText_3, 32),
          Tuple.of(TranslationKey.IntroText_4, 32),
          Tuple.of(TranslationKey.IntroText_5,32),
          Tuple.of(TranslationKey.IntroText_6, 120));

  private static final String MertensColor = "#aa00aa";

  /** List of outro texts, each with a corresponding font sizes. */
  public static final List<Tuple<String, Integer>> OutroTexts =
      List.of(
          Tuple.of(
              TranslationKey.OutroText_1, 32),
          Tuple.of(
              TranslationKey.OutroText_2,
              32),
          Tuple.of(
            TranslationKey.OutroText_3,
              32),
          Tuple.of(
            TranslationKey.OutroText_4,
              32),
          Tuple.of(
            TranslationKey.OutroText_5,32),
          Tuple.of(TranslationKey.OutroText_6, 120));

  /** List of outro texts shown when the timer expires before escape. */
  public static final List<Tuple<String, Integer>> BadOutroTexts =
      List.of(
          Tuple.of(
              TranslationKey.BadOutroText_1,32),
          Tuple.of(
            TranslationKey.BadOutroText_2,32),
          Tuple.of(
            TranslationKey.BadOutroText_3,32),
          Tuple.of(
            TranslationKey.BadOutroText_4,32),
          Tuple.of(
            TranslationKey.BadOutroText_5,32),
          Tuple.of(TranslationKey.BadOutroText_6, 120));

  /** List of blog entries, each with a title, content and a list of comments. */
  public static final List<BlogTab.BlogEntry> BlogEntries =
      List.of(
          new BlogTab.BlogEntry(
              TranslationKey.BlogEntryTitle_1,
              TranslationKey.BlogEntry_1,
              List.of()),
          new BlogTab.BlogEntry(
              TranslationKey.BlogEntryTitle_2,
              TranslationKey.BlogEntry_2,
              List.of(
                  new BlogTab.BlogComment(
                      "TechAnalyst",
                      TranslationKey.BlogComment_1,
                      60),
                  new BlogTab.BlogComment(
                      "SecureMind",
                      TranslationKey.BlogComment_2,
                      120),
                  new BlogTab.BlogComment(
                      "CyberLab",
                      TranslationKey.BlogComment_3,
                      180))),
          new BlogTab.BlogEntry(
              TranslationKey.BlogEntryTitle_3,
              TranslationKey.BlogEntry_3,
              List.of(
                  new BlogTab.BlogComment(
                      "BinaryCoffee",
                      TranslationKey.BlogComment_4,
                      240),
                  new BlogTab.BlogComment(
                      "BinaryCoffee",
                      TranslationKey.BlogComment_5,
                      300),
                  new BlogTab.BlogComment(
                      "BinaryCoffee",
                      TranslationKey.BlogComment_6,
                      360))));

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
              List.of("Sign_This_To_Receive_Funds.pdf")),
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
              List.of("Linkin_Park_-_In_The_End.mp3.exe")),
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
            List.of("Tracking Details.pdf")),
          new EmailsTab.Email(
              "CryptoGrowth Alerts",
              "alerts@cryptogrowth-daily.biz",
            TranslationKey.Email_9_Subject,
            TranslationKey.Email_9_Content,
            List.of()));

  /** List of attachment file names that lead to viruses. */
  public static final List<String> VirusAttachmentNames =
      List.of("Tracking Details.pdf", "Linkin_Park_-_In_The_End.mp3.exe");

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
  public static final Map<String, String> VirusTypeToCode =
      Map.of(
          "Trojan", "verify before trust",
          "Ransomware", "backup your data",
          "Adware", "read before click");

  /**
   * Special virus type triggered exclusively by plugging a wrong USB stick into the PC. This type
   * is never produced by emails or browser sites and cannot be neutralized via the standard virus
   * tab pass phrase flow. Instead the system shuts itself down after a short delay.
   */
  public static final String UnknownDeviceVirusType = "Unknown Device";

  /** Password required to unlock door 2 in the control panel. */
  public static final String ControlPanelDoor2Password = "214795541";

  /** Poem shown inside the hint.md file on the USB drive. */
  public static final String HintFilePoem =
      "Behind the grate\n"
          + "where dust has grown,\n"
          + "small scraps lie trapped,\n"
          + "forgotten, blown.\n\n"
          + "No hand can reach,\n"
          + "no tool can pry,\n"
          + "but wake the sleeping wind,\n"
          + "and watch them fly.";

  /** File name of the access code document downloaded from the recovery portal. */
  public static final String AccessCodeDownloadFileName = "unlock_code.pdf";

  /**
   * The door code encoded in Morse, to be decoded by the player using the 2nd decryption manual.
   */
  public static final String DoorCodeMorse = "...-- --... ..... ---..";

  /** The actual numeric door code as a list of individual digits. */
  public static final List<Integer> DoorCode = List.of(3, 7, 5, 8);

  public static final String Ringing1 =
      "[speaker name=\"???\"][shake][color=#333333][size=25]*kkrz*[/size][/color][/shake][n][n] Hello? Can you hear me?"
          + "[p]My name is Daniel Krell. I'm the CEO of Ciphera Labs."
          + "[p][speaker name=\"Daniel Krell?\"]How are you guys doing?[pause=0.5] You are still inside, right?"
          + "[p][speaker img={path}]Yes, we are trying to understand what happened here, recover the system and rescue the project data."
          + "[p][speaker name=\"Daniel Krell?\"]Oh, that's great to hear![pause=0.5] Listen, I know this is a tough situation, but I want you to know that we're doing everything we can to help you out."
          + "[p]In fact, [color="
          + MertensColor
          + "]Dr. Mertens[/color] left me a note instructing me to use the green USB Stick to do[tr speed=0.3]... [tr speed=1]something, in case he vanishes."
          + "[p]It doesn't say what needs to be done, but I'm sure you can figure it out."
          + "[p]I need to go now, good luck![n][n][pause=0.5][shake][color=#333333]*click*[/color][/shake]";

  public static final String Ringing2 =
      "[speaker name=\"???\"][shake][color=#333333][size=25]*kkrz*[/size][/color][/shake][n][n] ...Hello? Do you copy?"
          + "[p]It's Adrian Voss."
          + "[p][speaker name=\"Adrian Voss?\"]I've been trying to reach you.[pause=0.5] Heard you got trapped in a crime scene."
          + "[p][speaker img={path}]Another call?"
          + "[p][speaker name=\"Adrian Voss?\"]Did someone else try to contact you before?"
          + "[p][speaker img={path}]Yes, someone called Daniel Krell who claimed to be the CEO of Ciphera Labs."
          + "[p][speaker name=\"Adrian Voss?\"]I've never heard of that person, but they lied to you."
          + "[p][speaker img={path}]Well the door locked shut behind us. We're trying to understand what happened and get out."
          + "[p][speaker name=\"Adrian Voss?\"]Stay focused.[pause=0.5] Navigate [color="
          + MertensColor
          + "]Mertens'[/color] lab with caution."
          + "[p]Before he went missing, [color="
          + MertensColor
          + "]Mertens[/color] tried to pass something to me, but he never actually did."
          + "[p]He kept saying [shake strength=0.3 speed=0.5][color="
          + MertensColor
          + "]\"you'd like the looks, it's your favorite\"[/color][/shake] to me, but I'm not sure what he meant."
          + "[p]Maybe you'll have better luck connecting that to something inside the lab."
          + "[p]Whatever you do, be careful. Voss out.[n][n][pause=0.5][shake strength=0.3 speed=0.3][color=#333333]*click*[/color][/shake]";

  public static final String VentSerialNumber = "49221";
  public static final String VentDialog =
      "Just an ordinary air conditioner.[n][n]You see a text engraved on the steel rim of the gutter:[n][n][n][font=fonts/Doto_Rounded-ExtraBold][align=center][color=#777777]Smart Vents Inc. - SV.IO.5[n]Product Serial: sv000"
          + VentSerialNumber
          + "[n]";

  /**
   * Partial serial numbers shown on the two decoy vents in room 1. Each ends in three dashes
   * because the remaining digits are scratched off and unreadable.
   */
  public static final List<String> DecoyVentSerialNumbers = List.of("sv00057---", "sv00031---");

  /**
   * Dialog shown when interacting with a decoy vent in room 1. The {@code {serial}} placeholder is
   * replaced with the partial, scratched-off serial number of the respective vent.
   */
  public static final String DecoyVentDialog =
      "Just an ordinary air conditioner.[n][n]You see a text engraved on the steel rim of the gutter,"
          + " but most of it has been scratched off and is no longer readable:[n][n][n][font=fonts/Doto_Rounded-ExtraBold][align=center][color=#777777]Smart Vents Inc. - SV.IO.5[n]Product Serial: {serial}[n]";
}
