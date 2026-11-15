package engine.utils.logging;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Isolated;

/** Tests for {@link DungeonLoggerConfig}. */
@Isolated("Changes the root logger and the shared logging configuration")
class DungeonLoggerConfigTest {

  @TempDir Path directory;

  private final Logger root = Logger.getLogger("");
  private final Map<Field, Object> savedConfig = new HashMap<>();
  private List<Handler> savedHandlers;
  private Level savedLevel;
  private String savedDirectory;

  @BeforeEach
  void startWithFreshConfig() throws ReflectiveOperationException {
    savedHandlers = List.of(root.getHandlers());
    savedLevel = root.getLevel();
    savedDirectory = System.setProperty("BASELOGDIR", directory + File.separator);
    saveAndSet("initialized", false);
    saveAndSet("rootLogger", null);
    saveAndSet("fileHandler", null);
    saveAndSet("consoleHandler", null);
  }

  private void saveAndSet(String name, Object value) throws ReflectiveOperationException {
    Field field = DungeonLoggerConfig.class.getDeclaredField(name);
    field.setAccessible(true);
    savedConfig.put(field, field.get(null));
    field.set(null, value);
  }

  @AfterEach
  void restoreConfig() throws IllegalAccessException {
    for (Handler handler : root.getHandlers()) {
      root.removeHandler(handler);
      // Existing handlers must stay open for their original loggers.
      if (!savedHandlers.contains(handler)) {
        handler.close();
      }
    }
    for (Handler handler : savedHandlers) {
      root.addHandler(handler);
    }
    root.setLevel(savedLevel);
    if (savedDirectory == null) {
      System.clearProperty("BASELOGDIR");
    } else {
      System.setProperty("BASELOGDIR", savedDirectory);
    }
    for (Map.Entry<Field, Object> entry : savedConfig.entrySet()) {
      entry.getKey().set(null, entry.getValue());
    }
  }

  @Test
  public void keepsTheHandlersAndLevelsFromTheFirstConfiguration() {
    DungeonLoggerConfig.builder().consoleLevel(Level.WARNING).fileLevel(Level.INFO).build();
    Handler[] firstHandlers = root.getHandlers();
    assertEquals(2, firstHandlers.length);
    assertEquals(Level.WARNING, findHandler(ConsoleHandler.class).getLevel());
    assertEquals(Level.INFO, findHandler(FileHandler.class).getLevel());

    DungeonLoggerConfig.initWithLevel(DungeonLogLevel.FATAL);

    assertTrue(DungeonLoggerConfig.isInitialized());
    assertEquals(Level.INFO, root.getLevel());
    assertArrayEquals(firstHandlers, root.getHandlers());
  }

  @Test
  public void disablingFileOutputKeepsOnlyTheConsole() {
    DungeonLoggerConfig.builder().enableFile(false).useTimestampInFilename(false).build();

    assertEquals(1, root.getHandlers().length);
    assertInstanceOf(ConsoleHandler.class, root.getHandlers()[0]);
    assertFalse(Files.exists(directory.resolve("systemlogs/dungeon.log")));
  }

  @Test
  public void disablingConsoleOutputWritesOnlyToTheFile() throws IOException {
    DungeonLoggerConfig.builder()
        .enableConsole(false)
        .fileLevel(Level.INFO)
        .useTimestampInFilename(false)
        .build();

    assertEquals(1, root.getHandlers().length);
    FileHandler file = assertInstanceOf(FileHandler.class, root.getHandlers()[0]);
    root.info("Checkpoint saved");
    root.fine("Internal details");
    file.flush();

    String log = Files.readString(directory.resolve("systemlogs/dungeon.log"));
    assertTrue(log.contains("Checkpoint saved"));
    assertFalse(log.contains("Internal details"));
  }

  private Handler findHandler(Class<? extends Handler> type) {
    for (Handler handler : root.getHandlers()) {
      if (type.isInstance(handler)) {
        return handler;
      }
    }
    throw new AssertionError("Missing " + type.getSimpleName());
  }
}
