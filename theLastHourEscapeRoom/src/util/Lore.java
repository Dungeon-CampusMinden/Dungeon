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
          Tuple.of(
              "Your intrusion to his office triggered an alarm and locked the door behind you.\n\nA timer on the wall shows: You have 20 minutes until something happens...",
              32),
          Tuple.of("The Last Hour", 120));

  /** First post intro dialog. */
  public static final String PostIntroDialogText1 =
      """
        Your task is to reconstruct Dr. Mertens' final steps. Gain access to his PC, investigate his communication, and uncover the clues he left behind.

        But be careful: not every piece of information can be trusted. Among helpful messages, there may be deliberate manipulation attempts.

        If you discover what he was working on, you may come closer to understanding why he disappeared.""";

  /** 2nd intro dialog. */
  public static final String PostIntroDialogText2 =
      """
        Controls:

        Move -> WASD
        Interact -> E
        Pause/Settings -> P
        Close Dialog -> <ESC>""";

  /** List of outro texts, each with a corresponding font sizes. */
  public static final List<Tuple<String, Integer>> OutroTexts =
      List.of(
          Tuple.of(
              "This tunnel leads onward to the next room, but this is where the demo ends for now.",
              32),
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
                      180))),
          new BlogTab.BlogEntry(
              "Some Kind of Decoding",
              """
          Ok I did manage to contact the support, but they instructed me to use some kind of decoding process on a sequence of numbers they provided, which is supposed to lead me to the access code.

          The kicker: I forgot where I placed the manual that explains the decoding steps.

          Does anyone happen to know how to continue with this?""",
              List.of(
                  new BlogTab.BlogComment(
                      "BinaryCoffee",
                      """
              I remember we briefly talked about that sequence in the canteen the other day.
              At the time I couldn’t make sense of it either.

              Thinking about it again now, one detail came back to me: the numbers were grouped in blocks of eight, and you mentioned that they were all ones and zeros, akin to binary.

              Not quite sure how to go on, but I am fairly sure the grouping itself was important for the next step.""",
                      240),
                  new BlogTab.BlogComment(
                      "BinaryCoffee",
                      """
              It just clicked while reviewing some old documentation.

              If those values are indeed binary and arranged in groups of four bits, the usual interpretation would be to convert each group into its hexadecimal equivalent.

              In other words: every 4-bit binary block directly maps to a single hexadecimal digit.""",
                      300),
                  new BlogTab.BlogComment(
                      "BinaryCoffee",
                      """
              I checked my notes again and this should be the full chain you mentioned back then.

              First interpret the sequence as binary and group it into blocks of four bits.
              Convert each 4-bit block into its hexadecimal representation.

              After that, take the resulting hexadecimal values and interpret them as ASCII codes.
              That final ASCII conversion should reveal the numeric code you were trying to recover.""",
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
              "Confidential Assistance Required",
              "Greetings,\\pMy name is Prince Adewale. Due to a sensitive financial situation, I require assistance transferring 2.14 million USD from a restricted account.\\pIn return for your trustworthy support, you will receive a generous percentage.\\p\\aProvide secure contact details;https://royal-transferdesk.org/secure",
              List.of("Sign_This_To_Receive_Funds.pdf")),
          new EmailsTab.Email(
              "SecuGate Support",
              "support@secugate-reset247.com",
              "URGENT! Your Access Has Been Disabled",
              "Your SG-4 system has been temporarily locked for security reasons.\\pTo avoid permanent deactivation, immediate action is required.\\p\\aSecuGate Access Recovery Portal;"
                  + EmailCodeUrls.get(1)
                  + "\\pFollow the instructions on the portal to restore access.\\pThis is an automated message, please do not reply.",
              List.of()),
          new EmailsTab.Email(
              "Music Downloader",
              "noreply@illegal-music-downloader.com",
              "Your File Is Ready",
              "Hello,\\pYour requested file is now ready.\\pCheck the attachments or click the link below to download your file:\\p\\aDownload your file;https://illegal-music-downloader.com/download/12345\\pPlease note that this is a one-time download link and will expire in 24 hours.",
              List.of("Linkin_Park_-_In_The_End.mp3.exe")),
          new EmailsTab.Email(
              "Andreas Keller",
              "andreas.keller@secugate.com",
              "Re: SG-4 Access Code - Controlled Reconstruction",
              "Dear Dr. Mertens,\\pRegarding your inquiry about the SG-4 access system, I may be able to assist. The SG-4 model includes a recoverable access interface. I have prepared a test environment through which the code can be reconstructed in a controlled simulation.\\pPlease first access the following page:\\p\\aSecuGate Support - Access Recovery;"
                  + EmailCodeUrls.get(0)
                  + "\\pThere you will receive the final access code.\\pKind regards,\nAndreas Keller\nTechnical Support\nSecuGate Systems",
              List.of()),
          new EmailsTab.Email(
              "Marc",
              "marc.unlockhelp@gmail.com",
              "About your door issue",
              "Hey Elias,\\pThe SG-4 thing is easy. Just check this out:\\p\\aSG-4 Diagnostic Access Tool;"
                  + EmailCodeUrls.get(2)
                  + "\\pKind regards,\nMarc\nFreelance IT Specialist",
              List.of()),
          new EmailsTab.Email(
              "Telekom",
              "contact@local-connections-now.net",
              ">> Hot Single Moms in Your Area <<",
              "Hello,\\pOur system indicates that several friendly people in your area are interested in getting to know you.\\pCreate a profile now to see who is nearby.\\p\\aView nearby connections;https://local-connections-now.net/start\\pMembership is free for a limited time. Don't miss out!",
              List.of()),
          new EmailsTab.Email(
              "SG4 Recovery System",
              "system@secugate.com",
              "Automatic Code Extraction - SG-4",
              "Dear Dr. Mertens,\\pFollowing your inquiry regarding the SG-4 access control system, an automated extraction routine can be used to recover the stored access parameters.\\p"
                  + "Please initiate the diagnostic process through the following interface:\\p\\aSG-4 Diagnostic Interface;"
                  + EmailCodeUrls.get(3),
              List.of()),
          new EmailsTab.Email(
              "Global Parcel Logistics",
              "tracking@parcel-hold-center.info",
              "Package Delivery Failed - Immediate Confirmation Required",
              "Dear Customer,\\pA shipment addressed to you could not be delivered due to missing confirmation.\\pFailure to respond within 24 hours may result in return or storage fees.\\p\\aConfirm delivery details;https://adq.mmcaok.com/pl10fonmxdm1asmokxx0",
              List.of("Tracking Details.pdf")),
          new EmailsTab.Email(
              "CryptoGrowth Alerts",
              "alerts@cryptogrowth-daily.biz",
              "Your account qualified for guaranteed returns",
              "Investor Notice,\\pOur automated trading platform has identified you as eligible for a limited high-yield investment opportunity with guaranteed daily profit.\\pSpaces are extremely limited.\\p\\aActivate investment account;https://cryptogrowth-daily.biz/start?si=1ujg0h1ju8mnc980mumsdnuz0",
              List.of()));

  /** List of attachment file names that lead to viruses. */
  public static final List<String> VirusAttachmentNames =
      List.of("Tracking Details.pdf", "Linkin_Park_-_In_The_End.mp3.exe");

  /** List of URLs that lead to viruses. */
  public static final List<String> VirusWebsites =
      List.of(
          "https://illegal-music-downloader.com/download/12345",
          "https://adq.mmcaok.com/pl10fonmxdm1asmokxx0",
          "https://local-connections-now.net/start",
          "https://royal-transferdesk.org/secure",
          "https://cryptogrowth-daily.biz/start?si=1ujg0h1ju8mnc980mumsdnuz0");

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

  /** File name of the access code document downloaded from the recovery portal. */
  public static final String AccessCodeDownloadFileName = "unlock_code.pdf";

  /**
   * The door code encoded in Morse, to be decoded by the player using the 2nd decryption manual.
   */
  public static final String DoorCodeMorse = "...-- --... ..... ---..";

  /** The actual numeric door code as a list of individual digits. */
  public static final List<Integer> DoorCode = List.of(3, 7, 5, 8);
}
