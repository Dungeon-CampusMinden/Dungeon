package util;

import contrib.achivements.AchievementSystem;
import contrib.components.InventoryComponent;
import core.Entity;
import core.Game;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import modules.usbstick.UsbStickColor;
import modules.usbstick.UsbStickItem;

/** Achievement IDs and trigger helpers for The Last Hour. */
public final class LastHourAchievements {

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

  /**
   * Unlocks a global achievement for every player.
   *
   * @param name achievement id/name
   */
  public static void popForAll(String name) {
    AchievementSystem.instance().pop(name);
  }

  /**
   * Unlocks Bruteforce after enough wrong keypad submissions.
   *
   * @param wrongCodeAttempts wrong submit count
   */
  public static void checkBruteforce(int wrongCodeAttempts) {
    if (wrongCodeAttempts >= BRUTEFORCE_ATTEMPTS) {
      popForAll(BRUTEFORCE);
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
      popForAll(USB_COLLECTOR);
    }
  }

  /**
   * Checks whether the given player carries all four USB stick colors.
   *
   * @param player player to inspect
   */
  public static void checkAllUsbSticks(Entity player) {
    if (player == null) {
      return;
    }
    player.fetch(InventoryComponent.class).ifPresent(LastHourAchievements::checkAllUsbSticks);
  }

  private static void checkAllUsbSticks(InventoryComponent inventory) {
    Set<UsbStickColor> colors = EnumSet.noneOf(UsbStickColor.class);
    addUsbStickColors(inventory, colors);
    if (colors.size() == UsbStickColor.values().length) {
      popForAll(USB_COLLECTOR);
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
