package dsl;

import java.util.UUID;

public class IndexGenerator {
  private static long _idx = 1L; // running idx

  public static long getIdx() {
    return _idx++;
  }

  public static long getUniqueIdx() {
    return UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
  }
}
