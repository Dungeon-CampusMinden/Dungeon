package contrib.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import java.util.function.BiFunction;

/** A subclass of Dialog that allows the result handler to be defined per functional interface. */
public final class HandledDialog extends Dialog {

  /** Handler for Button presses. */
  private final BiFunction<Dialog, String, Boolean> resultHandler;

  /**
   * Creates an instance with the given title and skin and stores the functional interface for
   * Button events.
   *
   * @param skin Skin for the dialog (resources that can be used by UI widgets)
   * @param title Title of the dialog
   * @param resultHandler callbacks for the button presses
   */
  public HandledDialog(String title, Skin skin, BiFunction<Dialog, String, Boolean> resultHandler) {
    super(title, skin, !title.isBlank() ? "default" : "no-title");
    this.resultHandler = resultHandler;
  }

  /**
   * Creates an instance with the given title and skin and stores the functional interface for
   * Button events.
   *
   * @param title Title of the dialog
   * @param skin Skin for the dialog (resources that can be used by UI widgets)
   * @param windowStyleName the name of the style which should be used
   * @param resultHandler callbacks for the button presses
   */
  public HandledDialog(
      String title,
      Skin skin,
      String windowStyleName,
      BiFunction<Dialog, String, Boolean> resultHandler) {
    super(title, skin, windowStyleName);
    this.resultHandler = resultHandler;
  }

  /**
   * When a Button event happens, calls the stored resultHandler. When the resultHandler returns
   * false, stops the default hide on button press.
   *
   * @param object Object associated with the button
   */
  @Override
  protected void result(final Object object) {
    if (!resultHandler.apply(this, object.toString())) cancel();
  }
}
