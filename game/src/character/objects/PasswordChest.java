package character.objects;

import basiselements.hud.TextButtonListener;
import collision.CharacterDirection;
import collision.Collidable;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import controller.ScreenController;
import quest.Evaluateable;
import tools.Point;

public class PasswordChest extends TreasureChest {

    private String password;
    private PasswordInputUI ui;
    private ScreenController screenController;
    private int falseAttempts = 0;
    private boolean correctPassword = false;
    private boolean interacting = false;

    private Evaluateable evaluateable;
    private TextButtonListener okListener =
            new TextButtonListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    onOK();
                }
            };

    private TextButtonListener exitListener =
            new TextButtonListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    onExit();
                }
            };

    public PasswordChest(
            Point position, String password, ScreenController sc, Evaluateable evaluateable) {
        super(position);
        this.password = password;
        screenController = sc;
        this.evaluateable = evaluateable;
    }

    private void onOK() {
        if (password.equals(ui.getPasswordInput().getText())) {
            correctPassword = true;
            if (!isOpen) {
                open();
                evaluateable.evaluate();
            }
            onExit();
        } else {
            falseAttempts++;
            System.out.println(falseAttempts);
        }
    }

    private void onExit() {
        ui.getBackground().remove();
        ui.getPasswordInput().remove();
        ui.getOk().remove();
        ui.getExit().remove();
        interacting = false;
    }

    /**
     * Action to do a collision
     *
     * @param other Object you colide with
     * @param from Direction from where you colide
     */
    public void colide(Collidable other, CharacterDirection from) {
        if (!interacting && !correctPassword) {
            interacting = true;
            ui = new PasswordInputUI(okListener, exitListener);
            screenController.add(ui.getBackground());
            screenController.add(ui.getPasswordInput());
            screenController.add(ui.getOk());
            screenController.add(ui.getExit());
        }
    }

    public int getFalseAttempts() {
        return falseAttempts;
    }
}
