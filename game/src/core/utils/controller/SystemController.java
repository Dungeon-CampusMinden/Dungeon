package core.utils.controller;

import core.System;

/** used to integrate Systems in PM-Dungeon game loop */
public class SystemController extends AbstractController<System> {

    public SystemController() {
        super();
    }

    @Override
    public void process(System e) {
        if (e.isRunning()) {
            e.execute();
        }
    }
}
