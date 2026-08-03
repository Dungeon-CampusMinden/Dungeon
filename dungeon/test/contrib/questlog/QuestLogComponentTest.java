package contrib.questlog;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests for quest log entry ordering. */
public class QuestLogComponentTest {

  /** Verifies entries are stored chronologically even when inserted out of order. */
  @Test
  void addKeepsEntriesOrderedByTimestamp() {
    QuestLogComponent questLog = new QuestLogComponent();

    questLog.add("Main", entry("latest", 30));
    questLog.add("Main", entry("earliest", 10));
    questLog.add("Main", entry("middle", 20));

    assertEquals(
        List.of("earliest", "middle", "latest"),
        questLog.get("Main").stream().map(QuestLogEntry::text).toList());
  }

  /** Verifies tab recency uses the greatest timestamp in each tab, not insertion order. */
  @Test
  void getTabsOrderedByLastEntryUsesNewestTimestampPerTab() {
    QuestLogComponent questLog = new QuestLogComponent();

    questLog.add("Old", entry("old", 1));
    questLog.add("Newest", entry("new", 100));
    questLog.add("Middle", entry("middle", 50));
    questLog.add("Old", entry("late old insert", 2));

    assertEquals(List.of("Newest", "Middle", "Old"), questLog.getTabsOrderedByLastEntry());
  }

  private static QuestLogEntry entry(String text, int timestamp) {
    return new QuestLogEntry(text, timestamp, false, QuestLogEntry.DEFAULT_OWNER, false);
  }
}
