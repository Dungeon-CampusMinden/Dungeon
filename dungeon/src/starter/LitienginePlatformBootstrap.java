package starter;

import core.System;
import core.game.ECSManagement;
import core.platform.Platform;
import core.platform.litiengine.LitiengineLoopHost;
import core.platform.litiengine.systems.LitiengineDebugControlsSystem;
import java.util.function.Supplier;

/** Explicitly wires LITIENGINE-specific startup steps into the platform abstraction. */
public final class LitienginePlatformBootstrap {
  private LitienginePlatformBootstrap() {}

  public static void init() {
    Platform.loopHost(new LitiengineLoopHost());
  }

  /** Installs the LITIENGINE debugger controls if they are not already present. */
  public static void installDebugger() {
    addIfAbsent(LitiengineDebugControlsSystem.class, LitiengineDebugControlsSystem::new);
  }

  private static <T extends System> void addIfAbsent(Class<T> type, Supplier<T> factory) {
    if (!ECSManagement.systems().containsKey(type)) {
      ECSManagement.add(factory.get());
    }
  }
}
