package util;

import contrib.modules.interaction.InteractionComponent;
import contrib.utils.EntityUtils;
import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.utils.Point;
import java.util.Optional;
import java.util.Set;

/**
 * Cursor-first interaction helper for The Last Hour.
 *
 * <p>Provides discovery and targeting queries used by the client-side highlighting system. The
 * actual interaction resolution (which entity to interact with) is handled by {@link
 * contrib.entities.HeroController#findInteractable}, which uses the same {@link
 * EntityUtils#isPointOverEntity} algorithm, guaranteeing that the entity highlighted on the client
 * matches the entity the server selects for interaction.
 */
public final class InteractionHelper {

  private InteractionHelper() {}

  /**
   * Finds the interactable entity under the current cursor position, regardless of hero range. Used
   * for discovery semi-highlighting so players can scan the room and spot interactable objects.
   *
   * <p>Delegates to {@link EntityUtils#isPointOverEntity(Entity, Point)} for the containment check.
   * If multiple entities overlap at the cursor, the one whose center is closest is returned.
   *
   * @param point the world-space point to search near (typically the cursor position)
   * @return the entity under the point, or {@link Optional#empty()} if none qualifies
   */
  public static Optional<Entity> findCursorNearEntity(Point point) {
    return EntityUtils.findEntityAtPoint(
        point, Game.levelEntities(Set.of(PositionComponent.class, InteractionComponent.class)));
  }

  /**
   * Convenience overload that uses the current cursor position.
   *
   * @return the entity under the cursor, or {@link Optional#empty()} if none qualifies
   */
  public static Optional<Entity> findCursorNearEntity() {
    return findCursorNearEntity(SkillTools.cursorPositionAsPoint());
  }

  /**
   * Finds the interactable entity that the player is currently pointing at with the cursor,
   * provided it is also within interaction range of the hero. The cursor-nearest entity is checked
   * exclusively — if it is out of range, the result is empty even if other entities are in range.
   *
   * <p>Uses the same algorithm as {@link contrib.entities.HeroController#findInteractable} so that
   * highlighting always matches server-side interaction resolution.
   *
   * @param hero the player entity
   * @return the targeted entity, or {@link Optional#empty()} if nothing qualifies
   */
  public static Optional<Entity> findInteractTarget(Entity hero) {
    return findInteractTarget(hero, SkillTools.cursorPositionAsPoint());
  }

  /**
   * Finds the interactable entity under the given point, provided it is also within interaction
   * range of the hero.
   *
   * @param hero the player entity
   * @param cursorPos the world-space point to search near
   * @return the targeted entity, or {@link Optional#empty()} if nothing qualifies
   */
  public static Optional<Entity> findInteractTarget(Entity hero, Point cursorPos) {
    Point heroPos = EntityUtils.getPosition(hero);

    return findCursorNearEntity(cursorPos)
        .filter(
            e -> {
              float range =
                  e.fetch(InteractionComponent.class)
                      .orElseThrow()
                      .interactions()
                      .interact()
                      .range();
              return heroPos.distanceSquared(EntityUtils.getPosition(e)) <= range * range;
            });
  }
}
