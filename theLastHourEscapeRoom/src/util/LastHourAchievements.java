package util;

import contrib.achievements.AchievementManager;
import contrib.components.InventoryComponent;
import core.Entity;
import core.Game;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import modules.computer.ComputerFactory;
import modules.usbstick.UsbStickColor;
import modules.usbstick.UsbStickItem;

/** Achievement IDs and trigger helpers for The Last Hour. */
public final class LastHourAchievements {

  public static final String DEFINITION_PATH = "achievement.json";
  private static final String STATUS_PATH = "the-last-hour-achievement-unlock.json";

  public static final String LIGHTS_ON = "Lights On";
  public static final String TRASH_DIVER = "Trash Diver";
  public static final String PC_UNLOCKED = "PC Unlocked";
  public static final String VIRUS = "Virus";
  public static final String KEYPAD_CODE = "Keypad Code";
  public static final String BRUTEFORCE = "Bruteforce";
  public static final String USB_COLLECTOR = "USB Collector";
  public static final String CONTROL_PANEL = "Control Panel";
  public static final String ESCAPED_IN_TIME = "Escaped In Time";
  public static final String ESCAPED_TOO_LATE = "Escaped Too Late";

  private static final int BRUTEFORCE_ATTEMPTS = 10;

  private LastHourAchievements() {}

  /** Registers The Last Hour achievement definitions and event hooks. */
  public static void register() {
    AchievementManager.registerAchievements(DEFINITION_PATH, STATUS_PATH);
    registerComputerHooks();
  }

  /** Registers achievement hooks for Last Hour computer interactions. */
  public static void registerComputerHooks() {
    ComputerFactory.onVirusTriggered(LastHourAchievements::triggerVirus);
    ComputerFactory.onPcUnlocked(LastHourAchievements::triggerPcUnlocked);
    ComputerFactory.onControlPanelOpened(LastHourAchievements::triggerControlPanel);
  }

  /**
   * Triggers an achievement that does not have a specific acting player.
   *
   * @param name achievement id/name
   */
  public static void trigger(String name) {
    AchievementManager.instance().pop(name);
  }

  /**
   * Triggers an achievement for an acting player.
   *
   * <p>The achievement JSON decides whether this unlock is sent to all players or only to the
   * acting player.
   *
   * @param player player who caused the achievement trigger
   * @param name achievement id/name
   */
  public static void trigger(Entity player, String name) {
    AchievementManager.instance().popFor(player, name);
  }

  /**
   * Unlocks the trashcan-search achievement for the acting player.
   *
   * @param player player who opened a Last Hour trashcan
   */
  public static void triggerTrashDiver(Entity player) {
    trigger(player, TRASH_DIVER);
  }

  /**
   * Unlocks the virus achievement for the acting player.
   *
   * @param player player who caused the virus
   */
  public static void triggerVirus(Entity player) {
    trigger(player, VIRUS);
  }

  /**
   * Unlocks the PC achievement for the acting player.
   *
   * @param player player who logged in
   */
  public static void triggerPcUnlocked(Entity player) {
    trigger(player, PC_UNLOCKED);
  }

  /**
   * Unlocks the control-panel achievement for the acting player.
   *
   * @param player player who opened the control panel
   */
  public static void triggerControlPanel(Entity player) {
    trigger(player, CONTROL_PANEL);
  }

  /**
   * Unlocks Bruteforce after enough wrong keypad submissions.
   *
   * @param player player who submitted the wrong code
   * @param wrongCodeAttempts wrong submit count
   */
  public static void checkBruteforce(Entity player, int wrongCodeAttempts) {
    if (wrongCodeAttempts >= BRUTEFORCE_ATTEMPTS) {
      trigger(player, BRUTEFORCE);
    }
  }

  /** Checks whether all players together carry all four USB stick colors. */
  public static void checkAllUsbSticks() {
    Set<UsbStickColor> colors = EnumSet.noneOf(UsbStickColor.class);
    Game.allPlayers()
        .forEach(
            player ->
                player
                    .fetch(InventoryComponent.class)
                    .ifPresent(inventory -> addUsbStickColors(inventory, colors)));
    if (colors.size() == UsbStickColor.values().length) {
      trigger(USB_COLLECTOR);
    }
  }

  private static void addUsbStickColors(InventoryComponent inventory, Set<UsbStickColor> colors) {
    Arrays.stream(inventory.items())
        .filter(UsbStickItem.BaseUsbStick.class::isInstance)
        .map(UsbStickItem.BaseUsbStick.class::cast)
        .map(UsbStickItem.BaseUsbStick::color)
        .forEach(colors::add);
  }
}
