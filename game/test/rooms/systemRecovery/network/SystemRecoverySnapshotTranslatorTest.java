package rooms.systemRecovery.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import feature.questlog.QuestLogComponent;
import feature.questlog.QuestLogEntry;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Tests synchronization of the System Recovery questlog. */
public class SystemRecoverySnapshotTranslatorTest {

  /** Questlog tabs and entry metadata survive a server-to-client roundtrip. */
  @Test
  void questLogMetadataPreservesTabsAndEntries() {
    QuestLogComponent questLog = new QuestLogComponent();
    questLog.add(
        "Rätsel 1: Energieversorgung",
        new QuestLogEntry("Fülle das Energie-Array.", 17, false, QuestLogEntry.DEFAULT_OWNER, false));

    Map<String, String> metadata = SystemRecoverySnapshotTranslator.questLogMetadata(questLog);

    QuestLogComponent restored =
        SystemRecoverySnapshotTranslator.questLogFromMetadata(metadata).orElseThrow();
    QuestLogEntry entry = restored.get("Rätsel 1: Energieversorgung").get(0);
    assertEquals("Fülle das Energie-Array.", entry.text());
    assertEquals(17, entry.timestamp());
    assertTrue(metadata.containsKey(SystemRecoveryEntitySpawnStrategy.METADATA_QUESTLOG_ENTRIES));
  }
}
