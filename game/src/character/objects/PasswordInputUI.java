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
        background = new ScreenImage("hud/white.png", new Point(0, 70));
        passwordInput = new ScreenInput("Passwort?", new Point(50, 150));
        ok = new ScreenButton("OK", new Point(0, 80), okListener);
        exit = new ScreenButton("Abbrechen", new Point(50, 80), exitListener);
    }
}
