package contrib.client;

import core.System;
import core.game.ECSManagement;
import java.util.function.Supplier;

/** Shared registration helpers for contrib client startup installers. */
final class ContribClientSystemInstaller {

  private ContribClientSystemInstaller() {}

  static <T extends System> void addIfAbsent(Class<T> type, Supplier<T> factory) {
    if (!ECSManagement.systems().containsKey(type)) {
      ECSManagement.add(factory.get());
    }
  }
}
