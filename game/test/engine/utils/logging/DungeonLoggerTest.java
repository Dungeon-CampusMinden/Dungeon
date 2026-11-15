package engine.utils.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for {@link DungeonLogger}. */
class DungeonLoggerTest {

  private final Logger output = Logger.getLogger(UUID.randomUUID().toString());
  private final DungeonLogger logger = DungeonLogger.getLogger(output.getName());
  private final RecordingHandler handler = new RecordingHandler();

  @BeforeEach
  void captureMessages() {
    output.setUseParentHandlers(false);
    output.setLevel(Level.INFO);
    output.addHandler(handler);
  }

  @AfterEach
  void removeHandler() {
    output.removeHandler(handler);
  }

  @Test
  public void fillsPlaceholdersInOrder() {
    logger.info("Collected {} of {} keys", 2, 5);

    assertEquals("Collected 2 of 5 keys", handler.records.getFirst().getMessage());
  }

  @Test
  public void leavesPlaceholdersWithoutAnArgumentInTheMessage() {
    logger.info("Move from {} to {}", "spawn");

    assertEquals("Move from spawn to {}", handler.records.getFirst().getMessage());
  }

  @Test
  public void aNullFormatProducesANullMessage() {
    logger.info(null, "unused");

    assertNull(handler.records.getFirst().getMessage());
  }

  @Test
  public void skipsDebugBelowTheInfoThreshold() {
    logger.debug("Checking inventory");
    logger.info("Inventory ready");

    assertEquals(1, handler.records.size());
    assertEquals("Inventory ready", handler.records.getFirst().getMessage());
  }

  @Test
  public void keepsTheExceptionSeparateFromTheFormattedMessage() {
    Exception failure = new IllegalStateException("Missing texture");
    logger.error("Cannot load {}", "hero.png", failure);

    LogRecord record = handler.records.getFirst();
    assertEquals("Cannot load hero.png", record.getMessage());
    assertSame(failure, record.getThrown());
  }

  @Test
  public void varargsUsesTheLastExceptionAsTheCause() {
    Exception failure = new IllegalStateException("Save failed");
    logger.error("Save {} at ({}, {}): {}", "slot1", 3, 4, failure);

    LogRecord record = handler.records.getFirst();
    assertEquals("Save slot1 at (3, 4): {}", record.getMessage());
    assertSame(failure, record.getThrown());
  }

  private static class RecordingHandler extends Handler {
    private final List<LogRecord> records = new ArrayList<>();

    @Override
    public void publish(LogRecord record) {
      records.add(record);
    }

    @Override
    public void flush() {}

    @Override
    public void close() {}
  }
}
