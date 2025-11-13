package contrib.modules.interaction;

import java.util.Optional;

/**
 * Utility class for displaying an interaction selection menu.
 *
 * <p>Currently this is a placeholder implementation that always returns the default interaction
 * provided by the given {@link IInteractable}.
 *
 * <p>Once implemented, this class should present the player with a menu (e.g., a radial / ring
 * menu) allowing them to choose among multiple available interactions.
 */
public class RingMenue {

  /**
   * Shows an interaction menu for the given {@link IInteractable} and returns the interaction
   * selected by the player.
   *
   * <p>This temporary implementation simply returns the default interaction ({@link
   * IInteractable#interact()}) without showing any menu.
   *
   * @param i the interactable providing available interactions
   * @return an {@link Optional} containing the selected interaction; never empty in the current
   *     implementation
   */
  public static Optional<Interaction> showInteractionMenue(IInteractable i) {
    return Optional.of(i.interact());
  }
}
