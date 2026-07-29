package util;

import contrib.questlog.QuestLogUtil;
import core.Game;
import core.language.Translation;
import java.util.HashSet;
import java.util.Set;

/** Utility methods for Last Hour specific quest log setup and entries. */
public final class LastHourQuestLogUtil {

  private static final Translation QUESTLOG_ENTRIES = new Translation("questlog");
  private static final String T_MAIN_TAB = "main.tab";
  private static final String T_MAIN_LOCKED_IN = "main.entries.locked_in";
  private static final String T_MAIN_RESTORE_POWER = "main.entries.restore_power";
  private static final String T_MAIN_INVESTIGATE_PC = "main.entries.investigate_pc";
  private static final String T_INVESTIGATION_TAB = "investigation.tab";
  private static final String T_INVESTIGATION_MERTENS_MISSING =
      "investigation.entries.mertens_missing";
  private static final String T_INVESTIGATION_OFFICE_RANSACKED =
      "investigation.entries.office_ransacked";
  private static final String T_INVESTIGATION_TRUST_CAREFULLY =
      "investigation.entries.trust_carefully";
  private static final String T_COMPUTER_TAB = "computer.tab";
  private static final String T_COMPUTER_POWER_SWITCH = "computer.entries.power_switch";
  private static final String T_COMPUTER_LOGIN_NEEDED = "computer.entries.login_needed";
  private static final String T_COMPUTER_MAIL_REVIEW = "computer.entries.mail_review";
  private static final String T_COMPUTER_VIRUS_WARNING = "computer.entries.virus_warning";
  private static final String T_COMPUTER_CONTROL_PANEL = "computer.entries.control_panel";
  private static final String T_CLUES_TAB = "clues.tab";
  private static final String T_CLUES_DOOR_CODE = "clues.entries.door_code";
  private static final String T_CLUES_TRASH_NOTE = "clues.entries.trash_note";
  private static final String T_CLUES_DECODER_SHELVES = "clues.entries.decoder_shelves";
  private static final String T_CLUES_PROFILE_PAPER = "clues.entries.profile_paper";
  private static final String T_CLUES_FINAL_CODE = "clues.entries.final_code";
  private static final String T_USB_TAB = "usb.tab";
  private static final String T_USB_SEARCH = "usb.entries.search";
  private static final String T_USB_COLOR_HINT = "usb.entries.color_hint";
  private static final String T_USB_USE_ON_PC = "usb.entries.use_on_pc";
  private static final String T_USB_RECOVER_DATA = "usb.entries.recover_data";

  private static final Set<String> ENTRY_KEYS_ADDED = new HashSet<>();

  private LastHourQuestLogUtil() {}

  /** Initializes the shared quest log and adds the first Last Hour entry. */
  public static void initializeQuestLog() {
    ENTRY_KEYS_ADDED.clear();
    Game.add(QuestLogUtil.initServerQuestLog());
    addQuestLogEntryOnce(T_MAIN_TAB, T_MAIN_LOCKED_IN);
  }

  /** Adds the storage door code observation after it has been entered successfully. */
  public static void addDoorCodeQuestLogEntry() {
    addQuestLogEntryOnce(T_CLUES_TAB, T_CLUES_DOOR_CODE);
  }

  /** Adds the intro investigation observations after the intro starts. */
  public static void addIntroInvestigationQuestLogEntries() {
    addQuestLogEntryOnce(T_INVESTIGATION_TAB, T_INVESTIGATION_MERTENS_MISSING);
    addQuestLogEntryOnce(T_INVESTIGATION_TAB, T_INVESTIGATION_OFFICE_RANSACKED);
  }

  /** Adds the power restoration observation after players notice that the PC has no power. */
  public static void addRestorePowerQuestLogEntry() {
    addQuestLogEntryOnce(T_MAIN_TAB, T_MAIN_RESTORE_POWER);
  }

