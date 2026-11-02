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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

/** Client-side cursor and outline feedback for interactable world entities. */
public final class InteractionFeedback {

  private static final String SHADER_NAME = "highlight_outline";
  private static final Color HIGHLIGHT_SOLID = new Color(0.8f, 0, 0, 1f);
  private static final Color HIGHLIGHT_SEMI = new Color(0.8f, 0.7f, 0, 0.4f);

  private static final List<Entity> outlinedEntities = new ArrayList<>();
  // Picks the entity the hero would use while every interactable entity shows its outline, or null
  // while the outline follows the cursor.
  private static Supplier<Optional<Entity>> fixedTarget;
  // Picks the interactable entities that show an outline while fixedTarget is set.
  private static Predicate<Entity> outlined = entity -> true;

  private InteractionFeedback() {}

  /**
   * Updates cursor and outline feedback for the current mouse target.
   *
   * <p>The entity under the cursor receives a semi-transparent discovery outline. If that entity is
   * also within interaction range of the hero, it receives a solid outline and the world cursor
   * switches to {@link Cursors#INTERACT}.
   *
   * <p>After {@link #showAlways(Supplier)}, the cursor plays no part. Every interactable entity
   * receives the discovery outline, and the entity the hero would use receives the solid one. After
   * {@link #showAlways(Supplier, Predicate)}, only the entities the filter accepts show outlines.
   */
  public static void update() {
    Optional<Entity> hero = Game.player();

    if (hero.isEmpty() || Game.hud().blocksGameplayInput(hero.orElseThrow())) {
      clearFeedback();
      return;
    }
    if (fixedTarget != null) {
      outlineAll(hero.orElseThrow());
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
          } else {
            applyOutline(entity, HIGHLIGHT_SEMI);
          }
        });

    updateWorldCursor(inRange.isPresent());
  }

  /**
   * Outlines every interactable entity from now on, for a game that people follow on a shared
   * screen without a cursor. The entity that {@code target} picks receives the solid outline, all
   * others the discovery outline. The cursor no longer moves the outlines or changes its shape.
   *
   * @param target picks the entity the hero would use next, if any
   */
  public static void showAlways(Supplier<Optional<Entity>> target) {
    showAlways(target, entity -> true);
  }

  /**
   * Like {@link #showAlways(Supplier)}, but only the interactable entities that {@code outlined}
   * accepts show an outline, for a game in which only some of them can be chosen right now.
   *
   * @param target picks the entity the hero would use next, if any
   * @param outlined whether an interactable entity shows an outline
   */
  public static void showAlways(Supplier<Optional<Entity>> target, Predicate<Entity> outlined) {
    fixedTarget = Objects.requireNonNull(target);
    InteractionFeedback.outlined = Objects.requireNonNull(outlined);
  }

  /** Lets the outline follow the cursor again, as before {@link #showAlways(Supplier)}. */
  public static void followCursor() {
    fixedTarget = null;
    outlined = entity -> true;
  }

  private static void outlineAll(Entity hero) {
    clearHighlights();
    Optional<Entity> target = fixedTarget.get();
    Game.entities(Set.of(PositionComponent.class, InteractionComponent.class))
        .filter(entity -> entity.id() != hero.id())
        .filter(outlined)
        .forEach(
            entity -> {
              boolean used = target.isPresent() && target.orElseThrow().id() == entity.id();
              applyOutline(entity, used ? HIGHLIGHT_SOLID : HIGHLIGHT_SEMI);
            });
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
    outlinedEntities.forEach(InteractionFeedback::clearHighlight);
    outlinedEntities.clear();
  }

  private static void updateWorldCursor(boolean hasTarget) {
    if (hasTarget) {
      CursorUtil.setWorldCursor(Cursors.INTERACT);
    } else {
      CursorUtil.clearWorldCursor();
    }
  }

  private static void clearHighlight(Entity entity) {
    entity.fetch(DrawComponent.class).ifPresent(dc -> dc.shaders().remove(SHADER_NAME));
  }

  private static void applyOutline(Entity entity, Color color) {
    entity
        .fetch(DrawComponent.class)
        .ifPresent(dc -> dc.shaders().add(SHADER_NAME, new OutlineShader(1, color)));
    outlinedEntities.add(entity);
  }
}
