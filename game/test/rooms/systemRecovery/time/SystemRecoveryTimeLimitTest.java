package rooms.systemRecovery.time;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

/** Tests the shared budget with virtual time instead of sleeping for an hour. */
class SystemRecoveryTimeLimitTest {

  private final AtomicLong clock = new AtomicLong();
  private final SystemRecoveryTimeLimit timer =
      new SystemRecoveryTimeLimit(Duration.ofMinutes(4), clock::get);

  @Test
  void introTimeDoesNotConsumeTheBudget() {
    clock.set(Duration.ofMinutes(10).toMillis());

    assertEquals(3600, timer.remainingSeconds());
    assertFalse(timer.started());
    assertFalse(timer.expired());
    assertFalse(timer.hintDue());

    timer.start();
    clock.addAndGet(1000);
    assertEquals(3599, timer.remainingSeconds());
  }

  @Test
  void anotherPlayersIntroCannotRestartTheTimer() {
    timer.start();
    clock.set(Duration.ofMinutes(10).toMillis());

    timer.start();

    assertEquals(3000, timer.remainingSeconds());
  }

  @Test
  void expiryIsClampedToZeroAndDoesNotDeliverHints() {
    timer.start();
    clock.set(Duration.ofHours(1).toMillis() - 1);
    assertEquals(1, timer.remainingSeconds());
    assertFalse(timer.expired());

    clock.incrementAndGet();

    assertEquals(0, timer.remainingSeconds());
    assertTrue(timer.expired());
    assertFalse(timer.hintDue());
    clock.addAndGet(10_000);
    assertEquals(0, timer.remainingSeconds());
  }

  @Test
  void inactivityOffersHintsAtTheConfiguredInterval() {
    timer.start();
    clock.set(Duration.ofMinutes(4).toMillis() - 1);
    assertFalse(timer.hintDue());

    clock.incrementAndGet();
    assertTrue(timer.hintDue());
    timer.postponeHint();
    assertFalse(timer.hintDue());
    clock.addAndGet(Duration.ofMinutes(4).toMillis());
    assertTrue(timer.hintDue());
  }

  @Test
  void acceptedProgressOrManualHintResetsInactivityButNotRemainingTime() {
    timer.start();
    clock.set(Duration.ofMinutes(3).toMillis());
    timer.postponeHint();
    clock.addAndGet(Duration.ofMinutes(3).toMillis());

    assertFalse(timer.hintDue());
    assertEquals(3240, timer.remainingSeconds());
    clock.addAndGet(Duration.ofMinutes(1).toMillis());
    assertTrue(timer.hintDue());
  }

  @Test
  void loadingUsesRemainingBudgetWithoutChargingOfflineTime() {
    clock.set(Duration.ofDays(3).toMillis());
    timer.restore(1234);
    timer.start();

    assertEquals(1234, timer.remainingSeconds());
    assertFalse(timer.hintDue());
    clock.addAndGet(10_000);
    assertEquals(1224, timer.remainingSeconds());
  }

  @Test
  void completedPuzzleFreezesTheClockAndStopsHints() {
    timer.start();
    clock.set(Duration.ofMinutes(59).toMillis());
    timer.finish();
    clock.set(Duration.ofHours(2).toMillis());

    assertEquals(60, timer.remainingSeconds());
    assertFalse(timer.expired());
    assertFalse(timer.hintDue());
  }

  @Test
  void expiredSaveDoesNotGrantMoreTime() {
    timer.restore(0);
    timer.start();

    assertTrue(timer.expired());
    assertEquals(0, timer.remainingSeconds());
  }

  @Test
  void invalidSettingsAndBudgetsAreRejected() {
    assertThrows(IllegalArgumentException.class, () -> timer.restore(-1));
    assertThrows(IllegalArgumentException.class, () -> timer.restore(3601));
    assertThrows(
        IllegalArgumentException.class,
        () -> new SystemRecoveryTimeLimit(Duration.ZERO, clock::get));
  }
}
