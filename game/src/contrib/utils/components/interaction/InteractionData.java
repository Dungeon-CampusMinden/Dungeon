package contrib.utils.components.interaction;

import contrib.components.InteractionComponent;

import core.Entity;
import core.components.PositionComponent;
import core.utils.Point;

public record InteractionData(
        Entity e, PositionComponent pc, InteractionComponent ic, float dist, Point unitDir) {}
