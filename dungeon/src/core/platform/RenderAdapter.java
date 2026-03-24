package core.platform;

import core.System;
import core.ui.StageHandle;
import core.utils.Point;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public interface RenderAdapter {

  record SystemBinding(Class<? extends System> type, Supplier<? extends System> factory) {}

  /** Which ECS systems should be registered when a profile includes rendering. */
  default List<SystemBinding> defaultRenderSystems() {
    return Collections.emptyList();
  }

  /**
   * Projects a world-space point into stage/UI coordinates for the currently active backend.
   *
   * <p>The returned point is expressed in the coordinate system of the provided stage handle.
   * Backends that do not support this operation may return {@link Optional#empty()}.
   */
  default Optional<Point> projectWorldToStage(Point worldPoint, StageHandle stageHandle) {
    return Optional.empty();
  }

  /** Pre-Multiplied Alpha (PMA) blending via backend-specific render state. */
  default void setPMABlending() {}

  /** Pre-Multiplied Alpha (PMA) blending on a backend-specific batch object (or null). */
  default void setPMABlending(Object batch) {}

  /** Straight alpha blending via backend-specific render state. */
  default void setStraightAlphaBlending() {}

  /** Straight alpha blending on a backend-specific batch object (or null). */
  default void setStraightAlphaBlending(Object batch) {}
}
