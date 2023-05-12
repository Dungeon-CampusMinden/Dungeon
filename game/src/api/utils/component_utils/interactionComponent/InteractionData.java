package api.utils.component_utils.interactionComponent;

import api.Entity;
import api.components.InteractionComponent;
import api.components.PositionComponent;
import api.utils.Point;

public record InteractionData(
        Entity e, PositionComponent pc, InteractionComponent ic, float dist, Point unitDir) {}
