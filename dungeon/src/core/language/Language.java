package core.language;

/** Languages that can be used. */
public enum Language {
  /** German. */
  DE,
  /** English. */
  EN;

  @Override
  public String toString() {
    return name().toLowerCase();
  }
}
