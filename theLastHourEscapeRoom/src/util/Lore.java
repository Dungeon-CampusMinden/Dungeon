package util;

import core.utils.Tuple;
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

  /** Short name of the scientist. */
  public static final String ScientistPortraitDrawable = "scientist_portrait";

  /** Email and password for the computer login. */
  public static final String LoginEmail = ScientistEmail;

  /** Password for the computer login. */
  public static final String LoginPassword = "a12b34xy";

  /** List of intro texts, each with a corresponding font sizes. */
  public static final List<Tuple<String, Integer>> IntroTexts =
      List.of(
          Tuple.of(
              """
        Dr. Elias Mertens, lead security researcher at Ciphera Labs, had been working on a highly
        confidential project over the past several weeks.

        His research was considered a technological breakthrough in digital security.""",
              32),
          Tuple.of(
              "Shortly before his disappearance, he reported unusual access attempts to his systems and suspicious contact requests from unknown sources online.",
              32),
          Tuple.of("72 hours ago, all communication with him suddenly stopped.", 32),
          Tuple.of(
              "His office was found ransacked...\n\nHis project may have attracted more attention than he realized.",
              32),
          Tuple.of("The Last Hour", 120));

  /** List of post intro dialog texts. */
  public static final List<String> PostIntroDialogTexts =
      List.of(
          """
        Your task is to reconstruct his final steps. Gain access to his laptop, investigate his communication, and uncover the clues he left behind.

        But be careful: not every piece of information can be trusted. Among helpful messages, there may be deliberate manipulation attempts.

        If you discover what Dr. Mertens was working on, you may come closer to understanding why he disappeared.""");

  /** List of outro texts, each with a corresponding font sizes. */
  public static final List<Tuple<String, Integer>> OutroTexts =
      List.of(
          Tuple.of(
              "This tunnel leads to the next room in the game, but it is the end of the demo!", 32),
          Tuple.of("Congratulations!\n\nYou escaped! :D", 120));

  /** List of blog entries, each with a title, content and a list of comments. */
  public static final List<BlogTab.BlogEntry> BlogEntries =
      List.of(
          new BlogTab.BlogEntry(
              "Unexpected Issue with the Access System",
              """
            Amid all the work, something rather frustrating happened today:
            I forgot the access code to the adjacent room.

            The door system is manufactured by SecuGate Systems, model SG-4. I usually secure access credentials in multiple ways, but apparently I've been too focused on the project lately.

            I would prefer not to fully reset or reconfigure the entire system.
            Is there a way to recover the existing code or reconstruct it in a controlled manner?

            If anyone has experience with this system or knows a secure approach, I would appreciate your advice.""",
              List.of()),
          new BlogTab.BlogEntry(
              "Many Responses - Mixed Impressions",
              """
              Thank you for the numerous replies to my previous post.
              I received significantly more emails than I expected.

              In some cases, I am not entirely sure whether the intention is truly to help - or something else.

              I will carefully review the incoming messages before opening or executing anything.""",
              List.of(
                  new BlogTab.BlogComment(
                      "TechAnalyst",
                      """
                In situations like this, I would focus less on the promised outcome and more on how the message is written.
                Professional technical support is usually neutral in tone and avoids dramatic language or unnecessary pressure.""",
                      60),
                  new BlogTab.BlogComment(
                      "SecureMind",
                      """
                If a message redirects you to external websites, I would examine them closely.
                Official manufacturers typically use consistent domains and avoid unusual or slightly altered web addresses.
                Checking the full URL can be very revealing.""",
                      120),
                  new BlogTab.BlogComment(
                      "CyberLab",
                      """
                Messages that create urgency, demand immediate action, or come from generic sender addresses are almost always problematic.
                The actual manufacturer will usually contact you professionally and through their official domain - not through improvised support addresses or private email accounts.""",
                      180))));

  /** The real access URL */
  public static final String RealAccessUrl = "https://secugate-support.com/sg4/access";

  /** List of URLs mentioned in the emails, which may or may not be trustworthy. */
  public static final List<String> EmailCodeUrls =
      List.of(
          "https://secugate-support.com/sg4/recovery-sequence",
          "http://secure-sg4-reset-now.com/verify",
          "http://quick-unlock-sg4.net/code",
          "https://sg4-analysis-tool.co/extract");

  /** List of emails, each with a sender, sender email, subject, content and list of attachments. */
  public static final List<EmailsTab.Email> EmailList =
      List.of(
          new EmailsTab.Email(
              "Andreas Keller",
              "andreas.keller@secugate-systems.com",
              "Re: SG-4 Access Code - Controlled Reconstruction",
              "Dear Dr. Mertens,\\pRegarding your inquiry about the SG-4 access system, I may be able to assist. The SG-4 model includes a recoverable access interface. I have prepared a test environment through which the code can be reconstructed in a controlled simulation.\\pPlease first access the following page:\\p\\ahttps://secugate-support.com/sg4/recovery-sequence;"
                  + EmailCodeUrls.get(0)
                  + "\\pThere you will find four binary blocks that must be translated into ASCII digits. Use the resulting number as the password for:\\p\\ahttps://secugate-support.com/sg4/access;"
                  + RealAccessUrl
                  + "\\pThere you will receive the final access code.\\pKind regards,\nAndreas Keller\nTechnical Support\nSecuGate Systems",
              List.of()),
          new EmailsTab.Email(
              "SecuGate Support",
              "support@secugate-reset247.com",
              "URGENT! Your Access Has Been Disabled",
              "Your SG-4 system has been temporarily locked for security reasons.\\pTo avoid permanent deactivation, immediate action is required.\\p\\ahttp://secure-sg4-reset-now.com/verify;"
                  + EmailCodeUrls.get(1),
              List.of()),
          new EmailsTab.Email(
              "Marc",
              "marc.unlockhelp@gmail.com",
              "About your door issue",
              "Hey Elias,\\pThe SG-4 thing is easy. Just check this out:\\p\\ahttp://quick-unlock-sg4.net/code;"
                  + EmailCodeUrls.get(2),
              List.of()),
          new EmailsTab.Email(
              "SG4 System Service",
              "system.service@sg4-security.co",
              "Automatic Code Extraction - SG-4",
              "\\ahttps://sg4-analysis-tool.co/extract;" + EmailCodeUrls.get(3),
              List.of()));

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
          "Trojan", "ESCAPE",
          "Ransomware", "ESCAPE",
          "Adware", "ESCAPE");

  /** File name of the access code document downloaded from the recovery portal. */
  public static final String AccessCodeDownloadFileName = "unlock_code.pdf";

  /**
   * The door code encoded in Morse, to be decoded by the player using the 2nd decryption manual.
   */
  public static final String DoorCodeMorse = "...-- --... ..... ---..";

  /** The actual numeric door code as a list of individual digits. */
  public static final List<Integer> DoorCode = List.of(3, 7, 5, 8);
}
