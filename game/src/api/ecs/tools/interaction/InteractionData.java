package api.ecs.tools.interaction;

import api.ecs.components.InteractionComponent;
import api.ecs.components.PositionComponent;
import api.ecs.entities.Entity;
import api.utils.Point;

public record InteractionData(
        Entity e, PositionComponent pc, InteractionComponent ic, float dist, Point unitDir) {}
