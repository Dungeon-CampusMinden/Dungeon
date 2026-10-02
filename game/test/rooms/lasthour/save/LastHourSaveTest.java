package rooms.lasthour.save;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.network.messages.s2c.ItemState;
import engine.utils.JsonHandler;
import feature.inventory.items.HintItem;
import feature.questlog.QuestLogEntry;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import rooms.lasthour.modules.computer.ComputerProgress;
import rooms.lasthour.modules.computer.ComputerStateComponent;
import rooms.lasthour.petrinet.LastHourMilestone;

class LastHourSaveTest {
  @TempDir Path tempDir;

  @Test
  void roundTripPreservesNamedPlayerInventoryAndProgress() throws Exception {
    LastHourSave.SaveData original = sampleSaveData();
    Path file = tempDir.resolve("save.json");

    LastHourSave.write(file, original);

    assertEquals(original, LastHourLoad.read(file).orElseThrow());
    assertFalse(Files.readString(file).contains("\"achievements\""));
  }

  @Test
  void ignoresPopupHistoryInOlderSaveFiles() throws Exception {
    LastHourSave.SaveData original = sampleSaveData();
    Map<String, Object> legacySave =
        new LinkedHashMap<>(JsonHandler.readJson(LastHourSave.toJson(original)));
    legacySave.put("achievements", Map.of("Alex", List.of("lights_on")));
    Path file = tempDir.resolve("legacy-save.json");
    Files.writeString(file, JsonHandler.writeJson(legacySave, true));

    assertEquals(original, LastHourLoad.read(file).orElseThrow());
  }

  @Test
  void rejectsUnsupportedVersionWithoutMutatingFile() throws Exception {
    Path file = tempDir.resolve("bad.json");
    java.nio.file.Files.writeString(file, "{\"formatVersion\":999}");

    assertFalse(LastHourLoad.read(file).isPresent());
    assertTrue(java.nio.file.Files.exists(file));
  }

  private LastHourSave.SaveData sampleSaveData() {
    HintItem.ensureRegistration();
    ItemState item =
        new ItemState("HintItem", 1, 1, Map.of("imagePath", "images/note-password-1.png"));
    return new LastHourSave.SaveData(
        UUID.randomUUID(),
        Set.of(LastHourMilestone.POWER_ON, LastHourMilestone.STORAGE_OPENED),
        ComputerStateComponent.of(ComputerProgress.ON, false, null, 0),
        45,
        -1,
        true,
        7,
        true,
        1234,
        false,
        new LastHourSave.PhoneData(false, false, false, "", 12_000, -1),
        false,
        false,
        List.of(
            new LastHourSave.QuestEntryData(
                "Main", new QuestLogEntry("Find the exit", 123, false, "System", false))),
        Set.of("main.entries.locked_in"),
        List.of(
            new LastHourSave.PlayerData(
                "Alex", 12.5f, 3.5f, true, List.of(new LastHourSave.ItemData(2, item)))),
        true);
  }
}
