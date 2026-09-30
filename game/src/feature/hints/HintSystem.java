package feature.hints;

import engine.Entity;
import engine.System;
import feature.petrinet.PlaceComponent;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

  /** Shared hint progress used by the server-authoritative telephone. */
  private final Map<Integer, Integer> sharedHintIndices = new HashMap<>();

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
   * Peeks at the next shared hint without consuming it.
   *
   * <p>The active Petri-net place is selected from entities that currently carry a token. All
   * players see and advance the same hint sequence for that place.
   *
   * @return the next hint for the active state, if available
   */
  public synchronized Optional<Hint> peekSharedHint() {
    return activeHintEntity().flatMap(entity -> hintAt(entity, sharedHintIndex(entity.id())));
  }

  /**
   * Confirms the currently offered shared hint.
   *
   * <p>The index advances only after the player accepts the confirmation dialog. Declining a hint
   * leaves it available for the next phone request.
   *
   * @return the accepted hint, or empty if the state changed before confirmation
   */
  public Optional<Hint> acceptSharedHint() {
    return acceptSharedHint(null);
  }

  /**
   * Confirms a previously offered shared hint if it is still the current hint.
   *
   * <p>The check and index increment happen together so two players confirming the same phone
   * dialog cannot consume two hint stages. Passing {@code null} preserves the unconditional
   * behavior of {@link #acceptSharedHint()} for callers that do not keep an offer snapshot.
   *
   * @param expectedHint hint that was shown to the player, or {@code null} to accept the current
   *     hint without comparing it
   * @return the accepted hint, or empty if the shared hint changed before confirmation
   */
  public synchronized Optional<Hint> acceptSharedHint(Hint expectedHint) {
    Optional<Entity> activeEntity = activeHintEntity();
    if (activeEntity.isEmpty()) return Optional.empty();

    Entity entity = activeEntity.orElseThrow();
    int index = sharedHintIndex(entity.id());
    Optional<Hint> hint = hintAt(entity, index);
    if (hint.isEmpty()) return Optional.empty();
    if (expectedHint != null && !expectedHint.equals(hint.orElseThrow())) {
      return Optional.empty();
    }

    sharedHintIndices.put(entity.id(), index + 1);
    return hint;
  }

  /**
   * Confirms a hint only if the active place is still the entity that produced the offer.
   *
   * @param expectedEntityId entity ID captured when the telephone offered the hint
   * @param expectedHint exact hint shown in that offer
   * @return accepted hint, or empty when the place or hint changed
   */
  public synchronized Optional<Hint> acceptSharedHint(int expectedEntityId, Hint expectedHint) {
    Optional<Entity> activeEntity = activeHintEntity();
    if (activeEntity.isEmpty() || activeEntity.orElseThrow().id() != expectedEntityId) {
      return Optional.empty();
    }
    return acceptSharedHint(expectedHint);
  }

  /**
   * Reads the accepted shared-hint count for one hint-bearing entity without advancing it.
   *
   * @param entity entity whose active place owns the hint sequence
   * @return accepted and total hint counts, or {@code 0/0} when the entity has no hint component
   */
  public synchronized SharedHintProgress sharedProgress(Entity entity) {
    if (entity == null) return new SharedHintProgress(0, 0);
    Optional<HintComponent> component = entity.fetch(HintComponent.class);
    if (component.isEmpty()) return new SharedHintProgress(0, 0);

    int count = component.orElseThrow().size();
    int accepted = Math.min(sharedHintIndex(entity.id()), count);
    return new SharedHintProgress(accepted, count);
  }

  /** Resets the shared phone hint progress for the room. */
  public synchronized void resetHintProgress() {
    sharedHintIndices.clear();
  }

  private int sharedHintIndex(int entityId) {
    return sharedHintIndices.getOrDefault(entityId, 0);
  }

  private Optional<Entity> activeHintEntity() {
    return filteredEntityStream()
        .filter(
            entity ->
                entity
                    .fetch(PlaceComponent.class)
                    .map(place -> place.tokenCount() > 0)
                    .orElse(false))
        .min(Comparator.comparingInt(Entity::id));
  }

  private Optional<Hint> hintAt(Entity entity, int index) {
    return entity
        .fetch(HintComponent.class)
        .filter(component -> index < component.size())
        .map(component -> component.hint(index));
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

  /**
   * Immutable inspection result for the shared telephone-hint sequence.
   *
   * @param acceptedCount number of accepted hints in the current shared sequence
   * @param hintCount number of available hints in that sequence
   */
  public record SharedHintProgress(int acceptedCount, int hintCount) {}
}
