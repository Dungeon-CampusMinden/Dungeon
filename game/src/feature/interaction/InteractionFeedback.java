package feature.interaction;

import com.badlogic.gdx.graphics.Color;
import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.input.CursorUtils;
import engine.utils.CursorUtil;
import engine.utils.Cursors;
import engine.utils.Point;
import engine.utils.components.draw.shader.OutlineShader;
import feature.entities.HeroController;
import feature.utils.EntityUtils;
import java.util.Optional;
import java.util.Set;

/** Client-side cursor and outline feedback for interactable world entities. */
public final class InteractionFeedback {

  private static final String SHADER_NAME = "highlight_outline";
  private static final Color HIGHLIGHT_SOLID = new Color(0.8f, 0, 0, 1f);
  private static final Color HIGHLIGHT_SEMI = new Color(0.8f, 0.7f, 0, 0.4f);

  private static Entity currentHighlightedEntity;
  private static Entity currentSemiHighlightedEntity;

  private InteractionFeedback() {}

  /**
   * Updates cursor and outline feedback for the current mouse target.
   *
   * <p>The entity under the cursor receives a semi-transparent discovery outline. If that entity is
   * also within interaction range of the hero, it receives a solid outline and the world cursor
   * switches to {@link Cursors#INTERACT}.
   */
  public static void update() {
    Optional<Entity> hero = Game.player();

    if (hero.isEmpty()) {
      clearFeedback();
      return;
    }

    Point cursorPosition = CursorUtils.positionInWorld();
    Optional<Entity> nearCursor = findCursorNearEntity(cursorPosition);
    Optional<Entity> inRange = HeroController.findInteractable(hero.orElseThrow(), cursorPosition);

    clearHighlights();

    nearCursor.ifPresent(
        entity -> {
          if (inRange.isPresent() && entity.id() == inRange.orElseThrow().id()) {
            applyOutline(entity, HIGHLIGHT_SOLID);
            currentHighlightedEntity = entity;
          } else {
            applyOutline(entity, HIGHLIGHT_SEMI);
            currentSemiHighlightedEntity = entity;
          }
        });

    updateWorldCursor(inRange.isPresent());
  }

  /**
   * Finds the interactable entity under the current cursor position, regardless of hero range.
   *
   * @return the entity under the cursor, or {@link Optional#empty()} if none qualifies
   */
  public static Optional<Entity> findCursorNearEntity() {
    return findCursorNearEntity(CursorUtils.positionInWorld());
  }

  /**
   * Finds the interactable entity under the given point, regardless of hero range.
   *
   * @param point the world-space point to search near
   * @return the entity under the point, or {@link Optional#empty()} if none qualifies
   */
  public static Optional<Entity> findCursorNearEntity(Point point) {
    return EntityUtils.findEntityAtPoint(
        point, Game.levelEntities(Set.of(PositionComponent.class, InteractionComponent.class)));
  }

  /** Clears all currently active interaction feedback. */
  public static void clearFeedback() {
    clearHighlights();
    CursorUtil.clearWorldCursor();
  }

  private static void clearHighlights() {
    clearHighlight(currentHighlightedEntity);
    clearHighlight(currentSemiHighlightedEntity);
    currentHighlightedEntity = null;
    currentSemiHighlightedEntity = null;
  }

  private static void updateWorldCursor(boolean hasTarget) {
    if (hasTarget) {
      CursorUtil.setWorldCursor(Cursors.INTERACT);
    } else {
      CursorUtil.clearWorldCursor();
    }
  }

  private static void clearHighlight(Entity entity) {
    if (entity == null) {
      return;
    }
    entity.fetch(DrawComponent.class).ifPresent(dc -> dc.shaders().remove(SHADER_NAME));
  }

  private static void applyOutline(Entity entity, Color color) {
    entity
        .fetch(DrawComponent.class)
        .ifPresent(dc -> dc.shaders().add(SHADER_NAME, new OutlineShader(1, color)));
  }
}
