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
}
