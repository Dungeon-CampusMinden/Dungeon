package core.platform;

import core.Entity;
import core.System;
import core.ui.StageHandle;
import core.utils.Point;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Platform adapter interface for graphics rendering and rendering-related operations.
 *
 * <p>RenderAdapter abstracts rendering functionality, allowing different rendering backends
 * (e.g., LibGDX, AWT, custom renderers) to provide rendering capabilities without coupling the
 * game engine to a specific implementation.
 *
 * <p>Key responsibilities:
 * <ul>
 *   <li>Providing default rendering systems for the engine
 *   <li>Coordinate transformation between world and screen/stage space
 *   <li>Blending mode control (PMA and straight alpha)
 *   <li>Entity depth/z-ordering management
 *   <li>Debug visualization toggle
 * </ul>
 *
 * <p>All methods provide default no-op implementations, allowing implementations to selectively
 * support features based on the rendering backend capabilities.
 */
public interface RenderAdapter {

  /**
   * Record representing a system type binding with its factory.
   *
   * <p>Used to define render systems that should be automatically registered when the adapter
   * is initialized.
   *
   * @param type the System class type to register
   * @param factory a supplier function that creates instances of the system
   */
  record SystemBinding(Class<? extends System> type, Supplier<? extends System> factory) {}

  /**
   * Provides the default rendering systems for this adapter.
   *
   * <p>These systems are automatically registered in the engine when the adapter is initialized.
   * Implementations can return a list of systems specific to their rendering backend.
   * The default implementation returns an empty list.
   *
   * @return a list of system bindings, or an empty list if no default systems are provided
   */
  default List<SystemBinding> defaultRenderSystems() {
    return Collections.emptyList();
  }

  /**
   * Projects a world-space point to stage/screen coordinates.
   *
   * <p>This method performs any necessary coordinate transformations (e.g., applying camera
   * transformation, viewport scaling) to convert a world-space coordinate to stage/screen space.
   * The default implementation returns an empty Optional.
   *
   * @param worldPoint the point in world coordinates to project
   * @param stageHandle the stage to project onto
   * @return an Optional containing the projected point in stage/screen coordinates, or empty if the projection is not supported
   */
  default Optional<Point> projectWorldToStage(Point worldPoint, StageHandle stageHandle) {
    return Optional.empty();
  }

  /**
   * Enables Premultiplied Alpha (PMA) blending mode.
   *
   * <p>PMA blending is typically used for optimized alpha blending with premultiplied color channels.
   * This version applies to all rendering operations. The default implementation is a no-op.
   */
  default void setPMABlending() {}

  /**
   * Enables Premultiplied Alpha (PMA) blending mode for a specific batch object.
   *
   * <p>PMA blending is typically used for optimized alpha blending with premultiplied color channels.
   * This version applies to the specified batch object. The default implementation is a no-op.
   *
   * @param batch the batch object to configure (framework-specific type)
   */
  default void setPMABlending(Object batch) {}

  /**
   * Enables straight (non-premultiplied) alpha blending mode.
   *
   * <p>Straight alpha blending treats alpha channels without premultiplication.
   * This version applies to all rendering operations. The default implementation is a no-op.
   */
  default void setStraightAlphaBlending() {}

  /**
   * Enables straight (non-premultiplied) alpha blending mode for a specific batch object.
   *
   * <p>Straight alpha blending treats alpha channels without premultiplication.
   * This version applies to the specified batch object. The default implementation is a no-op.
   *
   * @param batch the batch object to configure (framework-specific type)
   */
  default void setStraightAlphaBlending(Object batch) {}

  /**
   * Changes the rendering depth (z-order) of an entity.
   *
   * <p>Depth typically controls the layering order: entities with higher depth values are
   * rendered on top of entities with lower depth values. The default implementation is a no-op.
   *
   * @param entity the entity whose depth should be changed
   * @param depth the new depth/z-order value
   */
  default void changeEntityDepth(Entity entity, int depth) {}

  /**
   * Toggles the debug HUD on and off.
   *
   * <p>The debug HUD typically displays diagnostic information such as performance metrics,
   * collision bounds, or other debugging visualizations. The default implementation is a no-op.
   */
  default void toggleDebugHud() {
    // no-op by default
  }
}
