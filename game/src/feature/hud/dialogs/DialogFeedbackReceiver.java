package feature.hud.dialogs;

import engine.network.messages.s2c.DialogFeedbackMessage;

/** Receives server-authoritative updates for an already open dialog. */
public interface DialogFeedbackReceiver {

  /**
   * Applies a feedback message to the visible dialog.
   *
   * @param feedback server response to apply
   */
  void applyFeedback(DialogFeedbackMessage feedback);
}
