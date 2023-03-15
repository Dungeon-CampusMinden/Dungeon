package ecs.tools.interaction;

import ecs.components.InteractionComponent;
import ecs.components.PositionComponent;
import ecs.entities.Entity;
import tools.Point;

public record InteractionData(
        Entity e, PositionComponent pc, InteractionComponent ic, float dist, Point unitDir) {}
