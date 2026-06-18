package util;

import com.badlogic.gdx.Input;
import contrib.configuration.KeyboardConfig;
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
              "Your intrusion to his office triggered an alarm and locked the door behind you.\n\nA timer on the wall shows: You have 60 minutes until something happens...",
              32),
          Tuple.of("The Last Hour", 120));

  private static final String MertensColor = "#aa00aa";

  /**
   * Dialog played as a recording of Dr. Mertens through the office security system speakers when
   * the world timer expires.
   */
  public static final String TimerExpiredRecording =
      "[speaker img=images/scientist_portrait.png name=\"[color="
          + MertensColor
          + "]Dr. Mertens (Recording)\"]"
          + "[tr speed=1.0]This is an automated message from the security system of [color="
          + MertensColor
          + "]Dr. Elias Mertens[/color]."
          + "[p][tr speed=1.0]If you are hearing this, the time has run out.[n][pause=0.5]"
          + "As a final safeguard, all of my data is being [color=#aa0000][shake strength=0.4 speed=0.5]automatically destroyed[/shake][/color] right now."
          + "[p]Whatever you did not manage to recover[tr speed=0.1]...[tr speed=0.4] is gone for good now.";

  /** First post intro dialog. */
  public static final String PostIntroDialogText1 =
      """
        Your task is to reconstruct [color=#aa00aa]Dr. Mertens'[/color] final steps.[n][pause=0.5]Gain access to his PC, investigate his communication, and uncover the clues he left behind.
        [p]
        But be careful: not every piece of information can be trusted.[n][pause=0.5]Among helpful messages, there may be deliberate [color=#aa0000]manipulation attempts[/color].
        [p]
        If you discover what he was working on, you may come closer to understanding why he[tr speed=0.1]...[pause=0.5][tr speed=0.3][shake strength=0.4 speed=0.5][color=#880000] disappeared[/color][/shake].""";

  /** 2nd intro dialog. */
  public static final String PostIntroDialogText2 =
      "[size=24]Controls:\n\n"
          + "Move -> "
          + "[key code="
          + core.configuration.KeyboardConfig.MOVEMENT_UP.value()
          + "]"
          + "[key code="
          + core.configuration.KeyboardConfig.MOVEMENT_LEFT.value()
          + "]"
          + "[key code="
          + core.configuration.KeyboardConfig.MOVEMENT_DOWN.value()
          + "]"
          + "[key code="
          + core.configuration.KeyboardConfig.MOVEMENT_RIGHT.value()
          + "]\n"
          + "Interact -> "
          + "[key code="
          + KeyboardConfig.INTERACT_WORLD.value()
          + "]"
          + " / "
          + "[key code="
          + Input.Buttons.LEFT
          + " type=mouse]\n"
          + "Inventory -> "
          + "[key code="
          + KeyboardConfig.INVENTORY_OPEN.value()
          + "]\n"
          + "Close Dialog -> "
          + "[key code="
          + KeyboardConfig.CLOSE_UI.value()
          + "]\n"
          + "Settings -> "
          + "[key code="
          + KeyboardConfig.PAUSE_MENU.value()
          + "]\n"
          + "[size=18][color=#888888]You can look the controls up in the Settings.[/color]\n\n"
          + "[size=24][align=center]Use the mouse to find things to interact with!";

  /** Note found on the writing desk in room 2. */
  public static final String R2DeskNoteText =
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
          + " tomorrow!";

  /** List of outro texts, each with a corresponding font sizes. */
  public static final List<Tuple<String, Integer>> OutroTexts =
      List.of(
          Tuple.of(
              "The exit door of Dr. Mertens' office finally clicks open - but his last message still echoes in your mind:",
              32),
          Tuple.of(
              "\"The project files are hidden in a locker on the 6th floor.\"\n\n"
                  + "You step out into the silent hallway, the timer behind you finally still.",
              32),
          Tuple.of(
              "With the evidence secured and the truth about Dr. Mertens' disappearance"
                  + " in your hands, you make your way up to recover the project files.",
              32),
          Tuple.of(
              "As you reach the locker and open it, you find a final note from Dr. Mertens:\n\n"
                  + "\"If you're reading this, it means you made it out. The project is safe with you now.\"\n\n",
              32),
          Tuple.of(
              "A sense of relief washes over you, but also a lingering question: What exactly is \"Mythos\", and why did it attract such dangerous attention?\n\nYou should google it...",
              32),
          Tuple.of("Congratulations!\n\nYou escaped! :D", 120));

  /** List of outro texts shown when the timer expires before escape. */
  public static final List<Tuple<String, Integer>> BadOutroTexts =
      List.of(
          Tuple.of(
              "The office door finally unlocks, but the speakers are silent now - Dr. Mertens' automated warning already said everything:",
              32),
          Tuple.of(
              "\"If you are hearing this, the time has run out.\"\n\nYou step into the hallway with empty hands, knowing the destruction protocol has already erased his data.",
              32),
          Tuple.of(
              "You made it out alive, but the evidence is gone. Whatever Dr. Mertens discovered about \"Mythos\" died with his files.",
              32),
          Tuple.of(
              "At the locker on the 6th floor, you find only scorched fragments and a final line in his handwriting:\n\n\"If this reaches you too late, protect the people - not the project.\"\n\n",
              32),
          Tuple.of(
              "No breakthrough to recover. No proof to hand over. Only unanswered questions, and the certainty that someone wanted this truth buried.",
              32),
          Tuple.of("You escaped...", 120));

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
              At the time I couldn't make sense of it either.

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
