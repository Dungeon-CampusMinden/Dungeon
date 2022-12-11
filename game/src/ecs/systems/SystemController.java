package ecs.systems;

import controller.AbstractController;

/** used to integrate ECS_Systems in PM-Dungeon game loop */
public class SystemController extends AbstractController<ECS_System> {

    public SystemController() {
        super();
    }

    @Override
    public void process(ECS_System e) {
        e.update();
    }
}
