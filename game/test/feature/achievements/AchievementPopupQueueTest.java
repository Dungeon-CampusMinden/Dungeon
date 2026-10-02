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
    assertEquals("second", queue.advance(4600).started().orElseThrow().id());
    assertEquals("third", queue.advance(9100).started().orElseThrow().id());
  }

  @Test
  void eachPopupGetsItsFullDurationAfterItStarts() {
    AchievementPopupQueue queue = new AchievementPopupQueue();
    queue.enqueue("first", "first.png");
    queue.enqueue("second", "second.png");

    queue.advance(100);
    assertTrue(queue.advance(4599).finished().isEmpty());
    AchievementPopupQueue.Transition switchToSecond = queue.advance(4600);
    assertEquals("first", switchToSecond.finished().orElseThrow().id());
    assertEquals("second", switchToSecond.started().orElseThrow().id());
    assertTrue(queue.advance(9099).finished().isEmpty());
    assertEquals("second", queue.advance(9100).finished().orElseThrow().id());
    assertTrue(queue.advance(9100).started().isEmpty());
  }

  @Test
  void ignoresDuplicateIdsWhileQueuedOrVisible() {
    AchievementPopupQueue queue = new AchievementPopupQueue();
    queue.enqueue("first", "first.png");
    queue.enqueue("first", "duplicate.png");
    queue.enqueue("second", "second.png");

    assertEquals("first.png", queue.advance(0).started().orElseThrow().imagePath());
    queue.enqueue("first", "duplicate.png");
    assertEquals("second", queue.advance(4500).started().orElseThrow().id());
    assertTrue(queue.advance(9000).started().isEmpty());
  }

  @Test
  void requeuesVisiblePopupBeforeWaitingOnesAfterStageChange() {
    AchievementPopupQueue queue = new AchievementPopupQueue();
    queue.enqueue("first", "first.png");
    queue.enqueue("second", "second.png");

    queue.advance(0);
    queue.restartActive();

    assertEquals("first", queue.advance(1000).started().orElseThrow().id());
    assertEquals("second", queue.advance(5500).started().orElseThrow().id());
  }
}
