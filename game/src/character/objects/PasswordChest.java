package character.objects;

import collision.Collidable;
import controller.ScreenController;
import tools.Point;

public class PasswordChest extends TreasureChest implements Collidable {

    private String password;
    private boolean correctPassword = false;
    private PasswordInputUI ui;

    public PasswordChest(Point position, String password) {
        super(position);
        this.password = password;
        ui = new PasswordInputUI();
    }

    public void onCollision(ScreenController sc) {
        if (!correctPassword) {
            sc.add(ui.ok);
            sc.add(ui.exit);
        }
    }
}
