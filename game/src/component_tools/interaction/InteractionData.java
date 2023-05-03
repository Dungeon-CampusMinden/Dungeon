package component_tools.interaction;

import components.InteractionComponent;
import components.PositionComponent;
import entities.Entity;
import component_tools.position.Point;

public record InteractionData(
        Entity e, PositionComponent pc, InteractionComponent ic, float dist, Point unitDir) {}
