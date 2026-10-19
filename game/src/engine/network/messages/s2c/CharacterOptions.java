package engine.network.messages.s2c;

import engine.network.messages.NetworkMessage;
import feature.entities.CharacterClass;
import java.util.List;
import java.util.Objects;

/**
 * Server→client: character availability and capacity for the requested player name.
 *
 * @param characters available character classes and their current admission state
 */
public record CharacterOptions(List<Option> characters) implements NetworkMessage {

  /** Creates an immutable character-options response. */
  public CharacterOptions {
    characters = List.copyOf(Objects.requireNonNull(characters, "characters"));
  }

  /**
   * Availability and admission status for one character class.
   *
   * @param characterClass character class represented
   * @param maxCount maximum admitted players, or {@code -1} for unlimited
   * @param playerCount current named reservations for this class
   * @param selectable whether the requesting player can select this class
   * @param assigned whether this is the requesting player's saved assignment
   */
  public record Option(
      CharacterClass characterClass,
      int maxCount,
      int playerCount,
      boolean selectable,
      boolean assigned) {
    /** Creates and validates a character option. */
    public Option {
      Objects.requireNonNull(characterClass, "characterClass");
      if (maxCount < -1) {
        throw new IllegalArgumentException("maxCount must be -1 or non-negative");
      }
      if (playerCount < 0) {
        throw new IllegalArgumentException("playerCount must be non-negative");
      }
    }
  }
}
