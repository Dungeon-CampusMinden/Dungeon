package feature.input.systems;

import engine.Entity;
import engine.Game;
import engine.System;
import engine.System.AuthoritativeSide;
import engine.systems.input.InputManager;
import feature.components.UIComponent;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.input.configuration.KeyboardConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/** Opens a room-specific controls dialog for the local player. */
public final class ControlsDialogSystem extends System {

  private final Supplier<String> controlsDialog;
  private final Map<Integer, UIComponent> openDialogs = new HashMap<>();

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
    discardClosedDialogs();
    if (Game.isHeadless() || !InputManager.isKeyJustPressed(KeyboardConfig.SHOW_CONTROLS.value())) {
      return;
    }
    Game.player().ifPresent(this::showControlsFor);
  }

  /**
   * Shows the active room's controls dialog for the given player.
   *
   * @param player player whose controls dialog should be shown
   */
  public void showControlsFor(Entity player) {
    showControlsFor(player, false);
  }

  /** Opens controls as part of the pause menu when explicitly requested by that menu. */
  public void showControlsFor(Entity player, boolean pausesPlayClock) {
    discardClosedDialogs();
    int playerId = player.id();
    if (openDialogs.containsKey(playerId)) return;
    UIComponent dialog = DialogFactory.showDialogDialog(controlsDialog.get(), () -> {}, playerId);
    if (pausesPlayClock)
      dialog.dialogContext().attributes().put(DialogContextKeys.PAUSES_PLAY_CLOCK, true);
    openDialogs.put(playerId, dialog);
  }

  private void discardClosedDialogs() {
    // Temporary HUD suppression does not end a dialog's lifetime.
    openDialogs
        .values()
        .removeIf(
            dialog ->
                dialog
                    .dialogContext()
                    .findEntity(DialogContextKeys.OWNER_ENTITY)
                    .flatMap(owner -> owner.fetch(UIComponent.class))
                    .filter(current -> current == dialog)
                    .isEmpty());
  }
}
