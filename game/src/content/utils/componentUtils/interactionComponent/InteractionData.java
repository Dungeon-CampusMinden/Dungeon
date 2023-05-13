package content.utils.componentUtils.interactionComponent;

import api.Entity;
import api.components.PositionComponent;
import api.utils.Point;
import content.component.InteractionComponent;

public record InteractionData(
        Entity e, PositionComponent pc, InteractionComponent ic, float dist, Point unitDir) {}
