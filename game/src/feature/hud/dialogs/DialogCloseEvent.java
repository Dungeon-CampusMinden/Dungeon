package feature.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.Event;

/**
 * Local request to close a dialog through the configured close shortcut.
 *
 * <p>A dialog may handle this event to advance a sequence or ignore the request instead of closing.
 * Unhandled requests use the dialog's regular close callback.
 */
public final class DialogCloseEvent extends Event {}
