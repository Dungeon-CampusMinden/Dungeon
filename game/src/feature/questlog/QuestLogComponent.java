package feature.questlog;

import engine.Component;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Component that stores quest log entries grouped by tabs.
 *
 * <p>Each tab is identified by its name and contains the {@link QuestLogEntry} objects that should
 * be displayed under that tab. Entries inside a tab are kept ordered by timestamp, so restored or
 * delayed entries are displayed chronologically regardless of insertion order.
 *
 * <p>Getter methods return copies of the stored collections. Call {@link #add(String,
 * QuestLogEntry)} and {@link #remove(String, QuestLogEntry)} to change the quest log instead of
 * modifying returned lists or maps.
 */
public class QuestLogComponent implements Component {

  private static final int EMPTY_TAB_TIMESTAMP = Integer.MIN_VALUE;

  private final Map<String, List<QuestLogEntry>> questlog = new HashMap<>();
  private Optional<Overview> overview = Optional.empty();

  /**
   * Returns the optional starting tab and its links to existing entries.
   *
   * @return the room's overview configuration, if any
   */
  public Optional<Overview> overview() {
    return overview;
  }

  /**
   * Configures a starting tab without copying the linked entries into it.
   *
   * @param tab the starting tab, which must contain an entry to be visible
   * @param references existing entries to link below the starting tab's first entry, in display
   *     order
   */
  public void overview(String tab, List<QuestLogEntry> references) {
    overview = Optional.of(new Overview(tab, references));
  }

  /**
   * Only references to stored entries visible to the current viewer are displayed.
   *
   * @param tab the starting tab
   * @param references links to existing entries, in display order
   */
  public record Overview(String tab, List<QuestLogEntry> references) {
    /**
     * Creates an immutable overview configuration.
     *
     * @param tab the starting tab
     * @param references links to existing entries, in display order
     */
    public Overview {
      Objects.requireNonNull(tab, "tab");
      references = List.copyOf(references);
    }
  }

  /**
   * Adds a quest log entry to the given tab.
   *
   * <p>If the tab does not exist yet, it is created automatically. Entries are sorted by timestamp
   * after insertion.
   *
   * @param tab the name of the tab
   * @param entry the quest log entry to add
   * @return {@code true} if the entry was added
   */
  public boolean add(String tab, QuestLogEntry entry) {
    List<QuestLogEntry> entries = questlog.computeIfAbsent(tab, ignored -> new ArrayList<>());
    boolean added = entries.add(entry);
    entries.sort(Comparator.comparingInt(QuestLogEntry::timestamp));
    return added;
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
   * <p>The returned list is a shallow-copy.
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
   * <p>The returned map and all contained lists are shallow-copy.
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
   * <p>The order is the iteration order of the internal map; use {@link
   * #getTabsOrderedByLastEntry()} when the tabs should be ordered by recency.
   *
   * @return a list of all tab names
   */
  public List<String> getQuestlogTabs() {
    return new ArrayList<>(questlog.keySet());
  }

  /**
   * Returns all tab names ordered by the newest timestamp in each tab.
   *
   * <p>The first tab has the newest entry, and the last tab has the oldest newest entry. Empty tabs
   * have no last entry and are therefore placed at the end.
   *
   * @return a list of tab names ordered by newest last entry first
   */
  public List<String> getTabsOrderedByLastEntry() {
    return questlog.entrySet().stream()
        .sorted(Comparator.comparingInt(QuestLogComponent::newestEntryTimestamp).reversed())
        .map(Map.Entry::getKey)
        .toList();
  }

  private static int newestEntryTimestamp(Map.Entry<String, List<QuestLogEntry>> tab) {
    List<QuestLogEntry> entries = tab.getValue();
    if (entries.isEmpty()) return EMPTY_TAB_TIMESTAMP;
    return entries.stream().mapToInt(QuestLogEntry::timestamp).max().orElse(EMPTY_TAB_TIMESTAMP);
  }
}
