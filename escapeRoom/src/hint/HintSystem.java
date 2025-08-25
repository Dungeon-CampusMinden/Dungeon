package hint;

import core.Entity;
import core.System;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import petriNet.PlaceComponent;

/**
 * System that manages hints.
 *
 * <p>This system tracks entities that have both a {@link HintComponent} and a {@link
 * PlaceComponent}. Entities with tokens in their {@link PlaceComponent} are added to a unique
 * queue. Hints are provided sequentially for each entity using its {@link HintComponent}. When an
 * entity has shown all its hints, the system automatically moves to the next entity in the queue.
 *
 * <p>How it works:
 *
 * <ul>
 *   <li>Entities must have a {@link PlaceComponent} that contains tokens and a {@link
 *       HintComponent} with hints.
 *   <li>During execution ({@link #execute()}), the system checks each entity:
 *       <ul>
 *         <li>If the entity has tokens, it is added to the hint queue.
 *         <li>If the entity has no tokens, it is removed from the queue. If it was the current
 *             entity showing hints, the current hint is cleared.
 *       </ul>
 *   <li>Hints are accessed via {@link #nextHint()}. This method returns the next hint for the
 *       current entity.
 *   <li>When an entity has no more hints, the system automatically moves to the next entity in the
 *       queue.
 * </ul>
 *
 * <p>How to use it:
 *
 * <ol>
 *   <li>Create entities with both a {@link PlaceComponent} (to track tokens) and a {@link
 *       HintComponent} (containing hints).
 *   <li>Instantiate the {@link HintSystem} and add it to the game.
 *   <li>Call {@link #nextHint()} to retrieve hints one by one. The system automatically handles
 *       moving to the next entity when needed.
 * </ol>
 *
 * <p>The queue preserves insertion order and ensures that each entity appears only once. This
 * allows hints to be provided fairly and in the order entities became eligible (i.e., had tokens).
 */
public class HintSystem extends System {

  private final Set<Entity> hintQueue = new LinkedHashSet<>();

  private HintComponent currentHint = null;

  /**
   * Constructs a new HintSystem.
   *
   * <p>The system automatically tracks entities that contain both a {@link HintComponent} and a
   * {@link PlaceComponent}. When an entity is removed, its hints are also removed from the system.
   */
  public HintSystem() {
    super(HintComponent.class, PlaceComponent.class);
    this.onEntityRemove = this::removeHint;
  }

  /**
   * Executes the system logic for all tracked entities.
   *
   * <p>Entities with tokens in their {@link PlaceComponent} are added to the hint queue. Entities
   * without tokens are removed from the queue. If the current hint belongs to an entity that is
   * removed, it is cleared.
   */
  @Override
  public void execute() {
    filteredEntityStream()
        .forEach(
            entity ->
                entity
                    .fetch(PlaceComponent.class)
                    .ifPresent(
                        placeComponent -> {
                          if (placeComponent.tokenCount() > 0) {
                            hintQueue.add(entity);
                          } else {
                            if (currentHint != null
                                && currentHint.equals(
                                    entity.fetch(HintComponent.class).orElse(null))) {
                              currentHint = null;
                            }
                            removeHint(entity);
                          }
                        }));
  }

  /**
   * Returns the next {@link Hint} from the current entity in the queue.
   *
   * <p>If the current entity has no remaining hints, the system automatically advances to the next
   * entity in the queue. If all entities have been exhausted, this method returns {@link
   * Optional#empty()}.
   *
   * @return an {@link Optional} containing the next hint if available, or {@link Optional#empty()}
   *     if no more hints remain
   */
  public Optional<Hint> nextHint() {
    if (currentHint == null) {
      currentHint = fetchNextEntityHint();
      if (currentHint == null) return Optional.empty();
    }

    Optional<Hint> hint = currentHint.hint();
    currentHint.increaseIndex();

    if (currentHint.isLastHintShown()) {
      // this allows requesting the hint again.
      currentHint.resetIndex();
      currentHint = fetchNextEntityHint();
    }
    return hint;
  }

  /**
   * Removes the given entity from the hint queue.
   *
   * <p>If the entity being removed is the one currently showing hints, the current hint is cleared.
   *
   * @param entity the entity to remove
   */
  private void removeHint(Entity entity) {
    if (currentHint != null && currentHint.equals(entity.fetch(HintComponent.class).orElse(null))) {
      currentHint = null;
    }
    hintQueue.remove(entity);
  }

  /**
   * Fetches the {@link HintComponent} of the next entity in the queue.
   *
   * <p>The entity is removed from the queue after fetching its hint. If the queue is empty, this
   * method returns null.
   *
   * @return the next entity's {@link HintComponent}, or null if the queue is empty
   */
  private HintComponent fetchNextEntityHint() {
    Iterator<Entity> iterator = hintQueue.iterator();
    if (iterator.hasNext()) {
      Entity nextEntity = iterator.next();
      iterator.remove();
      return nextEntity.fetch(HintComponent.class).orElse(null);
    }
    return null;
  }
}
