package core.utils;

import core.Game;
import core.System;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

public final class VMStatsLogging extends System {
  public enum ConsoleLogLevel {
    INFO,
    WARNING,
    OFF
  }

  public enum FileLogLevel {
    INFO,
    WARNING,
    OFF
  }

  private final Logger consoleLogger = Logger.getLogger(VMStatsLogging.class.getName());
  private final PrintWriter fileLogger;
  // Number of sequentially minima increments before warning about possible memory leak
  private final int localMinimumCountThreshold = 3;
  private FileLogLevel fileLogLevel;
  private int granularity;
  private int index = 0;
  private long frameCount = 0;
  private long maxUsedMemory = 0;
  private long localMinimumMemory = 0;
  private int localMinimumCount = 0;

  public VMStatsLogging() {
    consoleLogger.addHandler(new ConsoleHandler());
    setConsoleLogLevel(ConsoleLogLevel.WARNING);

    try {
      fileLogger =
          new PrintWriter(
              new FileOutputStream(
                  String.format("vmstats_%d.log.csv", java.lang.System.currentTimeMillis() / 1000)),
              true,
              Charset.defaultCharset());
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
    setFileLogLevel(FileLogLevel.INFO);

    setGranularityInMilliseconds(1000);

    logInfo("Index,Frame,TotalMemory,FreeMemory,UsedMemory,MaxUsedMemory," + "LocalMinimumMemory");
  }

  @Override
  public void execute() {
    frameCount++;
    if (frameCount % granularity == 0) {
      // New checkpoint reached
      index++;

      long totalMemory = Runtime.getRuntime().totalMemory();
      long freeMemory = Runtime.getRuntime().freeMemory();
      long usedMemory = totalMemory - freeMemory;

      // Check for possible memory leaks
      checkPossibleMemoryLeaks(usedMemory);

      maxUsedMemory = Math.max(maxUsedMemory, usedMemory);
      logInfo(
          String.format(
              Locale.ENGLISH,
              "%d,%d,%.2f,%.2f,%.2f,%.2f,%.2f",
              index,
              frameCount,
              totalMemory / 1024.0 / 1024.0,
              freeMemory / 1024.0 / 1024.0,
              usedMemory / 1024.0 / 1024.0,
              maxUsedMemory / 1024.0 / 1024.0,
              localMinimumMemory / 1024.0 / 1024.0));
    }
  }

  public void setConsoleLogLevel(ConsoleLogLevel level) {
    if (level == null) {
      throw new IllegalArgumentException("Level must not be null");
    }
    switch (level) {
      case INFO:
        consoleLogger.setLevel(java.util.logging.Level.INFO);
        break;
      case WARNING:
        consoleLogger.setLevel(java.util.logging.Level.WARNING);
        break;
      case OFF:
        consoleLogger.setLevel(java.util.logging.Level.OFF);
        break;
    }
  }

  public void setFileLogLevel(FileLogLevel level) {
    if (level == null) {
      throw new IllegalArgumentException("Level must not be null");
    }
    fileLogLevel = level;
  }

  public void setGranularityInFrames(int frames) {
    if (frames < 1) {
      throw new IllegalArgumentException("Frames must be at least 1");
    }
    granularity = frames;
  }

  public void setGranularityInMilliseconds(int milliseconds) {
    if (milliseconds < 500) {
      throw new IllegalArgumentException("Milliseconds must be at least 500");
    }
    setGranularityInFrames(milliseconds * Game.frameRate() / 1000);
  }

  private void logInfo(String line) {
    if (consoleLogger != null) {
      consoleLogger.info(line);
    }
    if (fileLogger != null && fileLogLevel.ordinal() == FileLogLevel.INFO.ordinal()) {
      fileLogger.println(line);
    }
  }

  private void logWarning(String line) {
    if (consoleLogger != null) {
      consoleLogger.warning(line);
    }
    if (fileLogger != null && fileLogLevel.ordinal() <= FileLogLevel.WARNING.ordinal()) {
      fileLogger.println(line);
    }
  }

  private void checkPossibleMemoryLeaks(long usedMemory) {
    if (usedMemory < maxUsedMemory) {
      boolean localMinimumCountIncreased = false;
      if (localMinimumMemory != 0 && usedMemory > localMinimumMemory) {
        if (localMinimumCount < localMinimumCountThreshold) {
          localMinimumCount++;
          localMinimumCountIncreased = true;
        } else {
          logWarning(
              String.format(
                  Locale.ENGLISH,
                  "Possible memory leak discovered: %.2f MB (was %.2f MB)",
                  usedMemory / 1024.0 / 1024.0,
                  localMinimumMemory / 1024.0 / 1024.0));
        }
      }
      if (!localMinimumCountIncreased) {
        localMinimumCount = 0;
      }
      localMinimumMemory = Math.max(localMinimumMemory, usedMemory);
    }
  }
}
