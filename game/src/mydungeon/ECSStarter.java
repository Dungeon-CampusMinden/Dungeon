package mydungeon;

import controller.Game;
import starter.DesktopLauncher;

public class ECSStarter extends Game {

    @Override
    protected void setup() {
        controller.clear();
    }

    @Override
    protected void frame() {}

    @Override
    public void onLevelLoad() {}

    public static void main(String[] args) {
        // start the game
        DesktopLauncher.run(new ECSStarter());
    }
}
