package contrib.utils.componentUtils.interactionComponent;

import core.Entity;
import core.components.PositionComponent;
import core.utils.Point;
import contrib.component.InteractionComponent;

public record InteractionData(
        Entity e, PositionComponent pc, InteractionComponent ic, float dist, Point unitDir) {}
