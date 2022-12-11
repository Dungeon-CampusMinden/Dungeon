package mydungeon;

import controller.Game;
import ecs.components.ComponentStore;
import ecs.systems.ISystem;
import ecs.systems.MovementSystem;
import java.util.ArrayList;
import java.util.List;
import starter.DesktopLauncher;

public class ECS extends Game {

    public static ComponentStore positionStore;
    public static ComponentStore velocityStore;

    private List<ISystem> systems;

    @Override
    protected void setup() {
        controller.clear();
        systems = new ArrayList<>();
        positionStore = new ComponentStore();
        velocityStore = new ComponentStore();
        systems.add(new MovementSystem());
    }

    @Override
    protected void frame() {
        systems.forEach(s -> s.update());
    }

    @Override
    public void onLevelLoad() {}

    public static void main(String[] args) {
        // start the game
        DesktopLauncher.run(new ECS());
    }
}
