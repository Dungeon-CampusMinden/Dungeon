package feature.achievements;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AchievementPopupQueueTest {

  @Test
  void startsOnlyFirstPopupAndKeepsArrivalOrder() {
    AchievementPopupQueue queue = new AchievementPopupQueue();
    queue.enqueue("first", "first.png");
    queue.enqueue("second", "second.png");
    queue.enqueue("third", "third.png");

    assertEquals("first", queue.advance(100).started().orElseThrow().id());
    assertTrue(queue.advance(200).started().isEmpty());
    long duration = AchievementPopupQueue.DISPLAY_DURATION_MS;
    assertEquals("second", queue.advance(100 + duration).started().orElseThrow().id());
    assertEquals("third", queue.advance(100 + 2 * duration).started().orElseThrow().id());
  }

  @Test
  void eachPopupGetsItsFullDurationAfterItStarts() {
    AchievementPopupQueue queue = new AchievementPopupQueue();
    queue.enqueue("first", "first.png");
    queue.enqueue("second", "second.png");

    long duration = AchievementPopupQueue.DISPLAY_DURATION_MS;
    queue.advance(100);
    assertTrue(queue.advance(100 + duration - 1).finished().isEmpty());
    AchievementPopupQueue.Transition switchToSecond = queue.advance(100 + duration);
    assertEquals("first", switchToSecond.finished().orElseThrow().id());
    assertEquals("second", switchToSecond.started().orElseThrow().id());
    assertTrue(queue.advance(100 + 2 * duration - 1).finished().isEmpty());
    assertEquals("second", queue.advance(100 + 2 * duration).finished().orElseThrow().id());
    assertTrue(queue.advance(100 + 2 * duration).started().isEmpty());
  }
}
