package contrib.utils.componentUtils.interactionComponent;

import contrib.component.InteractionComponent;
import core.Entity;
import core.components.PositionComponent;
import core.utils.Point;

public record InteractionData(
        Entity e, PositionComponent pc, InteractionComponent ic, float dist, Point unitDir) {}
