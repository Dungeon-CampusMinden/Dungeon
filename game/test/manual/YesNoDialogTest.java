package manual;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import contrib.hud.dialogs.TextDialog;
import contrib.hud.dialogs.YesNoDialog;
import contrib.systems.HudSystem;
import core.Game;
import core.utils.IVoidFunction;

/**
 * This is a manual test for the YesNODialog.
 *
 * <p>It sets up a basic game and will show a Yes-No if "F" is pressed.
 *
 * <p>Use this to check if the UI is displayed correctly.
 *
 * <p>Start this with ./gradlew runYesNoDialogTest
 */
public class YesNoDialogTest {

    public static void main(String[] args) {
        Game.initBaseLogger();
        Game.add(new HudSystem());
        Game.userOnFrame(
                () -> {
                    if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
                        YesNoDialog.showYesNoDialog(
                                "Test",
                                "Test",
                                new IVoidFunction() {
                                    @Override
                                    public void execute() {
                                        TextDialog.textDialog("Yes", "Ok", "Callback for yes.");
                                    }
                                },
                                new IVoidFunction() {
                                    @Override
                                    public void execute() {
                                        TextDialog.textDialog("No", "Ok", "Callback for no.");
                                    }
                                });
                    }
                });

        // build and start game
        Game.run();
    }
}
