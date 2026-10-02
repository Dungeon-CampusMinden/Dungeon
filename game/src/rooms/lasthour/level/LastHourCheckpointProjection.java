package rooms.lasthour.level;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PlayerComponent;
import engine.network.messages.s2c.ItemState;
import feature.components.InventoryComponent;
import feature.components.ItemComponent;
import feature.entities.WorldItemBuilder;
import feature.interaction.keypad.KeypadComponent;
import feature.inventory.Item;
import feature.inventory.items.HintItem;
import feature.systems.EventScheduler;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import rooms.lasthour.modules.computer.ComputerCallbacks;
import rooms.lasthour.modules.computer.ComputerProgress;
import rooms.lasthour.modules.computer.ComputerStateComponent;
import rooms.lasthour.modules.computer.LastHourBlogTime;
import rooms.lasthour.modules.computer.content.BlogTab;
import rooms.lasthour.petrinet.LastHourMilestone;
import rooms.lasthour.save.LastHourSave;
import rooms.lasthour.util.Lore;
import rooms.lasthour.util.translation.TranslationKey;

/** Projects a validated save onto The Last Hour room entities after their setup is complete. */
final class LastHourCheckpointProjection {

  private LastHourCheckpointProjection() {}

  /**
   * Restores physical room and player state without replaying gameplay callbacks.
   *
   * @param level level whose entities are being restored
   * @param data validated saved state to project into the level
   */
  static void apply(LastHourLevel level, LastHourSave.SaveData data) {
    ComputerStateComponent restoredComputer = data.computer();
    if (restoredComputer.timestampOfLogin() > 0) {
      int nowSeconds = (int) (System.currentTimeMillis() / 1000L);
      restoredComputer =
          restoredComputer.withTimestampOfLogin(
              LastHourBlogTime.rebasedLoginTimestamp(data.blogElapsedSeconds(), nowSeconds));
    }
    Entity stateEntity =
        Game.levelEntities(Set.of(ComputerStateComponent.class)).findFirst().orElseThrow();
    stateEntity.remove(ComputerStateComponent.class);
    stateEntity.add(restoredComputer);
    level.cscLastTick = restoredComputer;
    level.lastKnownVisibleCommentCount = BlogTab.countVisibleComments();
    restoreComputerPresentation(level, restoredComputer, data);

    level
        .keypad
        .fetch(KeypadComponent.class)
        .ifPresent(
            component -> {
              component.isUnlocked(data.keypadUnlocked());
              component.restoreWrongCodeAttempts(data.wrongCodeAttempts());
            });
    if (data.storageDoorOpen()) level.storageDoor.open();
    else level.storageDoor.close();
    if (restoredComputer.door2Open()) level.exitDoor.open();
    else level.exitDoor.close();
    LastHourLevel.timerExpired = data.timerExpired();
    level.codeAssembled = data.milestones().contains(LastHourMilestone.CODE_ASSEMBLED);
    level.escaped = data.milestones().contains(LastHourMilestone.ESCAPED);

    if (data.milestones().contains(LastHourMilestone.PAPERS_SPAWNED) && !level.codeAssembled) {
      level.createPaperPuzzle(false);
    } else if (level.codeAssembled
        && !containsPlayerItem(
            data.players(),
            "HintItem",
            "imagePath",
            LastHourLevel.R2_PUZZLE_IMAGE_EN.pathString())) {
      Game.add(
          WorldItemBuilder.buildWorldItem(
              new HintItem(LastHourLevel.R2_PUZZLE_IMAGE_EN, TranslationKey.R2PuzzleImage),
              level.worldPoint("r2-vent")));
    }

    level.pendingPlayers.addAll(data.players());
    restorePhone(level, data.phone());
    removeItemsAlreadyHeld(data.players());
    level.trashNoteAwarded =
        containsPlayerItem(data.players(), "HintItem", "imagePath", LastHourLevel.trashNote);
    level.blueTrashAwarded =
        data.milestones().contains(LastHourMilestone.BLUE_USB_INSERTED)
            || containsPlayerItem(data.players(), "BlueUsbStick", "", "");
    level.lastSaved = data;
  }

