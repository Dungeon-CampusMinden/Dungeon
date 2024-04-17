package dsl;

public class IndexGenerator {
  private static long _idx; // running idx

  public static long getIdx() {
    if (_idx == 2) {
      boolean b = true;
    }
    return _idx++;
  }
}
