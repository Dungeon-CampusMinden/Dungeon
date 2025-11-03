package core.utils;

import core.utils.logging.DungeonLogger;

/** Exception stating that there is no player in the game. */
public final class MissingHeroException extends NullPointerException {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(MissingHeroException.class);

  /**
   * Constructs a new MissingHeroException with the specified detail message.
   *
   * @param message Detail message.
   */
  public MissingHeroException(final String message) {
    super("There is no player: " + message);
    LOGGER.error("There is no player: {}", message);
  }

  /** Constructs a new MissingHeroException with default message. */
  public MissingHeroException() {
    super("There is no player!");
    LOGGER.error("There is no player!");
  }
}
