package gameOfGames.util;

import com.badlogic.gdx.graphics.Color;
import contrib.modules.interaction.InteractionComponent;
import contrib.utils.EntityUtils;
import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.CursorUtil;
import core.utils.Cursors;
import core.utils.Point;
import core.utils.components.draw.shader.OutlineShader;
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

  /** Updates cursor and outline feedback for the current mouse target. */
  public static void update() {
    Game.player()
        .ifPresent(
            hero -> {
              Optional<Entity> nearCursor = findCursorNearEntity();
              Optional<Entity> inRange = findInteractTarget(hero);

              clearHighlight(currentHighlightedEntity);
              clearHighlight(currentSemiHighlightedEntity);
              currentHighlightedEntity = null;
              currentSemiHighlightedEntity = null;

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
            });
  }

  private static Optional<Entity> findCursorNearEntity() {
    return EntityUtils.findEntityAtPoint(
        SkillTools.cursorPositionAsPoint(),
        Game.levelEntities(Set.of(PositionComponent.class, InteractionComponent.class)));
  }

  private static Optional<Entity> findInteractTarget(Entity hero) {
    Point heroPos = EntityUtils.getPosition(hero);

    return findCursorNearEntity()
        .filter(
            entity -> {
              float range =
                  entity
                      .fetch(InteractionComponent.class)
                      .orElseThrow()
                      .interactions()
                      .interact()
                      .range();
              return heroPos.distanceSquared(EntityUtils.getPosition(entity)) <= range * range;
            });
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
