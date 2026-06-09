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

  public String getName() {
    return name;
  }

  public String getShortName() {
    return shortName;
  }

  @Override
  public String toString() {
    return name().toLowerCase();
  }
}
