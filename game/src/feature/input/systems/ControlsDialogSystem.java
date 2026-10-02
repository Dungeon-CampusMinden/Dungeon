package feature.input.systems;

import engine.Entity;
import engine.Game;
import engine.System;
import engine.System.AuthoritativeSide;
import engine.systems.input.InputManager;
import feature.hud.dialogs.DialogFactory;
import feature.input.configuration.KeyboardConfig;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/** Opens a room-specific controls dialog for the local player. */
public final class ControlsDialogSystem extends System {

  private final Supplier<String> controlsDialog;
  private final Set<Integer> openDialogPlayers = new HashSet<>();

  /**
   * Creates the system with the controls dialog used by the active room.
   *
   * @param controlsDialog supplier for the localized controls dialog script
   */
  public ControlsDialogSystem(Supplier<String> controlsDialog) {
    super(AuthoritativeSide.CLIENT);
    this.controlsDialog = Objects.requireNonNull(controlsDialog, "controlsDialog");
  }

  @Override
  public void execute() {
    if (Game.isHeadless() || !InputManager.isKeyJustPressed(KeyboardConfig.SHOW_CONTROLS.value())) {
      return;
    }
    Game.player().ifPresent(this::showControls);
  }

  private void showControls(Entity player) {
    int playerId = player.id();
    if (!openDialogPlayers.add(playerId)) return;
    DialogFactory.showDialogDialog(
        controlsDialog.get(), () -> openDialogPlayers.remove(playerId), playerId);
  }
}
