package rooms.lasthour.util;

import engine.Entity;
import engine.Game;
import feature.achievements.AchievementManager;
import feature.components.InventoryComponent;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import rooms.lasthour.modules.computer.ComputerFactory;
import rooms.lasthour.modules.usbstick.UsbStickColor;
import rooms.lasthour.modules.usbstick.UsbStickItem;

/** Achievement IDs and trigger helpers for The Last Hour. */
public final class LastHourAchievements {

  public static final String DEFINITION_PATH = "achievement.json";
  private static final String STATUS_PATH = "the-last-hour-achievement-unlock.json";

  public static final String LIGHTS_ON = "lights_on";
  public static final String TRASH_DIVER = "trash_diver";
  public static final String PC_UNLOCKED = "pc_unlocked";
  public static final String VIRUS = "virus";
  public static final String KEYPAD_CODE = "keypad_code";
  public static final String BRUTEFORCE = "bruteforce";
  public static final String USB_COLLECTOR = "usb_collector";
  public static final String CONTROL_PANEL = "control_panel";
  public static final String ESCAPED_IN_TIME = "escaped_in_time";
  public static final String ESCAPED_TOO_LATE = "escaped_too_late";

  private static final int BRUTEFORCE_ATTEMPTS = 10;

  private LastHourAchievements() {}

  /** Registers The Last Hour achievement definitions and event hooks. */
  public static void register() {
    AchievementManager.registerAchievements(DEFINITION_PATH, STATUS_PATH);
    registerComputerHooks();
  }

  /** Registers achievement hooks for Last Hour computer interactions. */
  public static void registerComputerHooks() {
    ComputerFactory.onVirusTriggered(player -> trigger(player, VIRUS));
    ComputerFactory.onPcUnlocked(player -> trigger(player, PC_UNLOCKED));
    ComputerFactory.onControlPanelOpened(player -> trigger(player, CONTROL_PANEL));
  }

  /**
   * Triggers an achievement that does not have a specific acting player.
   *
   * @param id achievement id
   */
  public static void trigger(String id) {
    AchievementManager.instance().pop(id);
  }

  /**
   * Triggers an achievement for an acting player.
   *
   * <p>The achievement JSON decides whether this unlock is sent to all players or only to the
   * acting player.
   *
   * @param player player who caused the achievement trigger
   * @param id achievement id
   */
  public static void trigger(Entity player, String id) {
    AchievementManager.instance().popFor(player, id);
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
