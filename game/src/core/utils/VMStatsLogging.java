package core.utils;

import core.System;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.logging.Logger;

public final class VMStatsLogging extends System {
  private final Logger logger = Logger.getLogger(VMStatsLogging.class.getName());
  private final PrintWriter fileLogger;
  private int granularity = 30;
  private long frameCount = 0;
  private long maxUsedMemory = 0;

  public VMStatsLogging() {
    try {
      fileLogger =
          new PrintWriter(
              new FileOutputStream(
                  String.format("vmstats_%d.log.csv", java.lang.System.currentTimeMillis() / 1000)),
              true,
              Charset.defaultCharset());
      log("Frame,TotalMemory,FreeMemory,UsedMemory,MaxUsedMemory");
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void execute() {
    frameCount++;
    if (frameCount % granularity == 0) {
      long totalMemory = Runtime.getRuntime().totalMemory();
      long freeMemory = Runtime.getRuntime().freeMemory();
      long usedMemory = totalMemory - freeMemory;
      maxUsedMemory = Math.max(maxUsedMemory, usedMemory);
      log(
          String.format(
              Locale.ENGLISH,
              "%d,%.2f,%.2f,%.2f,%.2f",
              frameCount,
              totalMemory / 1024.0 / 1024.0,
              freeMemory / 1024.0 / 1024.0,
              usedMemory / 1024.0 / 1024.0,
              maxUsedMemory / 1024.0 / 1024.0));
    }
  }

  public void setGranularity(int granularity) {
    if (granularity < 1) {
      throw new IllegalArgumentException("Granularity must be at least 1");
    }
    this.granularity = granularity;
  }

  private void log(String line) {
    if (logger != null) {
      logger.info(line);
    }
    if (fileLogger != null) {
      fileLogger.println(line);
    }
  }
}
