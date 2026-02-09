package modules.computer;

public enum ComputerProgress {
  OFF(0),
  ON(1),
  LOGGED_IN(2),
  ;

  private final int progress;

  ComputerProgress(int progress) {
    this.progress = progress;
  }

  public int progress() {
    return progress;
  }

  public boolean hasReached(ComputerProgress other) {
    return this.progress >= other.progress;
  }
}
