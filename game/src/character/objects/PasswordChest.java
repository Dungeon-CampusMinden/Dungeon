package character.objects;

import basiselements.hud.TextButtonListener;
import collision.CharacterDirection;
import collision.Collidable;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import controller.ScreenController;
import tools.Point;

public class PasswordChest extends TreasureChest {

    private String password;
    private PasswordInputUI ui;
    private int attemptCounter = 0;
    private boolean correctPassword = false;
    private boolean interacting = false;
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

    public PasswordChest(Point position, String password) {
        super(position);
        this.password = password;
    }

    private void onOK() {
        if (password.equals(ui.passwordInput.getText())) {
            correctPassword = true;
            if (!isOpen) {
                currentAnimation = opening;
                inventory.forEach(i -> i.collect());
                inventory.clear();
                isOpen = true;
            }
            onExit();
        } else {
            attemptCounter++;
            System.out.println(attemptCounter);
        }
    }

    private void onExit() {
        ui.background.remove();
        ui.passwordInput.remove();
        ui.ok.remove();
        ui.exit.remove();
        interacting = false;
    }

    private void onCollision(ScreenController sc) {
        if (!interacting && !correctPassword) {
            interacting = true;
            ui = new PasswordInputUI(okListener, exitListener);
            sc.add(ui.background);
            sc.add(ui.passwordInput);
            sc.add(ui.ok);
            sc.add(ui.exit);
        }
    }

    /**
     * Action to do a collision
     *
     * @param other Object you colide with
     * @param from Direction from where you colide
     * @param sc ScreenController to use
     */
    public void colide(Collidable other, CharacterDirection from, ScreenController sc) {
        onCollision(sc);
    }

    public int getAttemptCounter() {
        return attemptCounter;
    }
}