  /** Adds the PC investigation entry after power has been restored. */
  public static void addInvestigatePcQuestLogEntry() {
    addQuestLogEntryOnce(T_MAIN_TAB, T_MAIN_INVESTIGATE_PC);
  }

  /** Adds the hidden-switch observation after the switch has been used. */
  public static void addPowerSwitchQuestLogEntry() {
    addQuestLogEntryOnce(T_COMPUTER_TAB, T_COMPUTER_POWER_SWITCH);
  }

  /** Adds the login-needed observation after the powered PC is opened while still locked. */
  public static void addLoginNeededQuestLogEntry() {
    addQuestLogEntryOnce(T_COMPUTER_TAB, T_COMPUTER_LOGIN_NEEDED);
  }

  /** Adds the computer communication observation after a successful login. */
  public static void addMailReviewQuestLogEntry() {
    addQuestLogEntryOnce(T_COMPUTER_TAB, T_COMPUTER_MAIL_REVIEW);
  }

  /** Adds the suspicious-file observation after the PC is infected. */
  public static void addVirusWarningQuestLogEntry() {
    addQuestLogEntryOnce(T_COMPUTER_TAB, T_COMPUTER_VIRUS_WARNING);
    addQuestLogEntryOnce(T_INVESTIGATION_TAB, T_INVESTIGATION_TRUST_CAREFULLY);
  }

  /** Adds the suspicious-source observation after a fitting clue has been found. */
  public static void addTrustCarefullyQuestLogEntry() {
    addQuestLogEntryOnce(T_INVESTIGATION_TAB, T_INVESTIGATION_TRUST_CAREFULLY);
  }

  /** Adds the profile-paper observation after the paper has been inspected. */
  public static void addProfilePaperQuestLogEntry() {
    addQuestLogEntryOnce(T_CLUES_TAB, T_CLUES_PROFILE_PAPER);
  }

  /** Adds the trash note observation after the note has been recovered. */
  public static void addTrashNoteQuestLogEntry() {
    addQuestLogEntryOnce(T_CLUES_TAB, T_CLUES_TRASH_NOTE);
  }

  /** Adds the decoder shelves observation after a decoder shelf has been inspected. */
  public static void addDecoderShelvesQuestLogEntry() {
    addQuestLogEntryOnce(T_CLUES_TAB, T_CLUES_DECODER_SHELVES);
  }

  /** Adds the final code observation after the paper puzzle has been solved. */
  public static void addFinalCodeQuestLogEntry() {
    addQuestLogEntryOnce(T_CLUES_TAB, T_CLUES_FINAL_CODE);
  }

  /** Adds the USB color hint observation after the note has been read. */
  public static void addUsbColorHintQuestLogEntry() {
    addQuestLogEntryOnce(T_USB_TAB, T_USB_COLOR_HINT);
  }

  /** Adds the USB mounted observation after a stick is inserted into the PC. */
  public static void addUsbUsedQuestLogEntry() {
    addQuestLogEntryOnce(T_USB_TAB, T_USB_USE_ON_PC);
  }

  /** Adds the recovered-data observation after the correct USB stick is mounted. */
  public static void addUsbRecoveredDataQuestLogEntry() {
    addQuestLogEntryOnce(T_USB_TAB, T_USB_RECOVER_DATA);
    addQuestLogEntryOnce(T_COMPUTER_TAB, T_COMPUTER_CONTROL_PANEL);
  }

  /** Adds the USB search observation after the blue USB stick has been found. */
  public static void addUsbSearchQuestLogEntry() {
    addQuestLogEntryOnce(T_USB_TAB, T_USB_SEARCH);
  }

  private static void addQuestLogEntryOnce(String tabKey, String entryKey) {
    String id = tabKey + "." + entryKey;
    if (ENTRY_KEYS_ADDED.contains(id)) {
      return;
    }

    if (QuestLogUtil.add(QUESTLOG_ENTRIES.text(tabKey), QUESTLOG_ENTRIES.text(entryKey))) {
      ENTRY_KEYS_ADDED.add(id);
    }
  }
}
