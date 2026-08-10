package wizard.runner.runtime.multiplayer;

import core.Game;
import core.network.ConnectionListener;
import foundation.multiplayer.bootstrap.ClientBootstrapCoordinator;
import foundation.multiplayer.game.FoundationDialogs;
import foundation.room.asset.RuntimeAssetBinder;
import foundation.room.model.FoundationRoom;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/** Client composition gated by equality of the locally and remotely derived room identity. */
final class MultiplayerJoinGameSetup {
  private final FoundationRoom room;
  private final AtomicReference<String> failure = new AtomicReference<>();
  private ClientBootstrapCoordinator coordinator;
  private boolean installed;

  MultiplayerJoinGameSetup(final FoundationRoom room) {
    this.room = Objects.requireNonNull(room, "room");
  }

  void install() {
    if (installed) {
      throw new IllegalStateException("Foundation multiplayer join setup was already installed");
    }
    installed = true;
    FoundationDialogs.register();
    RuntimeAssetBinder binder = RuntimeAssetBinder.rendering();
    coordinator =
        new ClientBootstrapCoordinator(
            Game.network().messageDispatcher(),
            roomInputSha256 -> {
              if (!room.inputSha256().equals(roomInputSha256)) {
                throw new IllegalArgumentException(
                    "Host and client use different Wizard room projects");
              }
              binder.bind(room.assets());
              Game.windowTitle(room.title() + " Client");
            },
            this::failJoin);
    coordinator.install();
    Game.network()
        .addConnectionListener(
            new ConnectionListener() {
              @Override
              public void onConnected() {
                coordinator.resetConnection();
              }

              @Override
              public void onDisconnected(final String reason) {
                coordinator.resetConnection();
              }
            });
  }

  void failJoin(final String reason) {
    String bounded = reason == null || reason.isBlank() ? "Foundation bootstrap failed" : reason;
    if (failure.compareAndSet(null, bounded)) {
      System.err.println(bounded);
      Game.exit(bounded);
    }
  }

  Optional<String> failure() {
    return Optional.ofNullable(failure.get());
  }
}
