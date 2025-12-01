package manual;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import contrib.hud.dialogs.DialogFactory;
import contrib.systems.HudSystem;
import core.Game;

/**
 * This is a manual test for the YesNoDialog via DialogFactory.
 *
 * <p>It sets up a basic game and will show a Yes-No if "F" is pressed.
 *
 * <p>Use this to check if the UI is displayed correctly.
 *
 * <p>Start this with ./gradlew runYesNoDialogTest
 */
public class YesNoDialogTest {

  /**
   * Main method.
   *
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    Game.add(new HudSystem());
    Game.userOnFrame(
        () -> {
          if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            DialogFactory.showYesNoDialog(
                "Test",
                "Test",
                () -> {
                  DialogFactory.showTextDialog(
                      "Yes", "Callback for yes.", () -> {}, "Ok", null, null, null);
                },
                () -> {
                  DialogFactory.showTextDialog(
                      "No", "Callback for no.", () -> {}, "Ok", null, null, null);
                });
          }
        });

    // build and start game
    Game.run();
  }
}
