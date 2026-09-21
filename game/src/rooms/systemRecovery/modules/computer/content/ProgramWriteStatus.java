package rooms.systemRecovery.modules.computer.content;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import engine.Game;
import engine.network.messages.s2c.DialogFeedbackMessage;
import engine.sound.SoundSpec;
import engine.utils.Scene2dElementFactory;
import feature.hud.dialogs.DialogFeedbackFingerprint;
import feature.hud.UIUtils;
import java.util.Objects;
import rooms.systemRecovery.util.SystemRecoveryText;

/** Shared client-side status display for the two programmable System Recovery chips. */
final class ProgramWriteStatus extends Table {

  private static final float MINIMUM_DISPLAY_SECONDS = 0.65f;
  private static final float SUCCESS_VISIBLE_SECONDS = 0.25f;
  private static final Color SUCCESS_COLOR = new Color(0.12f, 0.65f, 0.25f, 1f);
  private static final Color FAILURE_COLOR = new Color(0.85f, 0.12f, 0.12f, 1f);
  private static final Color WRITING_COLOR = new Color(0.10f, 0.35f, 0.70f, 1f);

  private final Label feedback;
  private final ProgressBar progress;
  private final com.badlogic.gdx.scenes.scene2d.utils.Drawable normalFill;
  private final com.badlogic.gdx.scenes.scene2d.utils.Drawable successFill;
  private final com.badlogic.gdx.scenes.scene2d.utils.Drawable failureFill;
  private String submittedFingerprint;
  private DialogFeedbackMessage pendingFeedback;
  private Runnable successAction = () -> {};
  private boolean minimumDisplayElapsed;

  ProgramWriteStatus() {
    Skin skin = UIUtils.defaultSkin();
    top().left().defaults().growX();
    feedback = Scene2dElementFactory.createLabel("", 18, Color.BLACK);
    add(feedback).left().row();

    progress = Scene2dElementFactory.createProgressBar(0f, 1f, 0.01f, false);
    ProgressBar.ProgressBarStyle style = new ProgressBar.ProgressBarStyle(progress.getStyle());
    progress.setStyle(style);
    normalFill = style.knobBefore;
    successFill = skin.newDrawable("progress-bar-fill", SUCCESS_COLOR);
    failureFill = skin.newDrawable("progress-bar-fill", FAILURE_COLOR);
    add(progress).height(16).padTop(6).row();
    progress.setVisible(false);
  }

  /** Starts the visible write phase before the request is sent to the authoritative server. */
  void begin(String source) {
    submittedFingerprint = DialogFeedbackFingerprint.of(source);
    pendingFeedback = null;
    minimumDisplayElapsed = false;
    successAction = () -> {};
    feedback.getStyle().fontColor = WRITING_COLOR;
    feedback.setText(SystemRecoveryText.text("computer.write-in-progress"));
    setProgressFill(normalFill);
    progress.clearActions();
    progress.setValue(0f);
    progress.setVisible(true);
    progress.addAction(
        Actions.sequence(
            Actions.delay(MINIMUM_DISPLAY_SECONDS),
            Actions.run(
                () -> {
                  minimumDisplayElapsed = true;
                  applyPendingFeedback();
                })));
  }

  /** Applies a server result after the minimum visible write duration has elapsed. */
  void apply(DialogFeedbackMessage serverFeedback, Runnable onSuccess) {
    Objects.requireNonNull(serverFeedback, "serverFeedback");
    if (!serverFeedback.sourceFingerprint().isEmpty()
        && !serverFeedback.sourceFingerprint().equals(submittedFingerprint)) {
      return;
    }
    pendingFeedback = serverFeedback;
    successAction = onSuccess == null ? () -> {} : onSuccess;
    applyPendingFeedback();
  }

  private void applyPendingFeedback() {
    if (!minimumDisplayElapsed || pendingFeedback == null) return;

    DialogFeedbackMessage result = pendingFeedback;
    pendingFeedback = null;
    progress.clearActions();
    progress.setValue(1f);
    if (result.successful()) {
      setProgressFill(successFill);
      feedback.getStyle().fontColor = SUCCESS_COLOR;
      feedback.setText(SystemRecoveryText.text(result.messageKey()));
      Game.audio().playGlobal(SoundSpec.builder("retro_event_correct"));
      // Keep the completed green bar visible for at least one rendered frame before the dialog
      // closes. Calling the close callback immediately hides the value before Scene2D can draw it.
      Runnable closeDialog = successAction;
      successAction = () -> {};
      progress.addAction(
          Actions.sequence(
              Actions.delay(SUCCESS_VISIBLE_SECONDS), Actions.run(closeDialog)));
      return;
    }

    setProgressFill(failureFill);
    feedback.getStyle().fontColor = FAILURE_COLOR;
    feedback.setText(SystemRecoveryText.text(result.messageKey()));
    Game.audio().playGlobal(SoundSpec.builder("retro_event_wrong"));
  }

  private void setProgressFill(com.badlogic.gdx.scenes.scene2d.utils.Drawable fill) {
    ProgressBar.ProgressBarStyle style = new ProgressBar.ProgressBarStyle(progress.getStyle());
    style.knobBefore = fill;
    progress.setStyle(style);
  }
}
