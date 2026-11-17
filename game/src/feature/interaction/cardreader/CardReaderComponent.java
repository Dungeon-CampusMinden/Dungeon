package feature.interaction.cardreader;

import engine.Component;
import engine.Entity;
import feature.inventory.Item;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Settings and state of a card reader on the server: the pace a swipe needs, the card a player has
 * to hold, whether the reader was used, and what happens then.
 *
 * <p>Players use the reader through {@link CardReaderFactory#use(Entity, Entity)}, which shows the
 * swipe dialog and rates every swipe again.
 */
public class CardReaderComponent implements Component {

  /**
   * The card a player has to hold to use the reader.
   *
   * @param matches matches the items that count as the card
   * @param consume whether the card is removed from the player's inventory once the reader accepted
   *     it
   * @param onMissing called with a player who tried to use the reader without the card, e.g. to
   *     tell which card is needed
   */
  public record KeyCard(Predicate<Item> matches, boolean consume, Consumer<Entity> onMissing) {}

  private final Pace pace;
  private boolean singleUse = true;
  private boolean used;
  private KeyCard keyCard;
  private Consumer<Entity> onAccepted = player -> {};
  private Consumer<Entity> onRejected = player -> {};

  /**
   * Creates a reader that needs no card and accepts a single swipe.
   *
   * @param pace how fast a swipe has to be
   */
  public CardReaderComponent(Pace pace) {
    this.pace = pace;
  }

  /**
   * Returns how fast a swipe has to be.
   *
   * @return the pace of the reader
   */
  public Pace pace() {
    return pace;
  }

  /**
   * Returns whether the reader stays locked once it accepted a card. Defaults to true.
   *
   * @return true if the reader accepts a single swipe
   */
  public boolean singleUse() {
    return singleUse;
  }

  /**
   * Sets whether the reader stays locked once it accepted a card; otherwise every accepted swipe
   * counts again.
   *
   * @param singleUse true if the reader accepts a single swipe
   */
  public void singleUse(boolean singleUse) {
    this.singleUse = singleUse;
  }

  /**
   * Returns whether a single-use reader accepted a card and stays locked. {@link
   * CardReaderFactory#markAccepted(Entity)} locks it.
   *
   * @return true if the reader is locked after an accepted swipe
   */
  public boolean used() {
    return used;
  }

  void markUsed() {
    used = true;
  }

  /**
   * Returns the card a player has to hold to use the reader.
   *
   * @return the card, or empty if nobody needs one
   */
  public Optional<KeyCard> keyCard() {
    return Optional.ofNullable(keyCard);
  }

  /**
   * Lets only players who hold a matching item use the reader.
   *
   * @param card the card a player has to hold
   */
  public void requireCard(KeyCard card) {
    this.keyCard = card;
  }

  /**
   * Sets the callback called with the player whose swipe the reader accepted. It runs once the
   * swipe dialogs of the reader closed.
   *
   * @param onAccepted callback receiving the player
   */
  public void onAccepted(Consumer<Entity> onAccepted) {
    this.onAccepted = onAccepted;
  }

  /**
   * Sets the callback called with the player whose swipe the reader rejected.
   *
   * @param onRejected callback receiving the player
   */
  public void onRejected(Consumer<Entity> onRejected) {
    this.onRejected = onRejected;
  }

  void accept(Entity player) {
    onAccepted.accept(player);
  }

  void reject(Entity player) {
    onRejected.accept(player);
  }
}
