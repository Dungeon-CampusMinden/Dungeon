package character.objects;

import basiselements.hud.ScreenButton;
import basiselements.hud.TextButtonListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import tools.Point;

public class PasswordInputUI {

    protected ScreenButton ok;
    protected ScreenButton exit;
    private TextButtonListener okListener;
    private TextButtonListener exitListener;

    public PasswordInputUI() {
        okListener =
                new TextButtonListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        System.out.println("OK wurde gedrückt");
                    }
                };

        exitListener =
                new TextButtonListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        System.out.println("Abbrechen wurde gedrückt");
                    }
                };

        ok = new ScreenButton("OK", new Point(0, 80), okListener);
        exit = new ScreenButton("Abbrechen", new Point(50, 80), exitListener);
    }
}
