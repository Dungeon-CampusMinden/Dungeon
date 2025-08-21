package petriNet;

import core.Component;

/**
 * Represents a place in a Petri net.
 *
 * <p>A place can hold tokens, which are represented by an internal counter. Tokens can be added or
 * removed, and the current number of tokens can be queried.
 *
 * <p>This class implements the {@link Component} interface for integration within a Petri net
 * system.
 */
public class PlaceComponent implements Component {

  /** The number of tokens currently in this place. */
  private int tokenCounter = 0;

  /**
   * Returns the current number of tokens in this place.
   *
   * @return the number of tokens
   */
  public int tokenCount() {
    return tokenCounter;
  }

  /**
   * Adds token to this place.
   *
   * @param amount of token to add.
   */
  public void produce(int amount) {
    tokenCounter += amount;
  }

  /** Adds one token to this place. */
  public void produce() {
    tokenCounter++;
  }

  /**
   * Removes token from this place.
   *
   * <p>It is the caller's responsibility to ensure that the counter does not become negative.
   *
   * @param amount amount of token to consume.
   * @return true if a token was consumed, false otherwise.
   */
  public boolean consume(int amount) {
    if (tokenCounter >= amount) {
      tokenCounter -= amount;
      return true;
    }
    return false;
  }

  /**
   * Removes one token from this place.
   *
   * <p>It is the caller's responsibility to ensure that the counter does not become negative.
   *
   * @return true if a token was consumed, false otherwise.
   */
  public boolean consume() {
    return consume(1);
  }
}
