package feature.achievements;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

/**
 * Controls the order and display time of achievement notifications without depending on graphics.
 */
final class AchievementPopupQueue {

  static final long DISPLAY_DURATION_MS = 4500L;

  record Entry(String id, String imagePath) {}

  record Transition(Optional<Entry> finished, Optional<Entry> started) {}

  private final Deque<Entry> pending = new ArrayDeque<>();
  private Entry active;
  private long activeSince;

  void enqueue(String id, String imagePath) {
    pending.addLast(new Entry(id, imagePath));
  }

  Transition advance(long now) {
    Entry finished = null;
    if (active != null && now - activeSince >= DISPLAY_DURATION_MS) {
      finished = active;
      active = null;
    }

    Entry started = null;
    if (active == null && !pending.isEmpty()) {
      started = pending.removeFirst();
      active = started;
      activeSince = now;
    }
    return new Transition(Optional.ofNullable(finished), Optional.ofNullable(started));
  }
}
