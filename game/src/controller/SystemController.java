package controller;

import ecs.systems.ECS_System;
import logging.CustomLogLevel;

/** used to integrate ECS_Systems in PM-Dungeon game loop */
public class SystemController extends AbstractController<ECS_System> {

    public SystemController() {
        super();
    }

    @Override
    public void process(ECS_System e) {
        if (e.isRunning()) {
            e.systemLogger.log(
                    CustomLogLevel.TRACE,
                    "System '" + e.getClass().getSimpleName() + "' is running.");
            e.update();
        }
    }
}
