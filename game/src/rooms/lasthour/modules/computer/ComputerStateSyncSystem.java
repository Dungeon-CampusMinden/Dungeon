package rooms.lasthour.modules.computer;

import engine.System;

/** Keeps the local computer dialog synchronized with the authoritative computer state. */
public class ComputerStateSyncSystem extends System {
  /** Creates the local dialog synchronization system. */
  public ComputerStateSyncSystem() {
    super(AuthoritativeSide.CLIENT);
  }

  @Override
  public void execute() {
    ComputerStateComponent.getState()
        .ifPresent(
            state ->
                ComputerDialog.getInstance()
                    .ifPresent(
                        dialog -> {
                          if (dialog.sharedState() != state) dialog.updateState(state);
                        }));
  }

  /** Reading dialogs keep receiving authoritative state. */
  @Override
  public void stop() {}
}
