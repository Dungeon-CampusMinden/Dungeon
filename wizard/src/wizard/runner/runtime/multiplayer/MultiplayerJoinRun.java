package wizard.runner.runtime.multiplayer;

import core.Game;
import core.game.ClientStarter;
import core.game.GameStarter;
import core.game.MainMenu;
import core.utils.Tuple;
import foundation.multiplayer.game.FoundationSnapshotTranslator;
import foundation.room.level.RoomLevel;
import foundation.room.model.FoundationRoom;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import wizard.runner.bootstrap.JoinBootstrapException;

/**
 * Immutable generic multiplayer client composition derived from the complete local DEER project.
 */
public final class MultiplayerJoinRun {
  private final FoundationRoom room;
  private final MultiplayerJoinGameSetup setup;
  private final AtomicBoolean started = new AtomicBoolean();

  private MultiplayerJoinRun(final FoundationRoom room) {
    this.room = Objects.requireNonNull(room, "room");
    setup = new MultiplayerJoinGameSetup(room);
  }

  /**
   * Creates a multiplayer join from the same complete room model used by the host.
   *
   * @param room complete locally derived room
   * @return immutable generic client composition
   */
  public static MultiplayerJoinRun from(final FoundationRoom room) {
    return new MultiplayerJoinRun(room);
  }

  /**
   * Runs the shared main menu exactly once.
   *
   * @param game menu and hosted-server configuration
   */
  public void runMainMenu(final GameStarter game) {
    Objects.requireNonNull(game, "game");
    runLifecycle(() -> MainMenu.run(game, clientStarter()));
  }

  /** Starts the generic client exactly once after installing bootstrap before transport start. */
  public void run() {
    runLifecycle(
        () -> {
          ClientStarter client = clientStarter();
          client.apply();
          Game.windowTitle("Foundation Runner Client");
          Game.run();
        });
  }

  private void runLifecycle(final Runnable lifecycle) {
    if (!started.compareAndSet(false, true)) {
      throw new IllegalStateException("Foundation multiplayer join was already started");
    }
    lifecycle.run();
    setup
        .failure()
        .ifPresent(
            reason -> {
              throw new JoinBootstrapException(reason);
            });
  }

  private ClientStarter clientStarter() {
    return ClientStarter.builder(setup::install)
        .levels(Tuple.of(MultiplayerHostRun.GENERIC_LEVEL_NAME, RoomLevel.class))
        .snapshotTranslator(new FoundationSnapshotTranslator(room))
        .disableAudio(true)
        .frameRate(60)
        .build();
  }
}
