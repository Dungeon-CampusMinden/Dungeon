package contrib.modules.interaction;

/**
 * A simplified version of {@link IInteractable} for entities that provide only a single
 * interaction.
 *
 * <p>Unlike {@link IInteractable}, which exposes multiple interaction types (look, talk, take,
 * use-with-item, etc.), an {@code ISimpleIInteractable} defines exactly one interaction: {@link
 * #interact()}.
 *
 * <p>This interface is typically used when an entity should always execute the same interaction
 * without showing an interaction menu.
 */
public interface ISimpleIInteractable extends IInteractable {

  /**
   * Returns the single interaction this interactable provides.
   *
   * @return the interaction executed when this interactable is triggered
   */
  @Override
  Interaction interact();
}
