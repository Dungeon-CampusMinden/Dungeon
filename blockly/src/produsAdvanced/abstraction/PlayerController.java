package produsAdvanced.abstraction;

/**
 * Abstract base class for player control logic in the game.
 *
 * <p>This class defines the interface for how a {@link Hero} should react to input keys. Concrete
 * subclasses implement specific control behaviors, which can be dynamically loaded or compiled at
 * runtime.
 *
 * <p>This abstraction allows for flexible, modular, and potentially user-defined control schemes
 * that can be injected into the game without modifying the core logic.
 */
public abstract class PlayerController {

  /**
   * Constructor.
   *
   * @param hero The {@link Hero} instance this controller will manipulate.
   */
  public PlayerController(Hero hero) {}

  /**
   * Handles a key press event by processing the given key string.
   *
   * @param key The string representation of the key (e.g., "W", "A", "D").
   */
  protected abstract void processKey(String key) throws Exception;
}
