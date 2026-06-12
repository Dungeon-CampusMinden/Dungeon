package core.language;

/** Languages that can be used. */
public enum Language {
  DE("Deutsch", "DE"),
  EN("English", "EN"),
  ;

  private final String name;
  private final String shortName;

  Language(String name, String shortName) {
    this.name = name;
    this.shortName = shortName;
  }

  /**
   * Gets the localized name of the language.
   *
   * @return The localized name of the language.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the localized code of the language.
   *
   * @return The localized code of the language.
   */
  public String getShortName() {
    return shortName;
  }

  @Override
  public String toString() {
    return name().toLowerCase();
  }
}
