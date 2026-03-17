package contrib.modules.emote;

/** All available emote types. */
public enum Emote {

  /** An emote. */
  BLANK("blank"),
  /** An emote. */
  ALERT("alert"),
  /** An emote. */
  ANGER("anger"),
  /** An emote. */
  BARS("bars"),
  /** An emote. */
  CASH("cash"),
  /** An emote. */
  CIRCLE("circle"),
  /** An emote. */
  CLOUD("cloud"),
  /** An emote. */
  CROSS("cross"),
  /** An emote. */
  DOTS1("dots1"),
  /** An emote. */
  DOTS2("dots2"),
  /** An emote. */
  DOTS3("dots3"),
  /** An emote. */
  DROP("drop"),
  /** An emote. */
  DROPS("drops"),
  /** An emote. */
  EXCLAMATION("exclamation"),
  /** An emote. */
  EXCLAMATIONS("exclamations"),
  /** An emote. */
  FACE_ANGRY("faceAngry"),
  /** An emote. */
  FACE_HAPPY("faceHappy"),
  /** An emote. */
  FACE_SAD("faceSad"),
  /** An emote. */
  HEART("heart"),
  /** An emote. */
  HEART_BROKEN("heartBroken"),
  /** An emote. */
  HEARTS("hearts"),
  /** An emote. */
  IDEA("idea"),
  /** An emote. */
  LAUGH("laugh"),
  /** An emote. */
  MUSIC("music"),
  /** An emote. */
  QUESTION("question"),
  /** An emote. */
  SLEEP("sleep"),
  /** An emote. */
  SLEEPS("sleeps"),
  /** An emote. */
  STAR("star"),
  /** An emote. */
  STARS("stars"),
  /** An emote. */
  SWIRL("swirl");

  private final String name;

  Emote(String name) {
    this.name = name;
  }

  /**
   * Gets the name of the emote.
   *
   * @return the name of the emote
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the path to the emote's image file.
   *
   * @return the path to the emote's image file
   */
  public String getPath() {
    return "emotes/emote_" + name + ".png";
  }
}
