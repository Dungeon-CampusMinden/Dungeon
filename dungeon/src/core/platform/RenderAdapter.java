package core.platform;

import core.System;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public interface RenderAdapter {

  record SystemBinding(Class<? extends System> type, Supplier<? extends System> factory) {}

  /** Which ECS systems should be registered when a profile includes rendering. */
  default List<SystemBinding> defaultRenderSystems() {
    return Collections.emptyList();
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
