package character.objects;

import basiselements.hud.TextButtonListener;
import character.player.Hero;
import collision.CharacterDirection;
import collision.Collidable;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import controller.ScreenController;
import tools.Point;

public class PasswordChest extends TreasureChest {

    private String password;
    private boolean correctPassword = false;
    private PasswordInputUI ui;
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
            if(!isOpen){
                currentAnimation = opening;
                inventory.forEach(i -> i.collect());
                inventory.clear();
                isOpen = true;
            }
            onExit();
        }
    }

    private void onExit() {
        ui.passwordInput.remove();
        ui.ok.remove();
        ui.exit.remove();
        interacting = false;
    }

    private void onCollision(ScreenController sc){
        if (!interacting && !correctPassword){
            interacting = true;
            ui= new PasswordInputUI(okListener, exitListener);
            sc.add(ui.passwordInput);
            sc.add(ui.ok);
            sc.add(ui.exit);
        }
    }

    public void colide(Collidable other, CharacterDirection from, ScreenController sc) {
        onCollision(sc);
    }
}