  private static void restoreComputerPresentation(
      LastHourLevel level, ComputerStateComponent computer, LastHourSave.SaveData data) {
    if (computer.isInfected() && Lore.UnknownDeviceVirusType.equals(computer.virusType())) {
      ComputerCallbacks.scheduleUnknownDeviceShutdown(
          data.unknownDeviceShutdownRemainingMs() < 0
              ? ComputerCallbacks.unknownDeviceShutdownDefaultDelayMs()
              : data.unknownDeviceShutdownRemainingMs());
    }
    if (computer.state() != ComputerProgress.OFF || computer.isInfected()) {
      boolean infected = computer.isInfected();
      level
          .pc
          .fetch(DrawComponent.class)
          .ifPresent(
              draw -> {
                draw.sendSignal(LastHourLevel.PC_SIGNAL_ON);
                if (infected) draw.sendSignal(LastHourLevel.PC_SIGNAL_INFECT);
              });
    }
  }

  private static void restorePhone(LastHourLevel level, LastHourSave.PhoneData data) {
    level.firstPhoneCallTriggered = data.firstTriggered();
    level.secondPhoneCallScheduled = data.secondScheduled();
    level.isPhoneRinging = data.ringing();
    level.ringingPhoneDialog = data.dialog();
    level.onCurrentPhoneCallResolved =
        TranslationKey.Ringing1.equals(level.ringingPhoneDialog)
            ? level::scheduleSecondPhoneCall
            : null;
    if (level.isPhoneRinging) level.showRingingPhoneEmote();
    if (data.firstDelayMs() >= 0 && !level.firstPhoneCallTriggered) {
      level.scheduleFirstPhoneCall(data.firstDelayMs());
    }
    if (data.secondDelayMs() >= 0 && level.secondPhoneCallScheduled && !level.isPhoneRinging) {
      level.secondPhoneRingAt = System.currentTimeMillis() + data.secondDelayMs();
      EventScheduler.scheduleAction(
          () -> {
            level.secondPhoneRingAt = -1;
            level.ringPhone(TranslationKey.Ringing2);
          },
          data.secondDelayMs());
    }
    level.updatePhoneInteraction();
  }

  private static void removeItemsAlreadyHeld(List<LastHourSave.PlayerData> players) {
    Map<String, Integer> held = new LinkedHashMap<>();
    players.stream()
        .flatMap(player -> player.items().stream())
        .forEach(item -> held.merge(itemKey(item.state()), 1, Integer::sum));
    if (held.isEmpty()) return;
    Game.levelEntities(Set.of(InventoryComponent.class))
        .filter(entity -> entity.fetch(PlayerComponent.class).isEmpty())
        .forEach(
            entity ->
                entity
                    .fetch(InventoryComponent.class)
                    .ifPresent(
                        inventory -> {
                          Item[] items = inventory.items();
                          for (int slot = 0; slot < items.length; slot++) {
                            if (items[slot] != null
                                && takeOne(held, itemKey(ItemState.fromItem(items[slot])))) {
                              inventory.set(slot, null);
                            }
                          }
                        }));
    Game.levelEntities(Set.of(ItemComponent.class))
        .toList()
        .forEach(
            entity -> {
              Item item = entity.fetch(ItemComponent.class).orElseThrow().item();
              if (takeOne(held, itemKey(ItemState.fromItem(item)))) Game.remove(entity);
            });
  }

  private static boolean takeOne(Map<String, Integer> counts, String key) {
    int count = counts.getOrDefault(key, 0);
    if (count == 0) return false;
    if (count == 1) counts.remove(key);
    else counts.put(key, count - 1);
    return true;
  }

  private static String itemKey(ItemState state) {
    return state.itemType() + "\u0000" + new java.util.TreeMap<>(state.itemData());
  }

  private static boolean containsPlayerItem(
      List<LastHourSave.PlayerData> players, String type, String dataKey, String dataValue) {
    return players.stream()
        .flatMap(player -> player.items().stream())
        .anyMatch(
            item ->
                item.state().itemType().equals(type)
                    && (dataKey.isEmpty()
                        || dataValue.equals(item.state().itemData().get(dataKey))));
  }
}
