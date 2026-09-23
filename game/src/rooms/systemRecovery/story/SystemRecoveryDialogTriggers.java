package rooms.systemRecovery.story;

import java.util.List;

/**
 * Defines the world positions that start the first instruction for each room.
 *
 * <p>The points intentionally have their own names instead of reusing door points. This lets the
 * level designer move a dialog trigger independently from the door in the level editor. A trigger
 * is consumed once per player; this keeps the behavior correct in multiplayer while preventing a
 * dialog from reopening on every tick.
 */
public final class SystemRecoveryDialogTriggers {

  /** Trigger point used when entering the module-storage room. */
  public static final String MODULE_STORAGE = "dialog_trigger_module_storage";

  /** Trigger point used when entering the inventory-scanner room. */
  public static final String INVENTORY_SCANNER = "dialog_trigger_inventory_scanner";

  /** Trigger point used when entering the transport-storage room. */
  public static final String TRANSPORT_STORAGE = "dialog_trigger_transport_storage";

  /** Trigger point used inside the manual-sorting room. */
  public static final String MANUAL_SORTING = "dialog_trigger_manual_sorting";

  /** Trigger point used when entering the data-archive room. */
  public static final String DATA_ARCHIVE = "dialog_trigger_data_archive";

  /** Trigger point used when entering the two-dimensional-storage room. */
  public static final String TWO_DIMENSIONAL_STORAGE = "dialog_trigger_two_dimensional_storage";

  /** Trigger point used when entering the system-core room. */
  public static final String SYSTEM_CORE = "dialog_trigger_system_core";

  /** All room-entry triggers in progression order. */
  public static final List<DialogTrigger> ROOM_ENTRY =
      List.of(
          new DialogTrigger(MODULE_STORAGE, SystemRecoveryStoryDialogs.MODULE_ARRAY),
          new DialogTrigger(INVENTORY_SCANNER, SystemRecoveryStoryDialogs.SCANNER_CODE),
          new DialogTrigger(TRANSPORT_STORAGE, SystemRecoveryStoryDialogs.PACKAGES_ARRAY),
          new DialogTrigger(MANUAL_SORTING, SystemRecoveryStoryDialogs.MANUAL_SORTING),
          new DialogTrigger(DATA_ARCHIVE, SystemRecoveryStoryDialogs.ARCHIVE_ARRAYS),
          new DialogTrigger(TWO_DIMENSIONAL_STORAGE, SystemRecoveryStoryDialogs.STORAGE_ARRAY),
          new DialogTrigger(SYSTEM_CORE, SystemRecoveryStoryDialogs.CENTRAL_SORT));

  private SystemRecoveryDialogTriggers() {}

  /**
   * One movable world trigger and the story instruction it starts.
   *
   * @param pointName custom-point name of the trigger
   * @param step story instruction started by the trigger
   */
  public record DialogTrigger(String pointName, SystemRecoveryStoryDialogs.StoryStep step) {}
}
