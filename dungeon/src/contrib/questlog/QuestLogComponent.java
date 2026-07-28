package contrib.questlog;

import core.Component;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Component that stores quest log entries grouped by tabs.
 *
 * <p>Each tab is identified by its name and contains the {@link QuestLogEntry} objects that should
 * be displayed under that tab. Entries are stored in insertion order inside each tab, so the last
 * entry in a tab is treated as that tab's newest entry for sorting.
 *
 * <p>Getter methods return copies of the stored collections. Call {@link #add(String,
 * QuestLogEntry)} and {@link #remove(String, QuestLogEntry)} to change the quest log instead of
 * modifying returned lists or maps.
 */
public class QuestLogComponent implements Component {

  private static final int EMPTY_TAB_TIMESTAMP = Integer.MIN_VALUE;

  private final Map<String, List<QuestLogEntry>> questlog = new HashMap<>();

  /** Creates an empty quest log. */
  public QuestLogComponent() {}

  /**
   * Adds a quest log entry to the end of the given tab.
   *
   * <p>If the tab does not exist yet, it is created automatically.
   *
   * @param tab the name of the tab
   * @param entry the quest log entry to add
   * @return {@code true} if the entry was added
   */
  public boolean add(String tab, QuestLogEntry entry) {
    return questlog.computeIfAbsent(tab, ignored -> new ArrayList<>()).add(entry);
  }

  /**
   * Removes a quest log entry from the given tab.
   *
   * @param tab the name of the tab
   * @param entry the quest log entry to remove
   * @return {@code true} if the entry was found and removed, {@code false} otherwise
   */
  public boolean remove(String tab, QuestLogEntry entry) {
    List<QuestLogEntry> entries = questlog.get(tab);
    return entries != null && entries.remove(entry);
  }

  /**
   * Returns the entries stored in the given tab.
   *
   * <p>The returned list is a copy and can be modified without changing this component.
   *
   * @param tab the name of the tab
   * @return a copy of the entries in the tab, or an empty list if the tab does not exist
   */
  public List<QuestLogEntry> get(String tab) {
    List<QuestLogEntry> entries = questlog.get(tab);
    return entries == null ? new ArrayList<>() : new ArrayList<>(entries);
  }

  /**
   * Returns all quest log entries grouped by tab.
   *
   * <p>The returned map and all contained lists are copies and can be modified without changing
   * this component.
   *
   * @return a copy of all tabs and their entries
   */
  public Map<String, List<QuestLogEntry>> getEntries() {
    Map<String, List<QuestLogEntry>> copy = new HashMap<>(questlog.size());
    questlog.forEach((tab, entries) -> copy.put(tab, new ArrayList<>(entries)));
    return copy;
  }

  /**
   * Returns all available tab names.
   *
   * <p>The returned list is a copy and can be modified without changing this component. The order
   * is the iteration order of the internal map; use {@link #getTabsOrderedByLastEntry()} when the
   * tabs should be ordered by recency.
   *
   * @return a copy of all tab names
   */
  public List<String> getQuestlogTabs() {
    return new ArrayList<>(questlog.keySet());
  }

  /**
   * Returns all tab names ordered by the timestamp of their last entry.
   *
   * <p>The first tab has the newest last entry, and the last tab has the oldest last entry. Empty
   * tabs have no last entry and are therefore placed at the end.
   *
   * @return a list of tab names ordered by newest last entry first
   */
  public List<String> getTabsOrderedByLastEntry() {
    return questlog.entrySet().stream()
        .sorted(Comparator.comparingInt(QuestLogComponent::lastEntryTimestamp).reversed())
        .map(Map.Entry::getKey)
        .toList();
  }

  private static int lastEntryTimestamp(Map.Entry<String, List<QuestLogEntry>> tab) {
    List<QuestLogEntry> entries = tab.getValue();
    if (entries.isEmpty()) return EMPTY_TAB_TIMESTAMP;
    return entries.get(entries.size() - 1).timestamp();
  }
}
