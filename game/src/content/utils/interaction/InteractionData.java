package content.utils.interaction;

import api.components.InteractionComponent;
import api.components.PositionComponent;
import api.Entity;
import content.utils.position.Point;

public record InteractionData(
        Entity e, PositionComponent pc, InteractionComponent ic, float dist, Point unitDir) {}
