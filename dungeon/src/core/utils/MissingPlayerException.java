package core.utils;

import core.utils.logging.DungeonLogger;

/** Exception stating that there is no player in the game. */
public final class MissingPlayerException extends NullPointerException {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(MissingPlayerException.class);

  /**
   * Constructs a new {@link MissingPlayerException} with the specified detail message.
   *
   * @param message Detail message.
   */
  public MissingPlayerException(final String message) {
    super("There is no player: " + message);
    LOGGER.error("There is no player: {}", message);
  }

  /** Constructs a new {@link MissingPlayerException} with default message. */
  public MissingPlayerException() {
    super("There is no player!");
    LOGGER.error("There is no player!");
  }
}
