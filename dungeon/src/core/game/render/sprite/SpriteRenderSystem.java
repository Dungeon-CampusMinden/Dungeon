package core.game.render.sprite;

import core.Game;
import core.System;
import core.camera.CameraViewportState;
import core.components.PositionComponent;
import core.game.render.RenderContext;
import core.level.elements.ILevel;
import core.utils.logging.DungeonLogger;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.util.Optional;

/**
 * The {@code SpriteRenderSystem} is responsible for rendering game sprites, including both level
 * tiles and entities, onto the screen.
 *
 * <p>This system determines the viewable region of the level based on the current camera position
 * and renders all visible elements. The system operates as a client-server agnostic, render-only
 * system. It does not perform any game logic updates, and its execution logic is intentionally left
 * as a no-op.
 *
 * <p>The rendering process includes the following steps:
 *
 * <ul>
 *   <li>Calculating the camera view and viewport dimensions.
 *   <li>Rendering the level tiles based on the current camera viewport.
 *   <li>Rendering the entities visible within the viewport.
 *   <li>Configuring rendering hints for better visual fidelity.
 * </ul>
 *
 * <p>Any exceptions encountered during the render process are logged for debugging purposes.
 *
 * <p>Note: This system assumes that the game environment provides valid rendering context and that
 * entities contain a {@code PositionComponent}.
 */
public final class SpriteRenderSystem extends System {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(SpriteRenderSystem.class);

  private final SpriteViewportCalculator spriteViewportCalculator = new SpriteViewportCalculator();
  private final LevelTileRenderer levelTileRenderer = new LevelTileRenderer();
  private final EntitySpriteRenderer entitySpriteRenderer = new EntitySpriteRenderer();

  /**
   * Creates a new sprite render system.
   *
   * <p>This system operates on both the client and server sides and requires entities to have a
   * PositionComponent.
   */
  public SpriteRenderSystem() {
    super(AuthoritativeSide.BOTH, PositionComponent.class);
  }

  @Override
  public void execute() {
    // no-op (render-only system)
  }

  @Override
  public void render(float deltaSeconds) {
    final Graphics2D g = RenderContext.get();
    if (g == null) return;

    final Object oldInterpolation = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
    g.setRenderingHint(
        RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

    final AffineTransform oldTx = g.getTransform();

    try {
      final int screenWidth = getWidthSafe();
      final int screenHeight = getHeightSafe();

      // Background (keep explicit to avoid "garbage" frames)
      g.setColor(Color.BLACK);
      g.fillRect(0, 0, screenWidth, screenHeight);

      final Optional<ILevel> levelOpt = Game.currentLevel();
      final SpriteViewport view =
          spriteViewportCalculator.calculate(levelOpt, screenWidth, screenHeight);

      // Publish view for cursor mapping
      CameraViewportState.set(view.offsetX(), view.offsetY(), view.levelHeight(), view.tilePx());

      g.translate(view.offsetX(), view.offsetY());

      levelOpt.ifPresent(level -> levelTileRenderer.render(g, level, view));
      entitySpriteRenderer.render(g, view, screenWidth, screenHeight);
    } catch (Exception e) {
      LOGGER.warn("Sprite rendering failed: {}", e.getMessage(), e);
    } finally {
      g.setTransform(oldTx);
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldInterpolation);
    }
  }

  private int getWidthSafe() {
    try {
      return de.gurkenlabs.litiengine.Game.window().getWidth();
    } catch (Exception ignored) {
      return 1280;
    }
  }

  private int getHeightSafe() {
    try {
      return de.gurkenlabs.litiengine.Game.window().getHeight();
    } catch (Exception ignored) {
      return 720;
    }
  }
}
