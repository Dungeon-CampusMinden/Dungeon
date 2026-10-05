package feature.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.Event;

/**
 * Local request to close a dialog through the configured close shortcut.
 *
 * <p>A dialog may consume this request to advance a sequence or ignore it. Unhandled requests use
 * the dialog's regular close callback if the dialog can be closed.
 */
public final class DialogCloseEvent extends Event {}
