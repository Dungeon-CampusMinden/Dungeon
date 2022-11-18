package character.objects;

import basiselements.hud.ScreenButton;
import basiselements.hud.ScreenImage;
import basiselements.hud.ScreenInput;
import basiselements.hud.TextButtonListener;
import tools.Point;

public class PasswordInputUI {

    ScreenImage background;
    ScreenInput passwordInput;
    ScreenButton ok;
    ScreenButton exit;

    public PasswordInputUI(TextButtonListener okListener, TextButtonListener exitListener) {
        background = new ScreenImage("hud/white.png", new Point(250, 170));
        passwordInput = new ScreenInput("Passwort?", new Point(300, 250));
        ok = new ScreenButton("OK", new Point(300, 180), okListener);
        exit = new ScreenButton("Abbrechen", new Point(350, 180), exitListener);
    }
}
